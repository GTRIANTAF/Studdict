package com.studdict.controller;

import com.studdict.service.InviteCodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InviteCodeController {

    private final InviteCodeService inviteCodeService;

    public InviteCodeController(InviteCodeService inviteCodeService) {
        this.inviteCodeService = inviteCodeService;
    }

    @GetMapping("/invite/generate")
    public String generateInviteCode() {
        return inviteCodeService.generateInviteCode();
    }

    @GetMapping("/invite/validate")
    public boolean validateCode() {
        return inviteCodeService.validateCode("123456");
    }

    @GetMapping("/invite/join")
    public boolean joinReservation() {
        return inviteCodeService.joinReservation(1L, 2L);
    }

}