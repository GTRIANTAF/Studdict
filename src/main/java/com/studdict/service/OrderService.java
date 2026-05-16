package com.studdict.service;

import com.studdict.model.*;
import com.studdict.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private ReservationRepository reservationRepository;

    public Order createOrder(Long reservationId, List<Long> menuItemIds) {
        // 1. Εύρεση κράτησης (από την οποία θα βρούμε το τραπέζι)
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Η κράτηση δεν βρέθηκε."));

        // 2. Δημιουργία νέας παραγγελίας [cite: 266]
        Order order = new Order();
        order.setPlacedAt(LocalDateTime.now());
        order.setStatus("PREPARING"); // Αποστολή στην κουζίνα [cite: 267]
        order.setTable(reservation.getTable());

        double totalAmount = 0.0;

        // 3. Προσθήκη προϊόντων (Items)
        for (Long itemId : menuItemIds) {
            MenuItem item = menuItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Το προϊόν δεν βρέθηκε."));

            if (!item.isAvailable()) {
                throw new RuntimeException("Το προϊόν " + item.getName() + " δεν είναι διαθέσιμο.");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(item);
            orderItem.setQuantity(1); // Για απλότητα, προσθέτουμε 1 τεμάχιο κάθε φορά
            orderItem.setSubTotal(item.getPrice());
            orderItem.setOrder(order);

            order.getItems().add(orderItem);
            totalAmount += item.getPrice();
        }

        order.setTotalAmount(totalAmount);

        // 4. Αποθήκευση παραγγελίας [cite: 266]
        return orderRepository.save(order);
    }
}