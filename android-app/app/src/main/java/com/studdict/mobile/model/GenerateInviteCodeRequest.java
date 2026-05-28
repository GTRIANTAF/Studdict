package com.studdict.mobile.model;

public class GenerateInviteCodeRequest {

    private Long reservationId;
    private String hostId;

    public GenerateInviteCodeRequest(Long reservationId, String hostId) {
        this.reservationId = reservationId;
        this.hostId = hostId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getHostId() {
        return hostId;
    }
}