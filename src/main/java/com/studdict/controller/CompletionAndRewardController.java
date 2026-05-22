package com.studdict.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment/completion")
public class CompletionAndRewardController {

    @GetMapping("/symmary/{billId}")
    public RewardResponse getSummaryAndRewards(@PathVariable Long billId) {
        return new RewardResponse("Ευχαριστούμε!", 50);
    }

    public static class RewardResponse {
        private String statusMessage;
        private int reward;
        public RewardResponse(String statusMessage, int reward) {
            this.statusMessage = statusMessage;
            this.reward = reward;
        }

        public String getStatusMessage() { return statusMessage; }
        public int getReward() { return reward; }
    }
}