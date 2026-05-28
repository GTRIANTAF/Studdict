package com.studdict.service;

import com.studdict.model.CheckIn;
import com.studdict.model.EBook;
import com.studdict.model.EBookLicense;
import com.studdict.model.EBookLoan;
import com.studdict.model.Reservation;
import com.studdict.repository.CheckInRepository;
import com.studdict.repository.EBookLicenseRepository;
import com.studdict.repository.EBookLoanRepository;
import com.studdict.repository.EBookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EBookServiceTest {

    @Mock
    private EBookRepository eBookRepository;

    @Mock
    private EBookLoanRepository loanRepository;

    @Mock
    private EBookLicenseRepository licenseRepository;

    @Mock
    private CheckInRepository checkInRepository;

    @InjectMocks
    private EBookService eBookService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRequestAccess_ValidCheckIn_ReturnsTrue() {
        // Arrange
        CheckIn checkIn = new CheckIn();
        checkIn.setSuccessful(true);
        
        Reservation reservation = new Reservation() {};
        reservation.setReservationDate(LocalDate.now());
        reservation.setStartTime(LocalTime.now().minusMinutes(10));
        reservation.setDurationMinutes(60);
        checkIn.setReservation(reservation);

        when(checkInRepository.findById(1L)).thenReturn(Optional.of(checkIn));

        // Act
        boolean access = eBookService.requestAccess(1L);

        // Assert
        assertTrue(access);
    }

    @Test
    public void testRequestAccess_InvalidCheckIn_ThrowsException() {
        // Arrange
        CheckIn checkIn = new CheckIn();
        checkIn.setSuccessful(false); // Invalid checkin

        when(checkInRepository.findById(1L)).thenReturn(Optional.of(checkIn));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            eBookService.requestAccess(1L);
        });

        assertTrue(exception.getMessage().contains("Check-in Required"));
    }

    @Test
    public void testCheckAvailability_LicenseAvailable_ReturnsLicense() {
        // Arrange
        EBook ebook = new EBook();
        EBookLicense license = new EBookLicense();
        license.setAvailable(true);
        ebook.setLicenses(Collections.singletonList(license));

        when(eBookRepository.findById(1L)).thenReturn(Optional.of(ebook));

        // Act
        EBookLicense availableLicense = eBookService.checkAvailability(1L);

        // Assert
        assertNotNull(availableLicense);
        assertTrue(availableLicense.isAvailable());
    }

    @Test
    public void testCreateLoan_ValidConditions_Success() {
        // Arrange
        CheckIn checkIn = new CheckIn();
        checkIn.setSuccessful(true);

        EBookLicense license = new EBookLicense();
        license.setAvailable(true);

        when(checkInRepository.findById(1L)).thenReturn(Optional.of(checkIn));
        when(loanRepository.save(any(EBookLoan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EBookLoan loan = eBookService.createLoan(1L, license);

        // Assert
        assertNotNull(loan);
        assertTrue(loan.isActive());
        assertFalse(license.isAvailable());
        verify(licenseRepository, times(1)).save(license);
        verify(loanRepository, times(1)).save(any(EBookLoan.class));
    }
}
