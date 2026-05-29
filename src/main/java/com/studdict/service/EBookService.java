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

    // UC7 step 4: returns catalog with per-book availability indicator
    public List<EBookCatalogDTO> getCatalog(String keyword) {
        return eBookRepository.findAll().stream()
                .filter(b -> keyword == null || keyword.isBlank()
                        || b.getTitle().toLowerCase().contains(keyword.toLowerCase())
                        || b.getAuthor().toLowerCase().contains(keyword.toLowerCase()))
                .map(b -> {
                    boolean available = b.getLicenses().stream().anyMatch(EBookLicense::isAvailable);
                    return new EBookCatalogDTO(b.geteBookId(), b.getTitle(), b.getAuthor(), b.getCategory(), available);
                })
                .toList();
    }

    // Returns active loans for a check-in enriched with book title/author for the bill screen
    public List<EBookLoanDTO> getActiveLoansWithInfo(Long checkInId) {
        return loanRepository.findActiveLoansByCheckIn(checkInId).stream()
                .filter(loan -> loan.getLicense() != null && loan.getLicense().getEbook() != null)
                .map(loan -> {
                    EBook book = loan.getLicense().getEbook();
                    return new EBookLoanDTO(loan.getLoanId(), book.geteBookId(), book.getTitle(), book.getAuthor());
                })
                .toList();
    }

    // Returns ALL loans of a check-in (active + already returned), so the bill screen can show
    // every e-book borrowed during the session even if some were returned early by the student.
    public List<EBookLoanDTO> getSessionLoansWithInfo(Long checkInId) {
        return loanRepository.findAllByCheckIn(checkInId).stream()
                .filter(loan -> loan.getLicense() != null && loan.getLicense().getEbook() != null)
                .map(loan -> {
                    EBook book = loan.getLicense().getEbook();
                    return new EBookLoanDTO(loan.getLoanId(), book.geteBookId(),
                            book.getTitle(), book.getAuthor(), !loan.isActive());
                })
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

    // Used by the Android reader screen to poll for external loan termination
    public EBookLoan getLoanStatus(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Ο δανεισμός δεν βρέθηκε."));
    }

    // UC7 Timer branch: called by LoanExpiryScheduler every minute
    public void revokeExpiredLoans() {
        for (EBookLoan loan : loanRepository.findByIsActiveTrue()) {
            CheckIn checkIn = loan.getCheckIn();
            if (checkIn != null && isReservationExpired(checkIn.getReservation())) {
                releaseLoan(loan);
            }
        }
    }

    public void revokeLoan(Long checkInId) {
        List<EBookLoan> activeLoans = loanRepository.findActiveLoansByCheckIn(checkInId);
        for (EBookLoan loan : activeLoans) {
            releaseLoan(loan);
        }
    }

    /**
     * Καλείται από το TimerSystem όταν λήξει η κράτηση (όπως προστέθηκε στο Sequence Diagram)
     */
    public void signalExpiry(Long checkInId) {
        revokeLoan(checkInId);
    }

    /**
     * Καλείται από το CheckoutService κατά το Standard Check-out
     */
    public void notifyCheckoutCompleted(Long checkInId) {
        revokeLoan(checkInId);
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