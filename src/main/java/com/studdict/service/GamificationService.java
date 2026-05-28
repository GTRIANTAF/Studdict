package com.studdict.service;

import com.studdict.model.LoyaltyWallet;
import com.studdict.model.PointsTransaction;
import com.studdict.repository.LoyaltyWalletRepository;
import com.studdict.repository.PointsTransactionRepository;
import com.studdict.repository.CheckInRepository;
import com.studdict.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@Transactional
public class GamificationService {

    @Autowired private LoyaltyWalletRepository walletRepository;
    @Autowired private PointsTransactionRepository transactionRepository;
    @Autowired private CheckInRepository checkInRepository;
    @Autowired private ReservationRepository reservationRepository;

    public LoyaltyWallet getWallet(String studentId) {
        return walletRepository.findById(studentId)
                .orElseGet(() -> walletRepository.save(new LoyaltyWallet(studentId)));
    }

    public int creditPointsForStudy(String studentId, Long reservationId) {
        java.util.List<com.studdict.model.CheckIn> checkIns = checkInRepository.findByReservation_ReservationId(reservationId);
        boolean hasValidCheckIn = false;
        for (com.studdict.model.CheckIn c : checkIns) {
            if (c.isSuccessful() && c.getStudent().getStudentId().equals(studentId)) {
                hasValidCheckIn = true;
                break;
            }
        }
        if (!hasValidCheckIn) {
            throw new RuntimeException("No successful check-in found for this reservation. Process terminated. No points awarded.");
        }

        com.studdict.model.Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found."));

        int durationMinutes = res.getDurationMinutes();
        int pointsEarned = durationMinutes / 15;

        if (pointsEarned > 0) {
            LoyaltyWallet wallet = walletRepository.findById(studentId)
                    .orElseGet(() -> walletRepository.save(new LoyaltyWallet(studentId)));

            wallet.setTotalBalance(wallet.getTotalBalance() + pointsEarned);
            walletRepository.save(wallet);

            recordTransaction(studentId, pointsEarned, "EARN", "Μελέτη " + durationMinutes + " λεπτών στην κράτηση #" + reservationId);
        }
        return pointsEarned;
    }

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