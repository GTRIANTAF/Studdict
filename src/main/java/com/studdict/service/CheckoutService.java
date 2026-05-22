package com.studdict.service;

import com.studdict.model.Bill;
import com.studdict.model.Order;
import com.studdict.model.Reservation;
import com.studdict.model.StudyTable;
import com.studdict.repository.BillRepository;
import com.studdict.repository.OrderRepository;
import com.studdict.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CheckoutService {

    private final ReservationRepository reservationRepository;
    private final BillRepository billRepository;
    private final OrderRepository orderRepository;

    public CheckoutService(ReservationRepository reservationRepository,
                           BillRepository billRepository,
                           OrderRepository orderRepository) {
        this.reservationRepository = reservationRepository;
        this.billRepository = billRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * UC6 - Check-out: δημιουργεί τον λογαριασμό μιας κράτησης.
     * Με το νέο domain model οι παραγγελίες (Order) συνδέονται με το StudyTable,
     * οπότε αθροίζουμε τις παραγγελίες του τραπεζιού της κράτησης.
     */
    public Bill generateBillForReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Η κράτηση με ID " + reservationId + " δεν βρέθηκε!"));

        // Idempotency: αν υπάρχει ήδη λογαριασμός για την κράτηση, τον επιστρέφουμε.
        Optional<Bill> existingBill = billRepository.findByReservationId(reservationId);
        if (existingBill.isPresent()) {
            return existingBill.get();
        }

        StudyTable table = reservation.getTable();
        Integer tableId = (table != null) ? table.getId() : null;

        double totalAmount = 0.0;
        if (tableId != null) {
            List<Order> orders = orderRepository.findByTableId(tableId);
            for (Order order : orders) {
                totalAmount += order.getTotalAmount();
            }
        }

        Bill bill = new Bill();
        bill.setReservationId(reservationId);
        bill.setTableId(tableId);
        bill.setTotalAmount(totalAmount);
        bill.setIssueTime(LocalDateTime.now());
        bill.setSettled(false);

        return billRepository.save(bill);
    }
}