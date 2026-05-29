package com.studdict.service;

import com.studdict.model.Reservation;
import com.studdict.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@Transactional(readOnly = false)
public class ReservationUpdateService {

    private final ReservationRepository reservationRepository;

    public ReservationUpdateService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation modifyReservation(Long reservationId, LocalTime newTime, int newDuration, int newCapacity) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("reservation " + reservationId + " not found"));

        if (reservation.getTable() != null && newCapacity > reservation.getTable().getCapacity()) {
            throw new IllegalArgumentException("Requested capacity exceeds table capacity");
        }

        reservation.setStartTime(newTime);
        reservation.setDurationMinutes(newDuration);
        reservation.setNumberOfPeople(newCapacity);

        return reservationRepository.save(reservation);
    }
}
