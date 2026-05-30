package com.studdict.controller;

import com.studdict.model.Bill;
import com.studdict.model.Reservation;
import com.studdict.repository.BillRepository;
import com.studdict.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UC8 — BillScreen: refreshScreen() step.
 * Provides the bill for a given table so the Android BillScreen can display it.
 */
@RestController
@RequestMapping("/api/bills")
public class BillLookupController {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @GetMapping("/table/{tableId}")
    public ResponseEntity<?> getBillByTable(@PathVariable Integer tableId) {
        // Find the most recent bill for this table (whether settled or not) so the user can see their F&B total.
        Bill bill = billRepository.findTopByTableIdOrderByIssueTimeDesc(tableId)
                .orElseGet(() -> {
                    // If NO bill exists at all, dynamically create an empty one so the UI doesn't say "Bill not found".
                    Bill newBill = new Bill();
                    newBill.setTableId(tableId);
                    newBill.setTotalAmount(0.0);
                    newBill.setIssueTime(LocalDateTime.now());

                    List<Reservation> activeReservations = reservationRepository.findByTable_TableIdAndStatus(tableId, "CONFIRMED");
                    if (!activeReservations.isEmpty()) {
                        newBill.setReservationId(activeReservations.get(0).getReservationId());
                    }

                    return billRepository.save(newBill);
                });

        return ResponseEntity.ok(bill);
    }
}
