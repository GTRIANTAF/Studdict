package com.studdict.model;

import jakarta.persistence.*;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Aligned with main branch
    @Column(name = "item_id")
    private Long itemId;

    private String name;
    private double price;

    @Column(name = "is_available")
    private boolean isAvailable;

    private String category;

    public MenuItem() {}

    public Long getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public String getCategory() {
        return category;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}