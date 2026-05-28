package com.studdict.repository;

import com.studdict.model.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InviteCodeRepository extends JpaRepository<InviteCode, Long> {

    InviteCode findByCode(String code);

    Optional<InviteCode> findFirstByReservation_ReservationIdAndActiveTrueOrderByCreatedAtDesc(Long reservationId);

    Optional<InviteCode> findFirstByReservation_ReservationIdOrderByCreatedAtAsc(Long reservationId);
}
