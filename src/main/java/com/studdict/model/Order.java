package com.studdict.model;
import java.util.List;

import javax.print.attribute.standard.DateTimeAtCompleted;

import java.util.ArrayList;

public class Order {
    private String orderId;
    private String status;
    private double totalAmount;
    private DateTimeAtCompleted placedAt;

    private List<OrderItem> items = new ArrayList<>();
}