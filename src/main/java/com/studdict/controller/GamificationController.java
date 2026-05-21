package com.studdict.controller;

import com.studdict.service.GamificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gamification")
public class GamificationController {

    @Autowired
    private GamificationService gamificationService;

    // ==========================================
    // UC 9 Endpoint: Απονομή Πόντων
    // ==========================================
    @PostMapping("/earn")
    public String earnPoints(@RequestParam String studentId, @RequestParam int durationMinutes) {
        int points = gamificationService.creditPointsForStudy(studentId, durationMinutes);

        if (points > 0) {
            return "Επιτυχία! Κερδίσατε " + points + " πόντους για " + durationMinutes + " λεπτά μελέτης.";
        }
        return "Η διάρκεια μελέτης δεν ήταν αρκετή για να κερδίσετε πόντους.";
    }

    // ==========================================
    // UC 10 Endpoint: Εξαργύρωση Πόντων
    // ==========================================
    @PostMapping("/redeem")
    public String redeemPoints(@RequestParam String studentId, @RequestParam int pointsToRedeem) {
        boolean success = gamificationService.redeemPoints(studentId, pointsToRedeem);

        if (success) {
            double discount = gamificationService.calculateDiscount(pointsToRedeem);
            return "Επιτυχής εξαργύρωση! Κερδίσατε έκπτωση " + discount + " ευρώ.";
        }
        return "Αποτυχία εξαργύρωσης. Ελέγξτε το υπόλοιπό σας ή το ελάχιστο όριο εξαργύρωσης.";
    }
}