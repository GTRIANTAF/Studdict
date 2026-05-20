package com.studdict.controller;

import com.studdict.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment/split")
public class SplitBillController {

    private final PaymentService paymentService;

    public SplitBillController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/calculate")
    public SplitBillResponse calculateSplit(@RequestBody SplitRequest request) {
        double perPerson = paymentService.calculateSplitAmount(request.getBillId(), request.getNumberOfPeople());
        return new SplitBillResponse(perPerson);
    }

    public static class SplitRequest {
        private Long billId;
        private int numberOfPeople;
        public Long getBillId() { return billId; }
        public int getNumberOfPeople() { return numberOfPeople; }
    }

    public static class SplitBillResponse {
        private double amountPerPerson;
        public SplitBillResponse(double amountPerPerson) { this.amountPerPerson = amountPerPerson; }
        public double getAmountPerPerson() { return amountPerPerson; }
    }
}