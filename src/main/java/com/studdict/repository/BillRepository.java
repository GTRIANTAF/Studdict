package com.studdict.repository;

import com.studdict.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    // UC6: Επιτρέπει στο Check-out να εντοπίσει υπάρχοντα λογαριασμό μιας κράτησης
    // (idempotency: δεν δημιουργούμε διπλό Bill αν ξανακληθεί το checkout).
    Optional<Bill> findByReservationId(Long reservationId);

    Optional<Bill> findTopByTableIdOrderByIssueTimeDesc(Integer tableId);
}