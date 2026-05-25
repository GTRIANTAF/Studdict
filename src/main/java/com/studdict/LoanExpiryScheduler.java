package com.studdict;

import com.studdict.service.EBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * UC7 — Automatic Revocation via Expiry branch.
 * Runs every 60 seconds and releases all active loans whose reservation has expired,
 * corresponding to the Timer -> EBookService: signalExpiry() step in the sequence diagram.
 */
@Component
public class LoanExpiryScheduler {

    @Autowired
    private EBookService eBookService;

    @Scheduled(fixedDelay = 60_000)
    public void revokeExpiredLoans() {
        eBookService.revokeExpiredLoans();
    }
}
