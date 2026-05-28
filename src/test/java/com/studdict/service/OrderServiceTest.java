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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
}
