package com.studdict.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment/failure")
public class FailureManagementController {

    @PostMapping("/report")
    public FailureReportResponse reportFailure(@RequestBody FailureRequest request) {
        String logMessage = "Αποτυχία Συναλλαγής στο Bill ID: " + request.getBillId() + "(" + request.getReason() + ")";
        System.out.println(logMessage);

        return new FailureReportResponse("Η αποτυχία καταγράφηκε. Δοκίμασε ξανά");
    }

    public static class FailureRequest {
        private Long billId;
        private String reason;
        public Long getBillId() { return billId; }
        public String getReason() { return reason; }
    }

    public static class FailureReportResponse {
        private String actionRequired;
        public FailureReportResponse(String actionRequired) { this.actionRequired = actionRequired; }
        public String getActionRequired() { return actionRequired; }
    }
}