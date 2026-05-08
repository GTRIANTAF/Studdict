package com.studdict.service;

import org.springframework.stereotype.Service;

@Service
public class InviteCodeService {

    public String generateInviteCode() {

        return "123456";
    }

    public boolean validateCode(String code) {

        return true;
    }

    public boolean joinReservation(Long reservationId, Long participantId) {

        return true;
    }
}