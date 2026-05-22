package com.studdict.repository;

import com.studdict.model.EBookLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EBookLoanRepository extends JpaRepository<EBookLoan, Long> {

    // UC7 (Βασική ροή βήμα 9 & Alt 4): Βρίσκει τους ενεργούς δανεισμούς ενός Check-in,
    // ώστε να απελευθερωθούν αυτόματα οι άδειες κατά το Check-out ή τη λήξη της κράτησης.
    @Query("SELECT l FROM EBookLoan l WHERE l.checkIn.checkInId = :checkInId AND l.isActive = true")
    List<EBookLoan> findActiveLoansByCheckIn(@Param("checkInId") Long checkInId);
}