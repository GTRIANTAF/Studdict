package com.studdict.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reservation_participants")
public class ReservationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "role")
    private String role; // "Host" or "Guest"

    @Column(name = "checked_in")
    private boolean checkedIn = false;

    public ReservationParticipant() {
    }

    public ReservationParticipant(Long id, String studentId, Long reservationId, String role, boolean checkedIn) {
        this.id = id;
        this.studentId = studentId;
        this.reservationId = reservationId;
        this.role = role;
        this.checkedIn = checkedIn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }
}