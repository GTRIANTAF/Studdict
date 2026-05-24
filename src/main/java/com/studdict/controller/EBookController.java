package com.studdict.controller;

import com.studdict.model.EBook;
import com.studdict.model.EBookLoan;
import com.studdict.service.EBookService;
import org.springframework.beans.factory.annotation.Autowired;
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
