package com.studdict.mobile.model;

public class OrderItemRequest {
    public Long menuItemId;
    public int quantity;

    public OrderItemRequest(Long menuItemId, int quantity) {
        this.menuItemId = menuItemId;
        this.quantity = quantity;
    }
}
