package com.studdict.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reservation_participants")
public class ReservationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentId;
    private Long reservationId;
    private String role; // "Host" or "Guest"
    private boolean checkedIn = false; // Set to true during UC5[cite: 1]

    public ReservationParticipant() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isCheckedIn() { return checkedIn; }
    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }
}