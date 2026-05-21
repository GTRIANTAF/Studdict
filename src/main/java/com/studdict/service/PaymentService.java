package com.studdict.service;

import com.studdict.model.Bill;
import com.studdict.model.Payment;
import com.studdict.repository.BillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentService {
    private final BillRepository billRepository;

    public PaymentService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public Bill processPayment(Long billId, String method, double amountGiven) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Ο λογαριασμός δεν βρέθηκε"));

        if (bill.isSettled()) {
            throw new RuntimeException("Ο λογαριασμός έχει ήδη εξοφληθεί");
        }
        if (amountGiven < bill.getTotalAmount()) {
            throw new RuntimeException("Το ποσό δεν επαρκεί υπολείπονται"+ (bill.getTotalAmount()-amountGiven)+"€");
        }

        bill.setSettled(true);
        billRepository.save(bill);

        applyRewardPoints(bill);

        return bill;
    }

    public double calculateSplitAmount(Long billId, int numOfPeople) {
        if (numOfPeople <= 0) {
            throw new RuntimeException("Ο αριθμός των ατόμων πρέπει να είναι τουλάχιστον 1");
        }

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("Ο λογαριασμός δεν βρέθηκε"));

        return bill.getTotalAmount()/numOfPeople;
    }

    public void applyRewardPoints(Bill bill) {
        System.out.println("🎉 [REWARD] Δόθηκαν " + (bill.getTotalAmount() * 10) + " πόντοι επιβράβευσης για την κράτηση " + bill.getReservation().getReservationId());
    }
}
