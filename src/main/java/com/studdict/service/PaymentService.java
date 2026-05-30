package com.studdict.service;

import com.studdict.model.Bill;
import com.studdict.model.CheckIn;
import com.studdict.model.Payment;
import com.studdict.repository.BillRepository;
import com.studdict.repository.CheckInRepository;
import com.studdict.repository.PaymentRepository;
import com.studdict.repository.StudyTableRepository;
import com.studdict.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PaymentService {

    @Autowired private BillRepository billRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private StudyTableRepository studyTableRepository;
    @Autowired private CheckInRepository checkInRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private EBookService eBookService;

    /**
     * UC6 - Πληρωμή (κάρτα ή μετρητά).
     * Εξοφλεί τον λογαριασμό και ολοκληρώνει το check-out:
     *  - καταγράφει το Payment,
     *  - απελευθερώνει το τραπέζι (UC6 βήμα 10),
     *  - ανακαλεί τις ενεργές άδειες e-book (UC7 βήμα 9).
     */
    public Bill processPayment(Long billId, String method, double amountGiven) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Ο λογαριασμός δεν βρέθηκε"));

        if (bill.isSettled()) {
            throw new RuntimeException("Ο λογαριασμός έχει ήδη εξοφληθεί");
        }
        if (amountGiven < bill.getTotalAmount()) {
            throw new RuntimeException("Το ποσό δεν επαρκεί, υπολείπονται " + (bill.getTotalAmount() - amountGiven) + "€");
        }

        bill.setSettled(true);
        billRepository.save(bill);

        recordPayment(bill, method);
        freeTable(bill);
        releaseEbookLoans(bill);
        removeReservation(bill);
        applyRewardPoints(bill);

        return bill;
    }

    public double calculateSplitAmount(Long billId, int numOfPeople) {
        if (numOfPeople <= 0) {
            throw new RuntimeException("Ο αριθμός των ατόμων πρέπει να είναι τουλάχιστον 1");
        }

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Ο λογαριασμός δεν βρέθηκε"));

        return bill.getTotalAmount() / numOfPeople;
    }

    // --- Βοηθητικές μέθοδοι ---

    private void recordPayment(Bill bill, String method) {
        Payment payment = new Payment();
        payment.setReservationId(bill.getReservationId() != null ? String.valueOf(bill.getReservationId()) : null);
        payment.setAmountPaid(bill.getTotalAmount());
        payment.setPaymentMethod(method);
        payment.setTimestamp(LocalDateTime.now());
        payment.setStatus("COMPLETED");
        paymentRepository.save(payment);
    }

    private void freeTable(Bill bill) {
        if (bill.getTableId() == null) {
            return;
        }
        studyTableRepository.findById(bill.getTableId()).ifPresent(table -> {
            table.setIsAvailable(true);
            studyTableRepository.save(table);
        });
    }

    private void releaseEbookLoans(Bill bill) {
        if (bill.getReservationId() == null) {
            return;
        }
        List<CheckIn> checkIns = checkInRepository.findByReservation_ReservationId(bill.getReservationId());
        for (CheckIn checkIn : checkIns) {
            eBookService.revokeLoan(checkIn.getCheckInId());
        }
    }

    private void removeReservation(Bill bill) {
        if (bill.getReservationId() != null) {
            reservationRepository.findById(bill.getReservationId()).ifPresent(reservation -> {
                reservation.setStatus("COMPLETED");
                reservationRepository.save(reservation);
            });
        }
    }

    private void applyRewardPoints(Bill bill) {
        // Οι πόντοι επιβράβευσης αποδίδονται μέσω του UC9 (GamificationService),
        // εδώ απλώς καταγράφουμε ότι ο λογαριασμός εξοφλήθηκε.
        System.out.println("✅ [PAYMENT] Ο λογαριασμός της κράτησης " + bill.getReservationId() + " εξοφλήθηκε.");
    }
}