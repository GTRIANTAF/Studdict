package com.studdict.dto;

import java.util.List;

public class OrderRequest {
    private int tableId;
    private List<OrderItemRequest> requestedItems;

    // Getters & Setters
    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }

    public List<OrderItemRequest> getRequestedItems() { return requestedItems; }
    public void setRequestedItems(List<OrderItemRequest> requestedItems) { this.requestedItems = requestedItems; }
}
