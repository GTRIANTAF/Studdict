package com.studdict.controller;

import com.studdict.dto.CheckInRequestDTO;
import com.studdict.dto.CheckInResponseDTO;
import com.studdict.model.CheckIn;
import com.studdict.model.ReservationParticipant;
import com.studdict.service.CheckInService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/check-in")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping("/validate")
    public CheckInService.ReservationValidationResult validateReservation(
            @RequestBody ValidateCheckInRequest request) {
        return checkInService.validateReservation(
                request.getReservationId(),
                request.getQrData()
        );
    }

    @GetMapping("/participants")
    public List<ReservationParticipant> getParticipants(@RequestParam Long reservationId) {
        return checkInService.getParticipants(reservationId);
    }

    @PostMapping("/confirm")
    public CheckInResponseDTO checkInParticipants(@RequestBody ConfirmCheckInRequest request) {
        return checkInService.confirmCheckInParticipants(
                request.getReservationId(),
                request.getQrData(),
                request.getParticipantIds()
        );
    }

    @PostMapping("/perform")
    public CheckInResponseDTO performCheckIn(@RequestBody CheckInRequestDTO request) {
        return checkInService.performCheckIn(
                request.getReservationId(),
                request.getStudentId(),
                request.getQrData()
        );
    }

    @GetMapping("/message")
    public String getCheckInMessage(@RequestParam CheckInService.ReservationValidationResult result) {
        if (result == CheckInService.ReservationValidationResult.VALID_CHECK_IN) {
            return "Το check-in ολοκληρώθηκε επιτυχώς.";
        }

        if (result == CheckInService.ReservationValidationResult.WRONG_TABLE) {
            return "Το check-in απέτυχε. Το QR code δεν αντιστοιχεί στο τραπέζι της κράτησης.";
        }

        if (result == CheckInService.ReservationValidationResult.WRONG_TIME) {
            return "Το check-in απέτυχε. Η κράτηση δεν είναι ενεργή αυτή τη χρονική στιγμή.";
        }

        if (result == CheckInService.ReservationValidationResult.RESERVATION_NOT_FOUND) {
            return "Το check-in απέτυχε. Δεν βρέθηκε η κράτηση.";
        }

        return "Το check-in απέτυχε. Ελέγξτε το QR code ή τα στοιχεία της κράτησης.";
    }

    public static class ValidateCheckInRequest {

        private Long reservationId;
        private String qrData;

        public Long getReservationId() {
            return reservationId;
        }

        public void setReservationId(Long reservationId) {
            this.reservationId = reservationId;
        }

        public String getQrData() {
            return qrData;
        }

        public void setQrData(String qrData) {
            this.qrData = qrData;
        }
    }

    public static class ConfirmCheckInRequest {

        private Long reservationId;
        private String qrData;
        private List<Long> participantIds;

        public Long getReservationId() {
            return reservationId;
        }

        public void setReservationId(Long reservationId) {
            this.reservationId = reservationId;
        }

        public String getQrData() {
            return qrData;
        }

        public void setQrData(String qrData) {
            this.qrData = qrData;
        }

        public List<Long> getParticipantIds() {
            return participantIds;
        }

        public void setParticipantIds(List<Long> participantIds) {
            this.participantIds = participantIds;
        }
    }
}
