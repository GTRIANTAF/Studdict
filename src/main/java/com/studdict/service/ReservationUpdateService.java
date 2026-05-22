package com.studdict.service;

import com.studdict.model.Reservation;
import com.studdict.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@Transactional
public class ReservationUpdateService {

    private final ReservationRepository reservationRepository;

    public ReservationUpdateService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation modifyReservation(Long reservationId, LocalTime newTime, int newDuration) {
        Reservation  reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("reservation"+reservationId+"not found"));

        reservation.setStartTime(newTime);
        reservation.setDurationMinutes(newDuration);

        return  reservationRepository.save(reservation);
    }
}
