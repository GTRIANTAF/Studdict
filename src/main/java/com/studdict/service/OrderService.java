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
    @Autowired private StudyTableRepository studyTableRepository;
    @Autowired private BillRepository billRepository;
    @Autowired private ReservationRepository reservationRepository;

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

    /**
     * Υλοποιεί την ακύρωση της παραγγελίας από τον χρήστη (προστέθηκε στο Sequence Diagram).
     */
    public void cancelOrder() {
        // Stateless REST: no server-side cart to clear. Frontend handles navigation.
    }

    // UC8: validates a single item before it is added to the client cart (addProduct step)
    public boolean addCartItem(Long menuItemId, int quantity) {
        if (menuItemId == null || quantity <= 0) return false;
        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Το προϊόν δεν βρέθηκε."));
        if (!item.isAvailable()) throw new RuntimeException("Το προϊόν δεν είναι διαθέσιμο.");
        return true;
    }

    // UC8: returns all orders currently being prepared — consumed by KitchenScreen
    public List<Order> getActiveOrders() {
        return orderRepository.findByStatus("PREPARING");
    }

    public List<String> getOrderItemsAsStrings(int tableId) {
        return orderRepository.findByTableId(tableId).stream()
                .flatMap(order -> order.getItems().stream())
                .map(item -> "• " + item.getMenuItem().getName() + " x" + item.getQuantity() + " (€" + String.format(java.util.Locale.US, "%.2f", item.getSubTotal()) + ")")
                .collect(java.util.stream.Collectors.toList());
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

        // 7. Ενημέρωση του (ενιαίου) λογαριασμού του τραπεζιού.
        //    Αν υπάρχει ήδη ανεξόφλητος λογαριασμός για το τραπέζι (π.χ. προηγούμενη
        //    παραγγελία στην ίδια κράτηση, χωρίς να έχει γίνει check-out), τον
        //    ενημερώνουμε ώστε η νέα παραγγελία να ΠΡΟΣΤΕΘΕΙ στις προηγούμενες, αντί
        //    να δημιουργηθεί νέος λογαριασμός που "ξεχνάει" τις προηγούμενες παραγγελίες.
        //    Το σύνολο υπολογίζεται πάντα ως το άθροισμα ΟΛΩΝ των παραγγελιών του τραπεζιού.
        double grossTotal = orderRepository.findByTableId(tableId).stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        Bill bill = billRepository.findTopByTableIdOrderByIssueTimeDesc(tableId)
                .filter(existing -> !existing.isSettled())
                .orElseGet(Bill::new);
        bill.setTableId(tableId);
        
        // Link the active reservation to the bill so checkout can properly "complete" the reservation
        List<Reservation> activeReservations = reservationRepository.findByTable_TableIdAndStatus(tableId, "CONFIRMED");
        if (!activeReservations.isEmpty()) {
            bill.setReservationId(activeReservations.get(0).getReservationId());
        }

        //    Διατηρούμε τυχόν έκπτωση που έχει ήδη εφαρμοστεί (π.χ. εξαργύρωση πόντων πριν
        //    από νέα παραγγελία): το νέο σύνολο είναι το μεικτό άθροισμα ΜΕΙΟΝ την έκπτωση,
        //    ώστε η νέα παραγγελία να προστίθεται στο ήδη εκπτωμένο ποσό αντί να μηδενίζει
        //    την έκπτωση. Για νέο λογαριασμό η έκπτωση είναι 0, οπότε σύνολο = μεικτό.
        bill.setTotalAmount(Math.max(0.0, grossTotal - bill.getDiscountAmount()));
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