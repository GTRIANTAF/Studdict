package com.studdict.controller;

import com.studdict.model.InviteCode;
import com.studdict.service.InviteCodeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invite-code")
public class InviteCodeController {

    private final InviteCodeService inviteCodeService;

    public InviteCodeController(InviteCodeService inviteCodeService) {
        this.inviteCodeService = inviteCodeService;
    }

    @PostMapping("/generate")
    public InviteCode generateInviteCode(@RequestBody GenerateInviteCodeRequest request) {
        return inviteCodeService.generateInviteCode(
                request.getReservationId(),
                request.getHostId()
        );
    }

    @PostMapping("/validate")
    public boolean validateCode(@RequestBody ValidateCodeRequest request) {
        return inviteCodeService.validateCode(request.getCode());
    }

    @PostMapping("/join")
    public boolean joinReservation(@RequestBody JoinReservationRequest request) {
        return inviteCodeService.joinReservation(
                request.getCode(),
                request.getGuestId()
        );
    }

    @PostMapping("/join-result")
    public JoinReservationResponse joinReservationWithResult(@RequestBody JoinReservationRequest request) {
        InviteCodeService.JoinReservationResult result =
                inviteCodeService.joinReservationWithResult(
                        request.getCode(),
                        request.getGuestId()
                );

        return new JoinReservationResponse(
                result.name(),
                getJoinMessage(result)
        );
    }

    @GetMapping("/message")
    public String getInviteCodeMessage(@RequestParam boolean success) {
        if (success) {
            return "Η συμμετοχή στην κράτηση ολοκληρώθηκε επιτυχώς.";
        }

        return "Ο κωδικός πρόσκλησης είναι λανθασμένος, ληγμένος ή η κράτηση είναι πλήρης.";
    }

    private String getJoinMessage(InviteCodeService.JoinReservationResult result) {
        if (result == InviteCodeService.JoinReservationResult.SUCCESS) {
            return "Η συμμετοχή στην κράτηση ολοκληρώθηκε επιτυχώς.";
        }

        if (result == InviteCodeService.JoinReservationResult.RESERVATION_FULL) {
            return "Η συγκεκριμένη κράτηση είναι ήδη πλήρης.";
        }

        if (result == InviteCodeService.JoinReservationResult.ALREADY_PARTICIPANT) {
            return "Ο φοιτητής συμμετέχει ήδη σε αυτή την κράτηση.";
        }

        if (result == InviteCodeService.JoinReservationResult.STUDENT_NOT_FOUND) {
            return "Δεν βρέθηκε ο φοιτητής.";
        }

        if (result == InviteCodeService.JoinReservationResult.RESERVATION_NOT_FOUND) {
            return "Δεν βρέθηκε η κράτηση που αντιστοιχεί στον κωδικό.";
        }

        return "Ο κωδικός πρόσκλησης είναι λανθασμένος ή ληγμένος.";
    }

    public static class GenerateInviteCodeRequest {

        private Long reservationId;
        private String hostId;

        public Long getReservationId() {
            return reservationId;
        }

        public void setReservationId(Long reservationId) {
            this.reservationId = reservationId;
        }

        public String getHostId() {
            return hostId;
        }

        public void setHostId(String hostId) {
            this.hostId = hostId;
        }
    }

    public static class ValidateCodeRequest {

        private String code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    public static class JoinReservationRequest {

        private String code;
        private String guestId;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getGuestId() {
            return guestId;
        }

        public void setGuestId(String guestId) {
            this.guestId = guestId;
        }
    }

    public static class JoinReservationResponse {

        private String status;
        private String message;

        public JoinReservationResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}