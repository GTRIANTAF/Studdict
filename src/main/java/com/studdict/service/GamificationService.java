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

    @Autowired
    private LoyaltyWalletRepository walletRepository;

    @Autowired
    private PointsTransactionRepository transactionRepository;

    // ==========================================
    // UC 9: ΑΠΟΚΤΗΣΗ ΠΟΝΤΩΝ (Earn Points)
    // ==========================================
    public int creditPointsForStudy(String studentId, int durationMinutes) {
        int pointsEarned = durationMinutes / 15; // 1 πόντος ανά 15 λεπτά μελέτης

        if (pointsEarned > 0) {
            LoyaltyWallet wallet = getWallet(studentId);
            wallet.setTotalBalance(wallet.getTotalBalance() + pointsEarned);
            walletRepository.save(wallet);

            recordTransaction(studentId, pointsEarned, "EARN", "Μελέτη " + durationMinutes + " λεπτών");
        }
        return pointsEarned;
    }

    // ==========================================
    // UC 10: ΕΞΑΡΓΥΡΩΣΗ ΠΟΝΤΩΝ (Redeem Points)
    // ==========================================
    public boolean redeemPoints(String studentId, int pointsToRedeem) {
        LoyaltyWallet wallet = getWallet(studentId);

        // Έλεγχος αν επαρκούν οι πόντοι και αν ξεπερνούν το ελάχιστο όριο
        if (wallet.getTotalBalance() >= pointsToRedeem && pointsToRedeem >= wallet.getMinimumRedeemLimit()) {
            wallet.setTotalBalance(wallet.getTotalBalance() - pointsToRedeem);
            walletRepository.save(wallet);

            recordTransaction(studentId, pointsToRedeem, "REDEEM", "Εξαργύρωση πόντων σε έκπτωση");
            return true;
        }
        return false;
    }

    public double calculateDiscount(int pointsToRedeem) {
        LoyaltyWallet temp = new LoyaltyWallet(); // Απλή κλήση για να πάρουμε το exchange rate
        return (pointsToRedeem / 100.0) * temp.getExchangeRate();
    }

    // ==========================================
    // ΒΟΗΘΗΤΙΚΕΣ ΜΕΘΟΔΟΙ (Internal Logic)
    // ==========================================
    private LoyaltyWallet getWallet(String studentId) {
        // Αν ο φοιτητής δεν έχει πορτοφόλι, του φτιάχνει ένα αυτόματα
        return walletRepository.findById(studentId).orElseGet(() -> {
            LoyaltyWallet newWallet = new LoyaltyWallet(studentId);
            return walletRepository.save(newWallet);
        });
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