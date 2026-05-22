package com.studdict.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_ins")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_in_id")
    private Long checkInId;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    private StudyTable table;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "scanned_qr_code", nullable = false)
    private String scannedQrCode;

    @Column(name = "successful")
    private boolean successful;

    public CheckIn() {
    }

    public CheckIn(Long checkInId, Reservation reservation, StudyTable table, Student student,
                   LocalDateTime checkInTime, String scannedQrCode, boolean successful) {
        this.checkInId = checkInId;
        this.reservation = reservation;
        this.table = table;
        this.student = student;
        this.checkInTime = checkInTime;
        this.scannedQrCode = scannedQrCode;
        this.successful = successful;
    }

    public Long getCheckInId() {
        return checkInId;
    }

    public void setCheckInId(Long checkInId) {
        this.checkInId = checkInId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public StudyTable getTable() {
        return table;
    }

    public void setTable(StudyTable table) {
        this.table = table;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getScannedQrCode() {
        return scannedQrCode;
    }

    public void setScannedQrCode(String scannedQrCode) {
        this.scannedQrCode = scannedQrCode;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}