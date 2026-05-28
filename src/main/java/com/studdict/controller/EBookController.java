package com.studdict.controller;

import com.studdict.model.EBook;
import com.studdict.model.EBookLoan;
import com.studdict.service.EBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ebooks")
public class EBookController {

    private final EBookService eBookService;

    @Autowired
    public EBookController(EBookService eBookService) {
        this.eBookService = eBookService;
    }

    @PostMapping("/access/{checkInId}")
    public ResponseEntity<?> requestAccess(@PathVariable Long checkInId) {
        try {
            boolean access = eBookService.requestAccess(checkInId);
            return ResponseEntity.ok(access);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<EBook>> executeSearch(@RequestParam String keyword) {
        return ResponseEntity.ok(eBookService.executeSearch(keyword));
    }

    @PostMapping("/loan")
    public ResponseEntity<?> requestLoan(@RequestParam Long checkInId, @RequestParam Long ebookId) {
        try {
            EBookLoan loan = eBookService.requestLoan(checkInId, ebookId);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            // UC7: Invalid Check-in branch → 403
            if (msg.contains("Check-in") || msg.contains("κράτηση") || msg.contains("λήξει") || msg.contains("Πρέπει")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
            }
            // UC7: No License Available branch → 409
            if (msg.contains("άδεια")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(msg);
            }
            return ResponseEntity.badRequest().body(msg);
        }
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<?> getLoanStatus(@PathVariable Long loanId) {
        try {
            EBookLoan loan = eBookService.getLoanStatus(loanId);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // UC7 Gap 1: separate availability check before requestLoan (per sequence diagram)
    @GetMapping("/availability/{ebookId}")
    public ResponseEntity<?> checkAvailability(@PathVariable Long ebookId) {
        try {
            eBookService.checkAvailability(ebookId);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(false);
        }
    }

    // UC7 Gap 2: called by the checkout UC6 when a student checks out
    @PostMapping("/notify-checkout/{checkInId}")
    public ResponseEntity<?> notifyCheckoutCompleted(@PathVariable Long checkInId) {
        try {
            eBookService.notifyCheckoutCompleted(checkInId);
            return ResponseEntity.ok("Loan(s) terminated due to checkout.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/return/{loanId}")
    public ResponseEntity<?> requestReturn(@PathVariable Long loanId) {
        try {
            EBookLoan loan = eBookService.checkRequest(loanId);
            eBookService.releaseLoan(loan);
            return ResponseEntity.ok("Returned Successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/revoke/{checkInId}")
    public ResponseEntity<?> revokeLoan(@PathVariable Long checkInId) {
        try {
            eBookService.revokeLoan(checkInId);
            return ResponseEntity.ok("Loans revoked due to expiry or checkout.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
