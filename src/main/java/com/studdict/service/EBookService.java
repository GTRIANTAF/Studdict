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

    public EBookLoan borrowEBook(Long checkInId, Long ebookId) {
        // 1. Έλεγχος αν ο φοιτητής είναι checked-in (UC7 - Βασική ροή) [cite: 411]
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new RuntimeException("Πρέπει να κάνετε Check-in για να δανειστείτε E-book."));

        if (!checkIn.isSuccessful()) {
            throw new RuntimeException("Μη έγκυρο Check-in.");
        }

        EBook ebook = eBookRepository.findById(ebookId).orElseThrow();

        // 2. Εύρεση διαθέσιμης άδειας [cite: 231, 240]
        EBookLicense availableLicense = ebook.getLicenses().stream()
                .filter(EBookLicense::isAvailable)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Δεν υπάρχει διαθέσιμη άδεια αυτή τη στιγμή."));

        // 3. Δέσμευση άδειας και δημιουργία δανεισμού [cite: 231]
        availableLicense.setAvailable(false);
        licenseRepository.save(availableLicense);

        EBookLoan loan = new EBookLoan();
        loan.setCheckIn(checkIn);
        loan.setLicense(availableLicense);
        loan.setStartTime(LocalDateTime.now());
        loan.setActive(true);

        return loanRepository.save(loan);
    }
}