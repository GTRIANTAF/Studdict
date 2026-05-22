package com.studdict.controller;

import com.studdict.model.Bill;
import com.studdict.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment/transaction")
public class TransactionProcessingController {

    private final PaymentService paymentService;

    public TransactionProcessingController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public PaymentResponse processTransaction(@RequestBody GenericPaymentRequest paymentRequest) {
        try {
            Bill bill = paymentService.processPayment(paymentRequest.getBillId(), "CARD", paymentRequest.getAmount());
            return new PaymentResponse(true, "Η επεξεργασία της κάρτας ολοκληρώθηκε", bill.getTotalAmount());
        } catch (Exception e) {
            return new PaymentResponse(false, e.getMessage(), 0.0);
        }
    }

    public static class GenericPaymentRequest {
        private Long billId;
        private double amount;
        public Long getBillId() { return billId; }
        public double getAmount() { return amount; }
    }

    public static class PaymentResponse {
        private boolean success;
        private String message;
        private double amountPaid;
        public PaymentResponse(boolean success, String message, double amountPaid) {
            this.success = success;
            this.message = message;
            this.amountPaid = amountPaid;
        }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public double getAmountPaid() { return amountPaid; }
    }
}