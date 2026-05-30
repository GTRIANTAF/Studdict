package com.studdict.mobile.model;

public class Bill {
    private Long billId;
    private Long reservationId;
    private Integer tableId;
    private double totalAmount;
    private double discountAmount;
    private boolean settled;

    public Long getBillId() { return billId; }
    public Long getReservationId() { return reservationId; }
    public Integer getTableId() { return tableId; }
    public double getTotalAmount() { return totalAmount; }
    public double getDiscountAmount() { return discountAmount; }
    public boolean isSettled() { return settled; }
}
