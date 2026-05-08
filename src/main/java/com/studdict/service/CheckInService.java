package com.studdict.service;

import org.springframework.stereotype.Service;

@Service
public class CheckInService {

    public boolean validateCheckIn(Long reservationId, Long tableId) {

        return true;
    }

    public boolean createCheckIn(Long reservationId) {

        return true;
    }

    public boolean activateServices(Long participantId) {

        return true;
    }
}