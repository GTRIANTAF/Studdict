package com.studdict.mobile.model;

public class PaymentRequest {
    private Long billId;
    private String paymentMethod;
    private Double amountGiven;
    private String studentId;

    public PaymentRequest(Long billId, String paymentMethod, Double amountGiven, String studentId) {
        this.billId = billId;
        this.paymentMethod = paymentMethod;
        this.amountGiven = amountGiven;
        this.studentId = studentId;
    }

    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Double getAmountGiven() { return amountGiven; }
    public void setAmountGiven(Double amountGiven) { this.amountGiven = amountGiven; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
}
