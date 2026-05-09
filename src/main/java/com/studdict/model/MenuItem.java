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

    // need to add Getters and Setters
}