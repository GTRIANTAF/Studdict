package com.studdict.mobile.model;

import java.util.List;

public class OrderRequest {
    public int tableId;
    public List<OrderItemRequest> requestedItems;

    public OrderRequest(int tableId, List<OrderItemRequest> requestedItems) {
        this.tableId = tableId;
        this.requestedItems = requestedItems;
    }
}
