package com.studdict.mobile.model;

public class SplitRequest {
    private Long billId;

    public SplitRequest(Long billId) {
        this.billId = billId;
    }

    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
}
