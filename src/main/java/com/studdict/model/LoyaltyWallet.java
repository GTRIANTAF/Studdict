package com.studdict.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "loyalty_wallets")
public class LoyaltyWallet {
    @Id
    private String studentId;
    private int totalBalance = 0;
    private int minimumRedeemLimit = 100;
    private double exchangeRate = 0.05; // 1 πόντος = 0.05 ευρώ

    public LoyaltyWallet() {}

    public LoyaltyWallet(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public int getTotalBalance() { return totalBalance; }
    public void setTotalBalance(int totalBalance) { this.totalBalance = totalBalance; }

    public int getMinimumRedeemLimit() { return minimumRedeemLimit; }
    public void setMinimumRedeemLimit(int minimumRedeemLimit) { this.minimumRedeemLimit = minimumRedeemLimit; }

    public double getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(double exchangeRate) { this.exchangeRate = exchangeRate; }
}