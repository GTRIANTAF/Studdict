package com.studdict.controller;

import com.studdict.dto.ReservationRequestDTO;
import com.studdict.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    // UC1: Δημιουργία Ιδιωτικής Κράτησης
    @PostMapping("/private")
    public ResponseEntity<Long> createPrivateReservation(@RequestBody ReservationRequestDTO request) {
        Long resId = reservationService.savePrivateReservation(
                request.getStudentId(), request.getTableId(),
                request.getDate(), request.getTime(), request.getDuration(),
                request.getNumberOfPeople()
        );
        return ResponseEntity.ok(resId);
    }

    // UC2: Δημιουργία Δημόσιας Κράτησης
    @PostMapping("/public")
    public ResponseEntity<Long> createPublicReservation(@RequestBody ReservationRequestDTO request) {
        Long resId = reservationService.savePublicReservation(
                request.getStudentId(), request.getTableId(),
                request.getDate(), request.getTime(), request.getDuration(),
                request.getNumberOfPeople(),
                request.getSubjectName()
        );
        return ResponseEntity.ok(resId);
    }

    // UC2: Συμμετοχή σε υπάρχουσα Δημόσια Κράτηση
    @PostMapping("/{reservationId}/join")
    public ResponseEntity<String> joinPublicReservation(@PathVariable Long reservationId, @RequestParam String studentId) {
        reservationService.joinPublicReservation(reservationId, studentId);
        return ResponseEntity.ok("Επιτυχής συμμετοχή στην παρέα!");
    }

    // Ακύρωση Κράτησης
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<String> cancelReservation(@PathVariable Long reservationId) {
        reservationService.discardReservation(reservationId);
        return ResponseEntity.ok("Η κράτηση ακυρώθηκε.");
    }

    // Λήψη ενεργών κρατήσεων φοιτητή
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getReservationsByStudent(@PathVariable String studentId) {
        return ResponseEntity.ok(reservationService.getReservationsByStudent(studentId));
    }
}
