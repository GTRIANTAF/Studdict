package com.studdict.mobile.model;

import com.google.gson.annotations.SerializedName;

public class Order {
    @SerializedName("orderId")
    private Long id;
    private String status;
    private double totalAmount;

    public Long getId() { return id; }
    public String getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
}
