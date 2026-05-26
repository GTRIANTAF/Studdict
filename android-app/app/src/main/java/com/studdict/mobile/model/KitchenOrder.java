package com.studdict.mobile.model;

import java.util.List;

public class KitchenOrder {
    private Long orderId;
    private String tableInfo;
    private String status;
    private String placedAt;
    private List<String> items;

    public Long getOrderId() { return orderId; }
    public String getTableInfo() { return tableInfo; }
    public String getStatus() { return status; }
    public String getPlacedAt() { return placedAt; }
    public List<String> getItems() { return items; }
}
