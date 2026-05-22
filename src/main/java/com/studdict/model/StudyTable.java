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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(name = "table_number", nullable = false)
    private int tableNumber;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "qr_code_string", unique = true, nullable = false)
    private String qrCodeString;

    @Column(name = "is_available")
    private boolean isAvailable = true;

    @Column(name = "soft_locked_by")
    private String softLockedBy;

    @Column(name = "soft_lock_expiration")
    private LocalTime softLockExpiration;

    public StudyTable() {
    }

    public StudyTable(Venue venue, int tableNumber, int capacity,
                      String qrCodeString, boolean isAvailable) {
        this.venue = venue;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.qrCodeString = qrCodeString;
        this.isAvailable = isAvailable;
    }

    public int getId() {
        return tableId;
    }

    public int getTableId() {
        return tableId;
    }

    public void setId(int tableId) {
        this.tableId = tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getQrCodeString() {
        return qrCodeString;
    }

    public void setQrCodeString(String qrCodeString) {
        this.qrCodeString = qrCodeString;
    }

    public boolean getIsAvailable() {
        return isAvailable;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public String getSoftLockedBy() {
        return softLockedBy;
    }

    public void setSoftLockedBy(String studentId) {
        this.softLockedBy = studentId;
    }

    public LocalTime getSoftLockExpiration() {
        return softLockExpiration;
    }

    public void setSoftLockExpiration(LocalTime expiration) {
        this.softLockExpiration = expiration;
    }
}