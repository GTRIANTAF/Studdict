package com.studdict.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "study_tables")
public class StudyTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private int tableId;

    // ManyToOne relationship connects this table to a specific Venue
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Venue venue;

    @Column(name = "table_number")
    private int tableNumber;

    @Column(name = "capacity")
    private int capacity;

    @Column(name = "qr_code_string")
    private String qrCodeString;

    @Column(name = "is_available")
    private boolean isAvailable = true;

    // --- Soft Lock Fields for UC1 Concurrency ---
    @Column(name = "soft_locked_by")
    private String softLockedBy;

    @Column(name = "soft_lock_expiration")
    private LocalTime softLockExpiration;

    public StudyTable() {}

    public StudyTable(Venue venue, Integer tableNumber, Integer capacity,
                      String qrCodeString, Boolean isAvailable) {
        this.venue = venue;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.qrCodeString = qrCodeString;
        this.isAvailable = isAvailable;
    }

    // Getters
    public int getId() {
        return tableId;
    }

    public Venue getVenue() {
        return venue;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getQrCodeString() {
        return qrCodeString;
    }

    public boolean getIsAvailable() {
        return isAvailable;
    }

    public String getSoftLockedBy() {
        return softLockedBy;
    }

    public LocalTime getSoftLockExpiration() {
        return softLockExpiration;
    }

    // Setters
    public void setId(int tableId) {
        this.tableId = tableId;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setQrCodeString(String qrCodeString) {
        this.qrCodeString = qrCodeString;
    }

    public void setIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public void setSoftLockedBy(String studentId) {
        this.softLockedBy = studentId;
    }

    public void setSoftLockExpiration(LocalTime expiration) {
        this.softLockExpiration = expiration;
    }
}