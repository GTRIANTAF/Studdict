package com.studdict.controller;

import com.studdict.service.GamificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gamification")
public class GamificationController {

    @Autowired private GamificationService gamificationService;

    @GetMapping("/wallet/{studentId}")
    public com.studdict.model.LoyaltyWallet getWallet(@PathVariable String studentId) {
        return gamificationService.getWallet(studentId);
    }

    @PostMapping("/validate")
    public boolean validatePoints(@RequestParam String studentId, @RequestParam int points) {
        com.studdict.model.LoyaltyWallet wallet = gamificationService.getWallet(studentId);
        return wallet.getTotalBalance() >= points && points >= wallet.getMinimumRedeemLimit();
    }

    @PostMapping("/earn")
    public String earnPoints(@RequestParam String studentId, @RequestParam Long reservationId) {
        int points = gamificationService.creditPointsForStudy(studentId, reservationId);
        if (points > 0) {
            return "Επιτυχία! Κερδίσατε " + points + " πόντους.";
        }
        return "Η διάρκεια μελέτης δεν ήταν αρκετή για πόντους.";
    }

    @PostMapping("/redeem")
    public String redeemPoints(
            @RequestParam String studentId,
            @RequestParam int pointsToRedeem,
            @RequestParam(required = false) Integer tableId) {
        boolean success = gamificationService.redeemPoints(studentId, pointsToRedeem);
        if (success) {
            double discount = gamificationService.calculateDiscount(pointsToRedeem);
            if (tableId != null) {
                gamificationService.applyDiscountToBill(tableId, discount);
            }
            return "Επιτυχής εξαργύρωση! Κερδίσατε έκπτωση " + discount + " ευρώ.";
        }
        return "Αποτυχία εξαργύρωσης. Ελέγξτε το υπόλοιπό σας.";
    }
}