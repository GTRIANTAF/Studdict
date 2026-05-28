package com.studdict.mobile.model;

public class Bill {
    private Long billId;
    private Long reservationId;
    private Integer tableId;
    private double totalAmount;
    private boolean settled;

    public Long getBillId() { return billId; }
    public Long getReservationId() { return reservationId; }
    public Integer getTableId() { return tableId; }
    public double getTotalAmount() { return totalAmount; }
    public boolean isSettled() { return settled; }
}
