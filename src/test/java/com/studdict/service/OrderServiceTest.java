package com.studdict.service;

import com.studdict.dto.OrderItemRequest;
import com.studdict.model.Bill;
import com.studdict.model.MenuItem;
import com.studdict.model.Order;
import com.studdict.model.StudyTable;
import com.studdict.repository.BillRepository;
import com.studdict.repository.MenuItemRepository;
import com.studdict.repository.OrderRepository;
import com.studdict.repository.StudyTableRepository;
import com.studdict.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StudyTableRepository studyTableRepository;

    @Mock
    private BillRepository billRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddCartItem_ItemAvailable_ReturnsTrue() {
        // Arrange
        MenuItem item = new MenuItem();
        item.setAvailable(true);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act
        boolean result = orderService.addCartItem(1L, 2);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testAddCartItem_ItemUnavailable_ThrowsException() {
        // Arrange
        MenuItem item = new MenuItem();
        item.setAvailable(false);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.addCartItem(1L, 1);
        });

        assertEquals("Το προϊόν δεν είναι διαθέσιμο.", exception.getMessage());
    }

    @Test
    public void testCreateOrder_CalculatesCostCorrectly() {
        // Arrange
        StudyTable table = new StudyTable();
        when(studyTableRepository.findById(1)).thenReturn(Optional.of(table));

        MenuItem item = new MenuItem();
        item.setPrice(2.50);
        item.setAvailable(true);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderItemRequest request = new OrderItemRequest(1L, 3); // 3 items * 2.50 = 7.50

        // Act
        Order order = orderService.createOrder(1, Collections.singletonList(request));

        // Assert
        assertNotNull(order);
        assertEquals(7.50, order.getTotalAmount(), 0.001);
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(billRepository, times(1)).save(any(Bill.class));
    }

    @Test
    public void testCreateOrder_SecondOrderAccumulatesOntoExistingBill() {
        // Arrange: a table that already has a previous, un-checked-out order of €10.00.
        StudyTable table = new StudyTable();
        when(studyTableRepository.findById(1)).thenReturn(Optional.of(table));

        MenuItem item = new MenuItem();
        item.setPrice(2.50);
        item.setAvailable(true);
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(item));

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // After saving the new order, the table has both orders: €10.00 + €7.50 (3 * 2.50).
        Order previousOrder = new Order();
        previousOrder.setTotalAmount(10.00);
        Order newOrder = new Order();
        newOrder.setTotalAmount(7.50);
        when(orderRepository.findByTableId(1)).thenReturn(Arrays.asList(previousOrder, newOrder));

        // An unsettled bill already exists for the table — it must be reused, not duplicated.
        Bill existingBill = new Bill();
        existingBill.setTableId(1);
        existingBill.setTotalAmount(10.00);
        existingBill.setSettled(false);
        when(billRepository.findTopByTableIdOrderByIssueTimeDesc(1)).thenReturn(Optional.of(existingBill));

        OrderItemRequest request = new OrderItemRequest(1L, 3); // adds €7.50

        // Act
        orderService.createOrder(1, Collections.singletonList(request));

        // Assert: the existing bill is reused and now reflects the cumulative total (€17.50),
        // instead of being replaced by a bill containing only the second order.
        ArgumentCaptor<Bill> billCaptor = ArgumentCaptor.forClass(Bill.class);
        verify(billRepository, times(1)).save(billCaptor.capture());
        Bill savedBill = billCaptor.getValue();
        assertSame(existingBill, savedBill, "Existing unsettled bill should be reused, not duplicated");
        assertEquals(17.50, savedBill.getTotalAmount(), 0.001);
    }
}
