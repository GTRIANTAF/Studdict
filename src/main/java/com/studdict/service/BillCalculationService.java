package com.studdict.service;

import com.studdict.model.Bill;
import com.studdict.model.Order;
import com.studdict.model.Reservation;
import com.studdict.repository.BillRepository;
import com.studdict.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BillCalculationService {

    private final ReservationRepository reservationRepository;
    private final BillRepository billRepository;

    public BillCalculationService(ReservationRepository reservationRepository, BillRepository billRepository) {
        this.reservationRepository = reservationRepository;
        this.billRepository = billRepository;
    }

    public Bill generateBillForReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Η κράτηση με ID " + reservationId + " δεν βρέθηκε!"));

        if (reservation.getBill() != null) {
            return reservation.getBill();
        }

        double totalAmount = 0.0;
        List<Order> orders = reservation.getOrders();

        if (orders != null && !orders.isEmpty()) {
            for (Order order : orders) {
                if (order.getTotalAmount() != null) {
                    totalAmount += order.getTotalAmount();
                }
            }
        }

        Bill bill = new Bill();
        bill.setTotalAmount(totalAmount);
        bill.setIssueTime(LocalDateTime.now());
        bill.setSettled(false);

        bill.setReservation(reservation);
        reservation.setBill(bill);

        return billRepository.save(bill);
    }
}