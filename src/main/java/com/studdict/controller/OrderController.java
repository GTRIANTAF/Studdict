package com.studdict.controller;

import com.studdict.dto.OrderRequest;
import com.studdict.model.MenuItem;
import com.studdict.model.Order;
import com.studdict.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/catalog")
    public ResponseEntity<List<MenuItem>> readCatalog() {
        return ResponseEntity.ok(orderService.readCatalog());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        try {
            // 1. Process Summary
            orderService.processSummary(request.getRequestedItems());

            // 2. Verify Availability
            orderService.verifyAvailability(request.getRequestedItems());

            // 3. Create Order
            Order order = orderService.createOrder(request.getTableId(), request.getRequestedItems());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelOrder() {
        try {
            orderService.cancelOrder();
            return ResponseEntity.ok("Order cancelled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
