package com.studdict.mobile.model;

public class LoyaltyWallet {
    private String studentId;
    private int totalBalance = 0;
    private int minimumRedeemLimit = 100;
    private double exchangeRate = 0.05;

    public LoyaltyWallet() {}

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public int getTotalBalance() { return totalBalance; }
    public void setTotalBalance(int totalBalance) { this.totalBalance = totalBalance; }

    public int getMinimumRedeemLimit() { return minimumRedeemLimit; }
    public void setMinimumRedeemLimit(int minimumRedeemLimit) { this.minimumRedeemLimit = minimumRedeemLimit; }

    public double getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(double exchangeRate) { this.exchangeRate = exchangeRate; }
}
