package com.studdict.repository;

import com.studdict.model.ReservationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantRepository extends JpaRepository<ReservationParticipant, Long> {
}