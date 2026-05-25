package com.studdict.mobile.model;

public class MenuItem {
    private Long itemId;
    private String name;
    private String description;
    private double price;

    public Long getItemId() { return itemId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }

    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
}
