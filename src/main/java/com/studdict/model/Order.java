package com.studdict.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    private String status;

    @Column(name = "total_amount")
    private double totalAmount;

    @Column(name = "placed_at")
    private LocalDateTime placedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    // Perfectly matches how Reservation connects to StudyTable in the main branch
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private StudyTable table;

    public Order() {}

    // need to add Getters and Setters
}