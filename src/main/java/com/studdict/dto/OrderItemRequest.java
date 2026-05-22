package com.studdict.dto;

/**
 * UC8 - Παραγγελία F&B.
 * Αντιπροσωπεύει μία γραμμή του "καλαθιού": ποιο προϊόν και πόσα τεμάχια.
 * Χρησιμοποιείται από τον OrderController για τη μεταφορά των στοιχείων της παραγγελίας.
 */
public class OrderItemRequest {

    private Long menuItemId;
    private int quantity;

    public OrderItemRequest() {}

    public OrderItemRequest(Long menuItemId, int quantity) {
        this.menuItemId = menuItemId;
        this.quantity = quantity;
    }

    public Long getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}