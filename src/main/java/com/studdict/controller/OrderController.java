package com.studdict.controller;

import com.studdict.dto.OrderItemRequest;
import com.studdict.dto.OrderRequest;
import com.studdict.model.MenuItem;
import com.studdict.model.Order;
import com.studdict.model.OrderItem;
import com.studdict.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
            String msg = e.getMessage() != null ? e.getMessage() : "";
            // UC8: Item Out of Stock branch → 409
            if (msg.contains("διαθέσιμο")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(msg);
            }
            return ResponseEntity.badRequest().body(msg);
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelOrder() {
        orderService.cancelOrder();
        return ResponseEntity.ok("Order cancelled successfully");
    }

    @GetMapping("/table/{tableId}/items")
    public ResponseEntity<List<String>> getOrderItemsByTable(@PathVariable Integer tableId) {
        return ResponseEntity.ok(orderService.getOrderItemsAsStrings(tableId));
    }

    // UC8 Gap 5: addProduct per-item validation (MenuView -> OrderService: addProduct)
    @PostMapping("/cart/add")
    public ResponseEntity<?> addCartItem(@RequestParam Long menuItemId, @RequestParam int quantity) {
        try {
            boolean valid = orderService.addCartItem(menuItemId, quantity);
            return ResponseEntity.ok(valid);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("διαθέσιμο")) return ResponseEntity.status(HttpStatus.CONFLICT).body(false);
            return ResponseEntity.badRequest().body(false);
        }
    }

    // UC8 Gap 6: processSummary before showing OrderReview (Cart -> OrderService: processSummary)
    @PostMapping("/summary")
    public ResponseEntity<?> processSummary(@RequestBody List<OrderItemRequest> items) {
        try {
            orderService.processSummary(items);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // UC8 Gap 8: KitchenScreen: displayOrder — kitchen staff polls this endpoint
    @GetMapping("/kitchen/active")
    public ResponseEntity<List<KitchenOrderDTO>> getKitchenActive() {
        List<KitchenOrderDTO> orders = orderService.getActiveOrders().stream()
                .map(KitchenOrderDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    public static class KitchenOrderDTO {
        public Long orderId;
        public String tableInfo;
        public String status;
        public String placedAt;
        public List<String> items;

        public KitchenOrderDTO(Order order) {
            this.orderId = order.getOrderId();
            this.tableInfo = order.getTable() != null
                    ? "Table " + order.getTable().getTableNumber() : "Unknown table";
            this.status = order.getStatus();
            this.placedAt = order.getPlacedAt() != null ? order.getPlacedAt().toString() : "";
            this.items = order.getItems().stream()
                    .map(oi -> oi.getMenuItem().getName() + " x" + oi.getQuantity())
                    .collect(Collectors.toList());
        }
    }
}
