package com.studdict.controller;

import com.studdict.model.Bill;
import com.studdict.service.CheckoutService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/generate")
    public BillResponse generateBill(@RequestBody CheckoutRequest request) {
        Bill bill = checkoutService.generateBillForReservation(request.getReservationId());
        return new BillResponse(bill);
    }

    public static class CheckoutRequest {
        private Long reservationId;
        public Long getReservationId() { return reservationId; }
        public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    }

    public static class BillResponse {
        private Long billId;
        private Double totalAmount;
        private LocalDateTime issueTime;
        private boolean isSettled;
        private Long reservationId;

        public BillResponse(Bill bill) {
            this.billId = bill.getBillId();
            this.totalAmount = bill.getTotalAmount();
            this.issueTime = bill.getIssueTime();
            this.isSettled = bill.isSettled();
            this.reservationId = bill.getReservation().getReservationId();
        }

        public Long getBillId() { return billId; }
        public Double getTotalAmount() { return totalAmount; }
        public LocalDateTime getIssueTime() { return issueTime; }
        public boolean isSettled() { return isSettled; }
        public Long getReservationId() { return reservationId; }
    }
}