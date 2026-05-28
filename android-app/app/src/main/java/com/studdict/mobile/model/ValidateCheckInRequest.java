package com.studdict.mobile.model;

public class ValidateCheckInRequest {

    private Long reservationId;
    private String qrData;

    public ValidateCheckInRequest(Long reservationId, String qrData) {
        this.reservationId = reservationId;
        this.qrData = qrData;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getQrData() {
        return qrData;
    }
}
