package com.studdict.controller;

import com.studdict.model.CheckIn;
import com.studdict.model.Reservation;
import com.studdict.model.Student;
import com.studdict.service.CheckInService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/check-in")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping
    public CheckIn checkInStudent(@RequestBody CheckInRequest request) {
        return checkInService.checkInStudent(
                request.getReservation(),
                request.getStudent(),
                request.getScannedQrCode()
        );
    }

    @GetMapping("/message")
    public String getCheckInMessage(@RequestParam boolean successful) {
        if (successful) {
            return "Το check-in ολοκληρώθηκε επιτυχώς.";
        }

        return "Το check-in απέτυχε. Ελέγξτε το QR code ή τα στοιχεία της κράτησης.";
    }

    public static class CheckInRequest {

        private Reservation reservation;
        private Student student;
        private String scannedQrCode;

        public Reservation getReservation() {
            return reservation;
        }

        public void setReservation(Reservation reservation) {
            this.reservation = reservation;
        }

        public Student getStudent() {
            return student;
        }

        public void setStudent(Student student) {
            this.student = student;
        }

        public String getScannedQrCode() {
            return scannedQrCode;
        }

        public void setScannedQrCode(String scannedQrCode) {
            this.scannedQrCode = scannedQrCode;
        }
    }
}