package com.studdict.service;

import com.studdict.model.CheckIn;
import com.studdict.model.EBook;
import com.studdict.model.EBookLicense;
import com.studdict.model.EBookContentDTO;
import com.studdict.model.EBookLoan;
import com.studdict.model.EBookLoanDTO;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    @Test
    public void testGetSessionLoans_IncludesReturnedBooks() {
        // Arrange: one still-active loan and one the student already returned, same check-in.
        EBook activeBook = new EBook();
        activeBook.seteBookId(10L);
        activeBook.setTitle("Active Book");
        activeBook.setAuthor("Author A");
        EBookLicense activeLicense = new EBookLicense();
        activeLicense.setEbook(activeBook);
        EBookLoan activeLoan = new EBookLoan();
        activeLoan.setLoanId(1L);
        activeLoan.setLicense(activeLicense);
        activeLoan.setActive(true);

        EBook returnedBook = new EBook();
        returnedBook.seteBookId(20L);
        returnedBook.setTitle("Returned Book");
        returnedBook.setAuthor("Author B");
        EBookLicense returnedLicense = new EBookLicense();
        returnedLicense.setEbook(returnedBook);
        EBookLoan returnedLoan = new EBookLoan();
        returnedLoan.setLoanId(2L);
        returnedLoan.setLicense(returnedLicense);
        returnedLoan.setActive(false); // returned early, before checkout

        when(loanRepository.findAllByCheckIn(1L))
                .thenReturn(Arrays.asList(activeLoan, returnedLoan));

        // Act
        List<EBookLoanDTO> result = eBookService.getSessionLoansWithInfo(1L);

        // Assert: BOTH books appear, and the returned one is flagged as returned.
        assertEquals(2, result.size());
        EBookLoanDTO active = result.stream()
                .filter(d -> d.getLoanId() == 1L).findFirst().orElseThrow();
        EBookLoanDTO returned = result.stream()
                .filter(d -> d.getLoanId() == 2L).findFirst().orElseThrow();
        assertEquals("Active Book", active.getTitle());
        assertFalse(active.isReturned());
        assertEquals("Returned Book", returned.getTitle());
        assertTrue(returned.isReturned());
    }

    @Test
    public void testGetBookContent_SplitsPagesOnFormFeed() {
        // Arrange: content stored as three pages separated by form-feed.
        EBook book = new EBook();
        book.seteBookId(7L);
        book.setTitle("Clean Code");
        book.setAuthor("Robert C. Martin");
        book.setContent("Page one text.\fPage two text.\fPage three text.");
        when(eBookRepository.findById(7L)).thenReturn(Optional.of(book));

        // Act
        EBookContentDTO dto = eBookService.getBookContent(7L);

        // Assert: split into 3 trimmed pages, metadata preserved.
        assertEquals(7L, dto.getEbookId());
        assertEquals("Clean Code", dto.getTitle());
        assertEquals(3, dto.getPages().size());
        assertEquals("Page one text.", dto.getPages().get(0));
        assertEquals("Page two text.", dto.getPages().get(1));
        assertEquals("Page three text.", dto.getPages().get(2));
    }

    @Test
    public void testGetBookContent_EmptyContent_ReturnsPlaceholderPage() {
        EBook book = new EBook();
        book.seteBookId(8L);
        book.setTitle("Empty Book");
        book.setAuthor("Nobody");
        book.setContent(null);
        when(eBookRepository.findById(8L)).thenReturn(Optional.of(book));

        EBookContentDTO dto = eBookService.getBookContent(8L);

        // Always at least one page so the reader has something to show.
        assertEquals(1, dto.getPages().size());
        assertFalse(dto.getPages().get(0).isEmpty());
    }
}
