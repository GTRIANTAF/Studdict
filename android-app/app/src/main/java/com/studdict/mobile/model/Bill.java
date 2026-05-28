package com.studdict.mobile.model;

public class Bill {
    private Long billId;
    private Integer tableId;
    private double totalAmount;
    private boolean settled;

    public Long getBillId() { return billId; }
    public Integer getTableId() { return tableId; }
    public double getTotalAmount() { return totalAmount; }
    public boolean isSettled() { return settled; }
}
