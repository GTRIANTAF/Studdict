package com.studdict.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment/timeout")
public class TimeoutManagementController {

    @GetMapping("/check/{billId}")
    public TimeoutResponse checkTimeout(@PathVariable Long billId) {
        boolean hasTimedOut = false;

        if (hasTimedOut) {
            return new TimeoutResponse(true, "Η διαδικασία πληρωμής έληξε. Δοκίμασε ξανά");
        }
        return new TimeoutResponse(false, "Η διαδικασία πληρωμής είναι ακόμα ενεργή");
    }

    public static class TimeoutResponse {
        private boolean timedOut;
        private String statusMessage;
        public TimeoutResponse(boolean timedOut, String statusMessage) {
            this.timedOut = timedOut;
            this.statusMessage = statusMessage;
        }
        public boolean isTimedOut() { return timedOut; }
        public String getStatusMessage() { return statusMessage; }
    }
}