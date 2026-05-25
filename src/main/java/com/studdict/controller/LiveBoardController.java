package com.studdict.controller;

import com.studdict.model.Reservation;
import com.studdict.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/liveboard")
@CrossOrigin(origins = "*")
public class LiveBoardController {

    @Autowired
    private ReservationService reservationService;

    // Boundary to return all published public reservations
    @GetMapping("/published")
    public ResponseEntity<List<Reservation>> getPublishedReservations() {
        return ResponseEntity.ok(reservationService.getPublishedReservations());
    }
}
