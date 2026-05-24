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
    /**
     * Υλοποιεί το requestAccess() του Sequence Diagram
     */
    public boolean requestAccess(Long checkInId) {
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new RuntimeException("Το Check-in δεν βρέθηκε."));
        if (!checkIn.isSuccessful() || isReservationExpired(checkIn.getReservation())) {
            throw new RuntimeException("Check-in Required (ή η κράτηση έληξε).");
        }
        return true;
    }

    public List<EBook> executeSearch(String keyword) {
        // Υλοποιεί το SearchCtrl.executeSearch() του Sequence Diagram
        return eBookRepository.findAll().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(keyword.toLowerCase()) || 
                             b.getAuthor().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

    /**
     * Υλοποιεί το requestLoan() του Sequence Diagram
     */
    public EBookLoan requestLoan(Long checkInId, Long ebookId) {
        EBookLicense license = checkAvailability(ebookId);
        return createLoan(checkInId, license);
    }

    public EBookLicense checkAvailability(Long ebookId) {
        EBook ebook = eBookRepository.findById(ebookId)
                .orElseThrow(() -> new RuntimeException("Το βιβλίο δεν βρέθηκε."));

        return ebook.getLicenses().stream()
                .filter(EBookLicense::isAvailable)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Δεν υπάρχει διαθέσιμη άδεια αυτή τη στιγμή."));
    }

    public EBookLoan createLoan(Long checkInId, EBookLicense availableLicense) {
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new RuntimeException("Πρέπει να κάνετε Check-in για να δανειστείτε E-book."));

        if (!checkIn.isSuccessful()) {
            throw new RuntimeException("Μη έγκυρο Check-in.");
        }

        if (isReservationExpired(checkIn.getReservation())) {
            throw new RuntimeException("Η κράτησή σας έχει λήξει. Η πρόσβαση στο e-book δεν είναι διαθέσιμη.");
        }

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
    public EBookLoan checkRequest(Long loanId) {
        EBookLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Ο δανεισμός δεν βρέθηκε."));

        if (!loan.isActive()) {
            throw new RuntimeException("Ο δανεισμός δεν είναι ενεργός.");
        }
        return loan;
    }

    public void revokeLoan(Long checkInId) {
        List<EBookLoan> activeLoans = loanRepository.findActiveLoansByCheckIn(checkInId);
        for (EBookLoan loan : activeLoans) {
            releaseLoan(loan);
        }
    }

    // --- Βοηθητικές μέθοδοι ---

    public void releaseLoan(EBookLoan loan) {
        // Re-fetch to attach to the current Hibernate session and avoid LazyInitializationException
        EBookLoan attachedLoan = loanRepository.findById(loan.getLoanId())
                .orElseThrow(() -> new RuntimeException("Ο δανεισμός δεν βρέθηκε."));

        attachedLoan.setActive(false);
        attachedLoan.setEndTime(LocalDateTime.now());

        EBookLicense license = attachedLoan.getLicense();
        if (license != null) {
            license.setAvailable(true);
            licenseRepository.save(license);
        }
        loanRepository.save(attachedLoan);
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