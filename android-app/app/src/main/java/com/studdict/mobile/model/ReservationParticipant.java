package com.studdict.mobile.model;

public class ReservationParticipant {

    private Long id;
    private String studentId;
    private Long reservationId;
    private String role;
    private boolean checkedIn;

    public Long getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getRole() {
        return role;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }
}
