package com.studdict.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
public class Bill {
    @Id
    private Long reservationId; // Έγινε Long για να ταιριάζει με το Reservation.java

    private double totalAmount;
    private LocalDateTime issueTime;
    private boolean isSettled = false;

    public Bill() {}

    // Getters & Setters
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getIssueTime() { return issueTime; }
    public void setIssueTime(LocalDateTime issueTime) { this.issueTime = issueTime; }

    public boolean isSettled() { return isSettled; }
    public void setSettled(boolean settled) { isSettled = settled; }
}