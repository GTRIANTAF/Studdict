package com.studdict.repository;

import com.studdict.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // UC2: Used for the LiveBoard to find active Public reservations
    List<Reservation> findByVisibilityAndStatus(String visibility, String status);

    // =====================================================================
    // 🚀 ΝΕΟ: UC2 - Matchmaking Query
    // =====================================================================
    @Query("SELECT pr FROM PublicReservation pr WHERE pr.table.venue.venueId = :venueId AND pr.status = 'CONFIRMED' AND LOWER(pr.studySubject.name) = LOWER(:subjectName)")
    List<Reservation> findMatchmakingReservations(@Param("venueId") Long venueId, @Param("subjectName") String subjectName);

    // Find active reservation by table id
    List<Reservation> findByTable_TableIdAndStatus(Integer tableId, String status);

    // Fetch active reservations for a specific student
    @Query("SELECT r FROM Reservation r JOIN ReservationParticipant rp ON r.reservationId = rp.reservationId WHERE rp.studentId = :studentId AND r.status = 'CONFIRMED'")
    List<Reservation> findActiveReservationsByStudent(@Param("studentId") String studentId);
}