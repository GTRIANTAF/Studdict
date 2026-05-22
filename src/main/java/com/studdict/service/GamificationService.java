package com.studdict.service;

import com.studdict.model.LoyaltyWallet;
import com.studdict.model.PointsTransaction;
import com.studdict.repository.LoyaltyWalletRepository;
import com.studdict.repository.PointsTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
public class GamificationService {

    @Autowired private LoyaltyWalletRepository walletRepository;
    @Autowired private PointsTransactionRepository transactionRepository;

    public int creditPointsForStudy(String studentId, int durationMinutes) {
        int pointsEarned = durationMinutes / 15;

        if (pointsEarned > 0) {
            LoyaltyWallet wallet = walletRepository.findById(studentId)
                    .orElseGet(() -> walletRepository.save(new LoyaltyWallet(studentId)));

            wallet.setTotalBalance(wallet.getTotalBalance() + pointsEarned);
            walletRepository.save(wallet);

            recordTransaction(studentId, pointsEarned, "EARN", "Μελέτη " + durationMinutes + " λεπτών");
        }
        return pointsEarned;
    }

    public boolean redeemPoints(String studentId, int pointsToRedeem) {
        LoyaltyWallet wallet = walletRepository.findById(studentId)
                .orElseGet(() -> walletRepository.save(new LoyaltyWallet(studentId)));

        if (wallet.getTotalBalance() >= pointsToRedeem && pointsToRedeem >= wallet.getMinimumRedeemLimit()) {
            wallet.setTotalBalance(wallet.getTotalBalance() - pointsToRedeem);
            walletRepository.save(wallet);

            recordTransaction(studentId, pointsToRedeem, "REDEEM", "Εξαργύρωση πόντων σε έκπτωση");
            return true;
        }
        return false;
    }

    public double calculateDiscount(int pointsToRedeem) {
        return pointsToRedeem * 0.05;
    }

    private void recordTransaction(String studentId, int points, String type, String desc) {
        PointsTransaction tx = new PointsTransaction();
        tx.setStudentId(studentId);
        tx.setPointsAmount(points);
        tx.setTransactionType(type);
        tx.setTimestamp(LocalDateTime.now());
        tx.setDescription(desc);
        transactionRepository.save(tx);
    }
}