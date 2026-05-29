package com.studdict.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
public class Bill {

    // Ο λογαριασμός έχει πλέον δικό του μοναδικό αναγνωριστικό (billId),
    // ώστε το payment layer να αναφέρεται σε αυτόν ανεξάρτητα από την κράτηση.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_id")
    private Long billId;

    // Σε ποια κράτηση αντιστοιχεί ο λογαριασμός (απλό πεδίο, χωρίς association).
    @Column(name = "reservation_id")
    private Long reservationId;

    // Σε ποιο τραπέζι αντιστοιχεί (χρήσιμο για απελευθέρωση τραπεζιού στο checkout/UC6).
    @Column(name = "table_id")
    private Integer tableId;

    @Column(name = "total_amount")
    private double totalAmount;

    // Συνολική έκπτωση (π.χ. από εξαργύρωση πόντων) που έχει εφαρμοστεί στον λογαριασμό.
    // Την κρατάμε ξεχωριστά ώστε, όταν προστεθεί νέα παραγγελία πριν το check-out, να
    // αφαιρείται από το μεικτό σύνολο αντί να "χάνεται". Το totalAmount = μεικτό - discountAmount.
    @Column(name = "discount_amount")
    private double discountAmount = 0.0;

    @Column(name = "issue_time")
    private LocalDateTime issueTime;

    @Column(name = "is_settled")
    private boolean isSettled = false;

    public Bill() {}

    // Getters & Setters
    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public LocalDateTime getIssueTime() { return issueTime; }
    public void setIssueTime(LocalDateTime issueTime) { this.issueTime = issueTime; }

    public boolean isSettled() { return isSettled; }
    public void setSettled(boolean settled) { isSettled = settled; }
}