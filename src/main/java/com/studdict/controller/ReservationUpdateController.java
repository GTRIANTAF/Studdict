package com.studdict.controller;

import com.studdict.model.Reservation;
import com.studdict.service.ReservationUpdateService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/reservations")
public class ReservationUpdateController {

    private final  ReservationUpdateService updateService;

    public ReservationUpdateController(ReservationUpdateService updateService) {
        this.updateService = updateService;
    }

    @PutMapping("/{id}/modify")
    public ReservationUpdateResponse modifyReservation(
            @PathVariable Long id,
            @RequestBody ReservationUpdateRequest request) {
        LocalTime parsedTime = LocalTime.parse(request.getNewTime());
        Reservation updatedReservation = updateService.modifyReservation(id, parsedTime, request.getNewDuration(), request.getNewCapacity());

        return new ReservationUpdateResponse(updatedReservation);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public org.springframework.http.ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return org.springframework.http.ResponseEntity.badRequest().body(ex.getMessage());
    }

    public static class ReservationUpdateRequest {
        private String newTime;
        private int newDuration;
        private int newCapacity;

        public String getNewTime() { return newTime; }
        public void setNewTime(String newTime) { this.newTime = newTime; }
        public int getNewDuration() { return newDuration; }
        public void setNewDuration(int newDuration) { this.newDuration = newDuration; }
        public int getNewCapacity() { return newCapacity; }
        public void setNewCapacity(int newCapacity) { this.newCapacity = newCapacity; }
    }

    public static class ReservationUpdateResponse {
        private Long reservationId;
        private String reservationDate;
        private String startTime;
        private int durationMinutes;
        private String status;

        public ReservationUpdateResponse(Reservation reservation) {
            this.reservationId = reservation.getReservationId();
            this.reservationDate = reservation.getReservationDate() != null ? reservation.getReservationDate().toString() : null;
            this.startTime = reservation.getStartTime() != null ? reservation.getStartTime().toString() : null;
            this.durationMinutes = reservation.getDurationMinutes();
            this.status = reservation.getStatus();
        }

        public Long getReservationId() { return reservationId; }
        public String getReservationDate() { return reservationDate; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public int getDurationMinutes() { return durationMinutes; }
    }
}