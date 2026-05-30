package com.studdict.mobile.model;

public class PaymentRequest {
    private Long billId;
    private String paymentMethod;
    private Double amountGiven;

    public PaymentRequest(Long billId, String paymentMethod, Double amountGiven) {
        this.billId = billId;
        this.paymentMethod = paymentMethod;
        this.amountGiven = amountGiven;
    }

    public Long getBillId() { return billId; }
    public String getPaymentMethod() { return paymentMethod; }
    public Double getAmountGiven() { return amountGiven; }
}
