package com.studdict.controller;

import com.studdict.service.ReservationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cancellation")
public class CancellationController {
    private final ReservationService reservationService;

    public CancellationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/{reservationId}")
    public CancellationResponse cancelReservation(@PathVariable Long reservationId) {
        try {
            reservationService.discardReservation(reservationId);
            return new CancellationResponse(true, "Η κράτηση ακυρώθηκε επιτυχώς");
        } catch (RuntimeException e) {
            return new CancellationResponse(false, "Σφάλμα: "+ e.getMessage());
        }
    }

    public static class CancellationResponse {
        private boolean success;
        private String message;

        public CancellationResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
