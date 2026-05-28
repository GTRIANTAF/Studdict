package com.studdict.mobile.model;

public class CheckInRequest {

    private Long reservationId;
    private String studentId;
    private String qrData;

    public CheckInRequest(Long reservationId, String studentId, String qrData) {
        this.reservationId = reservationId;
        this.studentId = studentId;
        this.qrData = qrData;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getQrData() {
        return qrData;
    }
}
