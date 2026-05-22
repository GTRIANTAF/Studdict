package com.studdict.repository;

import com.studdict.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CheckInRepository extends JpaRepository<CheckIn, Long> {

    // UC6/UC7: Βρίσκει τα check-ins μιας κράτησης, ώστε στο check-out
    // να απελευθερωθούν αυτόματα οι ενεργές άδειες e-book.
    List<CheckIn> findByReservation_ReservationId(Long reservationId);
}