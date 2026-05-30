package com.studdict.mobile.model;

public class PaymentResponse {
    private boolean success;
    private String message;
    private double amountPaid;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public double getAmountPaid() { return amountPaid; }
}
