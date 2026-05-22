package com.studdict.controller;

import com.studdict.service.GamificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gamification")
public class GamificationController {

    @Autowired private GamificationService gamificationService;

    @PostMapping("/earn")
    public String earnPoints(@RequestParam String studentId, @RequestParam int durationMinutes) {
        int points = gamificationService.creditPointsForStudy(studentId, durationMinutes);
        if (points > 0) {
            return "Επιτυχία! Κερδίσατε " + points + " πόντους.";
        }
        return "Η διάρκεια μελέτης δεν ήταν αρκετή για πόντους.";
    }

    @PostMapping("/redeem")
    public String redeemPoints(@RequestParam String studentId, @RequestParam int pointsToRedeem) {
        boolean success = gamificationService.redeemPoints(studentId, pointsToRedeem);
        if (success) {
            double discount = gamificationService.calculateDiscount(pointsToRedeem);
            return "Επιτυχής εξαργύρωση! Κερδίσατε έκπτωση " + discount + " ευρώ.";
        }
        return "Αποτυχία εξαργύρωσης. Ελέγξτε το υπόλοιπό σας.";
    }
}