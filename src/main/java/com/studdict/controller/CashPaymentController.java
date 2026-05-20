package com.studdict.controller;

import com.studdict.model.Bill;
import com.studdict.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment/cash")
public class CashPaymentController {

    private final PaymentService paymentService;

    public CashPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/accept")
    public CashPaymentResponse acceptPayment(@RequestBody CashRequest request) {
        try {
            Bill bill = paymentService.processPayment(request.getBillId(), "CASH", request.getAmountGiven());
            return new CashPaymentResponse(true, "Η πληρωμή με μετρητά έγινε επιτυχώς", bill.getTotalAmount());
        } catch (Exception e) {
            return new CashPaymentResponse(false, e.getMessage(), 0.0);
        }
    }

    public static class CashRequest {
        private Long billId;
        private Double amountGiven;
        public Long getBillId() { return billId; }
        public double getAmountGiven() { return amountGiven; }
    }

    public static class CashPaymentResponse {
        private boolean success;
        private String message;
        private double finalPaidAmount;
        public CashPaymentResponse(boolean success, String message, double finalPaidAmount) {
            this.success = success;
            this.message = message;
            this.finalPaidAmount = finalPaidAmount;
        }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public double getFinalPaidAmount() { return finalPaidAmount; }
    }
}