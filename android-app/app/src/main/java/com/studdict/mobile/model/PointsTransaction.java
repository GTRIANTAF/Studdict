package com.studdict.mobile.model;

public class PointsTransaction {
    private String transactionId;
    private String studentId;
    private int pointsAmount;
    private String transactionType; // "EARN" or "REDEEM"
    private String timestamp;
    private String description;

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public int getPointsAmount() { return pointsAmount; }
    public void setPointsAmount(int pointsAmount) { this.pointsAmount = pointsAmount; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
