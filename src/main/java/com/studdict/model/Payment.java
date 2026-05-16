package com.studdict.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    private double amount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    // Η πληρωμή γίνεται από έναν φοιτητή [cite: 407]
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // Η πληρωμή αφορά έναν λογαριασμό (ή μέρος αυτού για split bill) [cite: 407]
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    public Payment() {}

    // Getters and Setters
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Bill getBill() { return bill; }
    public void setBill(Bill bill) { this.bill = bill; }
}