package com.studdict.controller;

import com.studdict.model.InviteCode;
import com.studdict.model.Student;
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
                request.getGuest()
        );
    }

    @GetMapping("/message")
    public String getInviteCodeMessage(@RequestParam boolean success) {
        if (success) {
            return "Η συμμετοχή στην κράτηση ολοκληρώθηκε επιτυχώς.";
        }

        return "Ο κωδικός πρόσκλησης είναι λανθασμένος, ληγμένος ή η κράτηση είναι πλήρης.";
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
        private Student guest;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Student getGuest() {
            return guest;
        }

        public void setGuest(Student guest) {
            this.guest = guest;
        }
    }
}