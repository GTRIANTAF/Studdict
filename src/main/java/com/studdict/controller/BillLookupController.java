package com.studdict.controller;

import com.studdict.model.Bill;
import com.studdict.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * UC8 — BillScreen: refreshScreen() step.
 * Provides the bill for a given table so the Android BillScreen can display it.
 */
@RestController
@RequestMapping("/api/bills")
public class BillLookupController {

    @Autowired
    private BillRepository billRepository;

    @GetMapping("/table/{tableId}")
    public ResponseEntity<?> getBillByTable(@PathVariable Integer tableId) {
        return billRepository.findTopByTableIdOrderByIssueTimeDesc(tableId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
