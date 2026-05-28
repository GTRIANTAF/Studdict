package com.studdict.mobile.model;

import java.util.List;

public class ConfirmCheckInRequest {

    private Long reservationId;
    private String qrData;
    private List<Long> participantIds;

    public ConfirmCheckInRequest(Long reservationId, String qrData, List<Long> participantIds) {
        this.reservationId = reservationId;
        this.qrData = qrData;
        this.participantIds = participantIds;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getQrData() {
        return qrData;
    }

    public List<Long> getParticipantIds() {
        return participantIds;
    }
}
