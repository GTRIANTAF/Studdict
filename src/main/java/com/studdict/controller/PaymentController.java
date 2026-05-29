package com.studdict.controller;

import com.studdict.model.Bill;
import com.studdict.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public PaymentResponse processPayment(@RequestBody PaymentRequest request) {
        try {
            Bill paidBill = paymentService.processPayment(
                    request.getBillId(),
                    request.getPaymentMethod(),
                    request.getAmountGiven(),
                    request.getStudentId()
            );
            return new PaymentResponse(true, "Η πληρωμή ολοκληρώθηκε", paidBill.getTotalAmount());
        } catch (RuntimeException e) {
            return new PaymentResponse(false, e.getMessage(), 0.0);
        }
    }

    @PostMapping("/split")
    public SplitResponse splitBill(@RequestBody SplitRequest request) {
        double amountPerPerson = paymentService.calculateSplitAmount(request.getBillId(), request.getNumberOfPeople());
        return new SplitResponse(amountPerPerson);
    }


    public static class PaymentRequest {
        private Long billId;
        private String paymentMethod;
        private Double amountGiven;
        private String studentId;

        public Long getBillId() { return billId; }
        public String getPaymentMethod() { return paymentMethod; }
        public Double getAmountGiven() { return amountGiven; }
        public String getStudentId() { return studentId; }
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

    public static class SplitRequest {
        private Long billId;
        private int numberOfPeople;

        public Long getBillId() { return billId; }
        public int getNumberOfPeople() { return numberOfPeople; }
    }

    public static class SplitResponse {
        private double amountPerPerson;

        public SplitResponse(double amountPerPerson) {
            this.amountPerPerson = amountPerPerson;
        }

        public double getAmountPerPerson() { return amountPerPerson; }
    }
}