package com.studdict.service;

import com.studdict.dto.OrderItemRequest;
import com.studdict.model.*;
import com.studdict.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private MenuItemRepository menuItemRepository;
    // Με το νέο domain model η Order συνδέεται απευθείας με το StudyTable (μέσω QR),
    // οπότε δεν χρειαζόμαστε πλέον το ReservationRepository.
    @Autowired private StudyTableRepository studyTableRepository;
    @Autowired private BillRepository billRepository;

    /**
     * UC8 - Παραγγελία F&B.
     * Η παραγγελία συσχετίζεται με το τραπέζι (StudyTable) που έχει ταυτοποιηθεί μέσω του QR code.
     *
     * @param tableId       το τραπέζι στο οποίο γίνεται η παραγγελία
     * @param requestedItems τα προϊόντα του "καλαθιού" με τις ποσότητές τους
     */
    /**
     * Υλοποιεί το LoadCatalogCtrl του Sequence Diagram
     * Ανακτά τον κατάλογο των προϊόντων
     */
    public List<MenuItem> readCatalog() {
        return menuItemRepository.findAll();
    }

    /**
     * Υλοποιεί το AddProductCtrl του Sequence Diagram
     * Προσθέτει ένα προϊόν στην παραγγελία
     */
    public void addProduct(List<OrderItemRequest> cart, Long menuItemId, int quantity) {
        OrderItemRequest request = new OrderItemRequest();
        request.setMenuItemId(menuItemId);
        request.setQuantity(quantity);
        cart.add(request);
    }

    public void processSummary(List<OrderItemRequest> requestedItems) {
        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new RuntimeException("Η παραγγελία είναι κενή.");
        }
    }

    public void verifyAvailability(List<OrderItemRequest> requestedItems) {
        for (OrderItemRequest req : requestedItems) {
            if (req.getMenuItemId() == null) {
                throw new RuntimeException("Λείπει το αναγνωριστικό προϊόντος.");
            }
            if (req.getQuantity() <= 0) {
                throw new RuntimeException("Η ποσότητα πρέπει να είναι μεγαλύτερη του μηδενός.");
            }
            MenuItem item = menuItemRepository.findById(req.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Το προϊόν δεν βρέθηκε."));
            if (!item.isAvailable()) {
                throw new RuntimeException("Το προϊόν " + item.getName() + " δεν είναι διαθέσιμο.");
            }
        }
    }

    public Order createOrder(int tableId, List<OrderItemRequest> requestedItems) {
        StudyTable table = studyTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Το τραπέζι δεν βρέθηκε."));

        // 3. Συνένωση τυχόν διπλότυπων γραμμών (ίδιο προϊόν -> άθροισμα ποσοτήτων)
        Map<Long, Integer> quantitiesByItem = new LinkedHashMap<>();
        for (OrderItemRequest req : requestedItems) {
            if (req.getMenuItemId() == null) {
                throw new RuntimeException("Λείπει το αναγνωριστικό προϊόντος.");
            }
            if (req.getQuantity() <= 0) {
                throw new RuntimeException("Η ποσότητα πρέπει να είναι μεγαλύτερη του μηδενός.");
            }
            quantitiesByItem.merge(req.getMenuItemId(), req.getQuantity(), Integer::sum);
        }

        // 4. Δημιουργία νέας παραγγελίας
        Order order = new Order();
        order.setPlacedAt(LocalDateTime.now());
        order.setStatus("PREPARING");
        order.setTable(table);

        // 5. Δημιουργία των γραμμών (OrderItems) με σωστή ποσότητα & subtotal
        for (Map.Entry<Long, Integer> entry : quantitiesByItem.entrySet()) {
            Long itemId = entry.getKey();
            int quantity = entry.getValue();

            MenuItem item = menuItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Το προϊόν δεν βρέθηκε."));

            double subTotal = item.getPrice() * quantity;

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(item);
            orderItem.setQuantity(quantity);
            orderItem.setSubTotal(subTotal);
            orderItem.setOrder(order);

            order.getItems().add(orderItem);
        }

        // Αρχική αποθήκευση για να πάρει ID
        order = orderRepository.save(order);

        // 6. Υπολογισμός κόστους (self-call όπως στο UC8)
        calculateCost(order);

        // 7. Προσθήκη κόστους στον λογαριασμό (Bill)
        Bill bill = new Bill();
        bill.setTableId(tableId);
        bill.setTotalAmount(order.getTotalAmount());
        bill.setIssueTime(LocalDateTime.now());
        billRepository.save(bill);

        // 8. Αποστολή παραγγελίας στην κουζίνα (Mock - KitchenSystem)
        System.out.println("[KitchenSystem] Ελήφθη νέα παραγγελία για το τραπέζι " + tableId + " με συνολικό κόστος: " + order.getTotalAmount() + "€");

        return order;
    }

    /**
     * Υλοποιεί το UpdateBillCtrl του Sequence Diagram
     * Υπολογίζει το συνολικό κόστος της παραγγελίας
     */
    public void calculateCost(Order order) {
        double totalAmount = order.getItems().stream()
                .mapToDouble(OrderItem::getSubTotal)
                .sum();
        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
    }
}