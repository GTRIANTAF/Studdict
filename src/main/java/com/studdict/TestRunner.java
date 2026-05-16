package com.studdict;

import com.studdict.model.*;
import com.studdict.repository.*;
import com.studdict.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class TestRunner implements CommandLineRunner {

    @Autowired private StudentRepository studentRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private StudyTableRepository tableRepository;
    @Autowired private ReservationService reservationService;
    @Autowired private StudyTableService tableService;
    @Autowired private ParticipantRepository participantRepository;

    // --- Services & Repositories για UC5, UC7, UC8 ---
    @Autowired private EBookRepository eBookRepository;
    @Autowired private EBookLicenseRepository licenseRepository;
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private CheckInService checkInService;
    @Autowired private EBookService eBookService;
    @Autowired private OrderService orderService;
    @Autowired private ReservationRepository reservationRepository;


    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=======================================================");
        System.out.println("=== 🚀 ΕΝΑΡΞΗ ΠΡΟΣΟΜΟΙΩΣΗΣ ΚΡΑΤΗΣΕΩΝ & ΥΠΗΡΕΣΙΩΝ  ===");
        System.out.println("=======================================================\n");

        // --- ΒΗΜΑ 1: SETUP ΔΕΔΟΜΕΝΩΝ (Αν η βάση είναι άδεια) ---
        if (studentRepository.count() == 0) {
            System.out.println("⏳ Δημιουργία εικονικών δεδομένων...");

            // 1. Φοιτητές
            studentRepository.save(new Student("S1", "Γιάννης", "Α.", "giannis@upatras.gr"));
            studentRepository.save(new Student("S2", "Μαρία", "Β.", "maria@upatras.gr"));

            // 2. Χώρος & Τραπέζια
            Venue venue = new Venue("Κεντρική Βιβλιοθήκη", "Πανεπιστημιούπολη", "Library", true);
            venueRepository.save(venue);
            StudyTable table1 = new StudyTable(venue, 1, 4, "QR-1", true);
            tableRepository.save(table1);
            tableRepository.save(new StudyTable(venue, 2, 4, "QR-2", true));

            // 3. E-books & Licenses (Για UC7)
            EBook ebook = new EBook();
            ebook.setTitle("Software Engineering 101");
            ebook.setAuthor("Ian Sommerville");
            ebook.setIsbn("978-1234567890");
            ebook.setCategory("Computer Science");
            ebook = eBookRepository.save(ebook);

            EBookLicense license = new EBookLicense();
            license.setEbook(ebook);
            license.setAvailable(true);
            licenseRepository.save(license);

            // 4. Menu Items (Για UC8)
            MenuItem coffee = new MenuItem();
            coffee.setName("Freddo Espresso");
            coffee.setPrice(2.50);
            coffee.setCategory("Coffee");
            coffee.setAvailable(true);
            menuItemRepository.save(coffee);

            MenuItem sandwich = new MenuItem();
            sandwich.setName("Club Sandwich");
            sandwich.setPrice(5.00);
            sandwich.setCategory("Food");
            sandwich.setAvailable(true);
            menuItemRepository.save(sandwich);

            System.out.println("✅ Τα δεδομένα δημιουργήθηκαν επιτυχώς!\n");
        }

        LocalDate today = LocalDate.now();

        try {
            // =====================================================================
            // ΠΡΟΕΤΟΙΜΑΣΙΑ: Ο Γιάννης κάνει μια κράτηση στο Τραπέζι 1
            // =====================================================================
            System.out.println("▶️ ΠΡΟΕΤΟΙΜΑΣΙΑ: Ο Γιάννης (S1) κάνει κράτηση στο Τραπέζι 1");
            StudyTable table1 = tableRepository.findAll().get(0);
            Student giannis = studentRepository.findById("S1").orElseThrow();

            Long resId = reservationService.savePrivateReservation(
                    "S1", table1.getId(), today, LocalTime.of(10, 0), 120);
            Reservation reservation = reservationRepository.findById(resId).orElseThrow();
            System.out.println("   ✅ Κράτηση δημιουργήθηκε (ID: " + resId + ")\n");

            // =====================================================================
            // TEST 7: Ψηφιακός Δανεισμός E-book (Απαιτεί Check-in)
            // =====================================================================
            System.out.println("▶️ TEST 7 (UC5 & UC7): Check-in και Δανεισμός E-book");

            // Βήμα 7.1: Check-in του Γιάννη (Σκανάρει το σωστό QR)
            CheckIn checkIn = checkInService.checkInStudent(reservation, giannis, "QR-1");
            if (checkIn.isSuccessful()) {
                System.out.println("   ✅ Check-in Επιτυχές! (CheckIn ID: " + checkIn.getCheckInId() + ")");
            }

            // Βήμα 7.2: Δανεισμός Βιβλίου
            EBook bookToBorrow = eBookRepository.findAll().get(0);
            EBookLoan loan = eBookService.borrowEBook(checkIn.getCheckInId(), bookToBorrow.geteBookId());
            System.out.println("   📚 Δανεισμός Επιτυχής! Το βιβλίο '" + bookToBorrow.getTitle() + "' δανείστηκε.");
            System.out.println("   🔒 Η άδεια (License ID: " + loan.getLicense().getLicenseId() + ") δεν είναι πλέον διαθέσιμη.\n");

            // =====================================================================
            // TEST 8: Παραγγελία F&B
            // =====================================================================
            System.out.println("▶️ TEST 8 (UC8): Παραγγελία Food & Drinks");

            List<MenuItem> menu = menuItemRepository.findAll();
            Long coffeeId = menu.get(0).getItemId(); // Freddo Espresso
            Long foodId = menu.get(1).getItemId();   // Club Sandwich

            // Ο Γιάννης παραγγέλνει 1 καφέ και 1 τοστ
            List<Long> itemsToOrder = List.of(coffeeId, foodId);
            Order myOrder = orderService.createOrder(reservation.getReservationId(), itemsToOrder);

            System.out.println("   🍔 Η Παραγγελία στάλθηκε στην Κουζίνα! (Order ID: " + myOrder.getOrderId() + ")");
            System.out.println("   💳 Συνολικό Ποσό Παραγγελίας: " + myOrder.getTotalAmount() + "€");
            System.out.println("   📍 Σχετίζεται με το Τραπέζι: " + myOrder.getTable().getTableNumber() + "\n");

            System.out.println("=======================================================");
            System.out.println("🎉 ΟΛΑ ΤΑ ΣΕΝΑΡΙΑ (UC7 & UC8) ΕΚΤΕΛΕΣΤΗΚΑΝ ΜΕ ΑΠΟΛΥΤΗ ΕΠΙΤΥΧΙΑ! 🎉");
            System.out.println("=======================================================");

        } catch (Exception e) {
            System.err.println("\n❌ ΣΦΑΛΜΑ κατά την εκτέλεση των σεναρίων: " + e.getMessage());
            e.printStackTrace();
        }
    }
}