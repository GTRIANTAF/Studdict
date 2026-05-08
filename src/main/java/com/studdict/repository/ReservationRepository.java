package com.studdict.repository;

import com.studdict.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // UC2: Used for the LiveBoard to find active Public reservations
    List<Reservation> findByVisibilityAndStatus(String visibility, String status);
}