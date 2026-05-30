package com.studdict.mobile.model;

public class SplitRequest {
    private Long billId;
    private int numberOfPeople;

    public SplitRequest(Long billId, int numberOfPeople) {
        this.billId = billId;
        this.numberOfPeople = numberOfPeople;
    }

    public Long getBillId() { return billId; }
    public int getNumberOfPeople() { return numberOfPeople; }
}
