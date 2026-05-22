package com.studdict.service;

import com.studdict.model.*;
import com.studdict.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EBookService {

    @Autowired private EBookRepository eBookRepository;
    @Autowired private EBookLoanRepository loanRepository;
    @Autowired private EBookLicenseRepository licenseRepository;
    @Autowired private CheckInRepository checkInRepository;

    /**
     * UC7 - Ψηφιακός Δανεισμός E-book (Βασική Ροή).
     * Διαθέσιμο μόνο όταν ο φοιτητής έχει ενεργό, έγκυρο Check-in σε κράτηση που δεν έχει λήξει.
     */
    public EBookLoan borrowEBook(Long checkInId, Long ebookId) {
        // 1. Έλεγχος ενεργού Check-in (Alt 1: αν δεν υπάρχει -> σφάλμα)
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new RuntimeException("Πρέπει να κάνετε Check-in για να δανειστείτε E-book."));

        if (!checkIn.isSuccessful()) {
            throw new RuntimeException("Μη έγκυρο Check-in.");
        }

        // Η λειτουργία e-book είναι διαθέσιμη μόνο κατά τη διάρκεια ενεργής παραμονής
        if (isReservationExpired(checkIn.getReservation())) {
            throw new RuntimeException("Η κράτησή σας έχει λήξει. Η πρόσβαση στο e-book δεν είναι διαθέσιμη.");
        }

        EBook ebook = eBookRepository.findById(ebookId)
                .orElseThrow(() -> new RuntimeException("Το βιβλίο δεν βρέθηκε."));

        // 2. Εύρεση διαθέσιμης άδειας (Alt 2: αν δεν υπάρχει -> σφάλμα)
        EBookLicense availableLicense = ebook.getLicenses().stream()
                .filter(EBookLicense::isAvailable)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Δεν υπάρχει διαθέσιμη άδεια αυτή τη στιγμή."));

        // 3. Δέσμευση άδειας και δημιουργία δανεισμού
        availableLicense.setAvailable(false);
        licenseRepository.save(availableLicense);

        EBookLoan loan = new EBookLoan();
        loan.setCheckIn(checkIn);
        loan.setLicense(availableLicense);
        loan.setStartTime(LocalDateTime.now());
        loan.setActive(true);

        return loanRepository.save(loan);
    }

    /**
     * UC7 - Εναλλακτική Ροή 3: Πρόωρη επιστροφή E-book από τον φοιτητή.
     * Απελευθερώνει την άδεια, ώστε να είναι άμεσα διαθέσιμη σε άλλους φοιτητές.
     */
    public EBookLoan returnEBook(Long loanId) {
        EBookLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Ο δανεισμός δεν βρέθηκε."));

        if (!loan.isActive()) {
            throw new RuntimeException("Ο δανεισμός δεν είναι ενεργός.");
        }

        releaseLoan(loan);
        return loan;
    }

    /**
     * UC7 - Βασική Ροή βήμα 9 & Εναλλακτική Ροή 4 (αυτόματη ανάκληση λόγω Check-out / λήξης).
     * Καλείται από το Check-out (UC6) ή τον μηχανισμό λήξης χρόνου για να απελευθερώσει
     * όλες τις ενεργές άδειες ενός Check-in.
     */
    public void releaseLoansForCheckIn(Long checkInId) {
        List<EBookLoan> activeLoans = loanRepository.findActiveLoansByCheckIn(checkInId);
        for (EBookLoan loan : activeLoans) {
            releaseLoan(loan);
        }
    }

    // --- Βοηθητικές μέθοδοι ---

    private void releaseLoan(EBookLoan loan) {
        loan.setActive(false);
        loan.setEndTime(LocalDateTime.now());

        EBookLicense license = loan.getLicense();
        if (license != null) {
            license.setAvailable(true);
            licenseRepository.save(license);
        }
        loanRepository.save(loan);
    }

    private boolean isReservationExpired(Reservation reservation) {
        if (reservation == null) {
            return false; // Δεν μπορούμε να ελέγξουμε λήξη -> δεν μπλοκάρουμε τον δανεισμό
        }
        LocalDateTime end = reservation.getReservationDate()
                .atTime(reservation.getStartTime())
                .plusMinutes(reservation.getDurationMinutes());
        return LocalDateTime.now().isAfter(end);
    }
}