package com.studdict.repository;

import com.studdict.model.ReservationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationParticipantRepository extends JpaRepository<ReservationParticipant, Long> {

    List<ReservationParticipant> findByReservationId(Long reservationId);

    List<ReservationParticipant> findByIdIn(List<Long> participantIds);
}