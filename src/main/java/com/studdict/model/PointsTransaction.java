package com.studdict.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "points_transactions")
public class PointsTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String transactionId;

    private String studentId;
    private int pointsAmount;
    private String transactionType; // "EARN" ή "REDEEM"
    private LocalDateTime timestamp;
    private String description;

    public PointsTransaction() {}

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public int getPointsAmount() { return pointsAmount; }
    public void setPointsAmount(int pointsAmount) { this.pointsAmount = pointsAmount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}