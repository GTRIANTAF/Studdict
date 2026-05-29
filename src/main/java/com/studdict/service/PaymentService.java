package com.studdict.service;

import com.studdict.model.Bill;
import com.studdict.model.CheckIn;
import com.studdict.model.Payment;
import com.studdict.repository.BillRepository;
import com.studdict.repository.CheckInRepository;
import com.studdict.repository.PaymentRepository;
import com.studdict.repository.StudyTableRepository;
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
    @Autowired private EBookService eBookService;
    @Autowired private GamificationService gamificationService;

    /**
     * UC6 - Πληρωμή (κάρτα ή μετρητά).
     * Εξοφλεί τον λογαριασμό και ολοκληρώνει το check-out:
     *  - καταγράφει το Payment,
     *  - απελευθερώνει το τραπέζι (UC6 βήμα 10),
     *  - ανακαλεί τις ενεργές άδειες e-book (UC7 βήμα 9).
     */
    public Bill processPayment(Long billId, String method, double amountGiven, String studentId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Το παραστατικό δεν βρέθηκε"));

        if (bill.isSettled()) {
            throw new RuntimeException("Ο λογαριασμός είναι ήδη εξοφλημένος");
        }

        bill.setPaidAmount(bill.getPaidAmount() + amountGiven);
        
        recordPayment(bill, method, amountGiven);

        if (bill.getPaidAmount() >= bill.getTotalAmount() - 0.01) {
            bill.setSettled(true);
            freeTable(bill);
            releaseEbookLoans(bill);
        }

        billRepository.save(bill);
        applyRewardPoints(bill, studentId);

        return bill;
    }

    public double calculateSplitAmount(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Το παραστατικό δεν βρέθηκε"));

        int numOfPeople = 1;
        if (bill.getReservationId() != null) {
            com.studdict.model.Reservation res = reservationRepository.findById(bill.getReservationId()).orElse(null);
            if (res != null && res.getNumberOfPeople() > 0) {
                numOfPeople = res.getNumberOfPeople();
            }
        }
        
        return bill.getTotalAmount() / numOfPeople;
    }

    // --- Βοηθητικές μέθοδοι ---

    private void recordPayment(Bill bill, String method, double amountGiven) {
        Payment payment = new Payment();
        payment.setReservationId(bill.getReservationId() != null ? String.valueOf(bill.getReservationId()) : null);
        payment.setAmountPaid(amountGiven);
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

    private void applyRewardPoints(Bill bill, String studentId) {
        if (studentId != null && !studentId.isEmpty() && bill.getReservationId() != null) {
            gamificationService.creditPointsForStudy(studentId, bill.getReservationId());
            System.out.println("-> [PAYMENT] Reward points applied for reservation " + bill.getReservationId());
        } else {
            System.out.println("-> [PAYMENT] Bill settled, but no points awarded (missing studentId or reservationId).");
        }
    }
}