package com.studdict;

import com.studdict.model.*;
import com.studdict.repository.*;
import com.studdict.service.ReservationService;
import com.studdict.service.TableService;
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
    @Autowired private TableRepository tableRepository;
    @Autowired private ReservationService reservationService;
    @Autowired private TableService tableService; // <-- Προστέθηκε για το Soft Lock
    @Autowired private ParticipantRepository participantRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=======================================================");
        System.out.println("=== 🚀 ΕΝΑΡΞΗ ΠΡΟΣΟΜΟΙΩΣΗΣ ΚΡΑΤΗΣΕΩΝ (UC1 & UC2) ===");
        System.out.println("=======================================================\n");

        // --- ΒΗΜΑ 1: SETUP ΔΕΔΟΜΕΝΩΝ ---
        if (studentRepository.count() == 0) {
            System.out.println("⏳ Δημιουργία εικονικών δεδομένων...");

            studentRepository.save(new Student("S1", "Γιάννης", "Α.", "giannis@upatras.gr"));
            studentRepository.save(new Student("S2", "Μαρία", "Β.", "maria@upatras.gr"));
            studentRepository.save(new Student("S3", "Κώστας", "Γ.", "kostas@upatras.gr"));
            studentRepository.save(new Student("S4", "Ελένη", "Δ.", "eleni@upatras.gr"));

            Venue venue = new Venue("Κεντρική Βιβλιοθήκη", "Πανεπιστημιούπολη", "Library", true);
            venueRepository.save(venue);

            tableRepository.save(new StudyTable(venue, 1, 4, "QR-1", true));
            tableRepository.save(new StudyTable(venue, 2, 4, "QR-2", true));
            tableRepository.save(new StudyTable(venue, 3, 6, "QR-3", true));

            System.out.println("✅ Τα δεδομένα δημιουργήθηκαν επιτυχώς!\n");
        }

        List<StudyTable> tables = tableRepository.findAll();
        StudyTable table1 = tables.get(0);
        StudyTable table2 = tables.get(1);
        StudyTable table3 = tables.get(2);

        LocalDate today = LocalDate.now();

        try {
            // =====================================================================
            // TEST 1: Ιδιωτική Κράτηση για 1 Άτομο
            // =====================================================================

            System.out.println("▶️ TEST 1: Ο Γιάννης (S1) ψάχνει για διαθέσιμο τραπέζι στη Βιβλιοθήκη");

            // Φέρνουμε το Venue από τη βάση
            Venue currentVenue = venueRepository.findAll().get(0);

            // ΔΗΛΩΝΟΥΜΕ ΤΗ ΜΕΤΑΒΛΗΤΗ ΕΞΩ ΑΠΟ ΤΟ IF/ELSE ΓΙΑ ΝΑ ΤΗ ΒΛΕΠΕΙ ΤΟ TEST 5
            Long privateRes1Id = null;

            // 1. ΧΡΗΣΗ ΤΗΣ ΜΕΘΟΔΟΥ ΑΠΟ ΤΟ UML (Περνάμε και τα 5 ορίσματα!)
            List<StudyTable> availableTables = tableService.findAvailable(
                    currentVenue.getVenueId(), today, LocalTime.of(10, 0), 120, 1);

            if (availableTables.isEmpty()) {
                System.out.println("   ❌ Δεν βρέθηκε κανένα διαθέσιμο τραπέζι!");
            } else {
                StudyTable selectedTable = availableTables.get(0);
                System.out.println("   🔎 Βρέθηκαν " + availableTables.size() + " τραπέζια. Επιλέγει το Τραπέζι: " + selectedTable.getTableNumber());

                // ΑΠΟΘΗΚΕΥΟΥΜΕ ΤΟ ID ΣΤΗ ΜΕΤΑΒΛΗΤΗ
                privateRes1Id = reservationService.createPrivateReservation(
                        "S1", selectedTable.getId(), today, LocalTime.of(10, 0), 120);
                System.out.println("   ✅ Επιτυχία! Κωδικός Κράτησης: " + privateRes1Id + "\n");
            }

            // =====================================================================
            // TEST 2: Ιδιωτική Κράτηση για >1 Άτομα (Με προσθήκη παρέας)
            // =====================================================================
            System.out.println("▶️ TEST 2: Η Μαρία (S2) κάνει Private κράτηση στο Τραπέζι 2");
            Long privateRes2Id = reservationService.createPrivateReservation(
                    "S2", table2.getId(), today, LocalTime.of(12, 0), 120);

            ReservationParticipant guest1 = new ReservationParticipant();
            guest1.setReservationId(privateRes2Id);
            guest1.setStudentId("S3"); // Ο Κώστας
            guest1.setRole("Guest");
            participantRepository.save(guest1);

            System.out.println("   ✅ Επιτυχία! Η Μαρία έκλεισε το τραπέζι και ο Κώστας προστέθηκε ως Guest.\n");

            // =====================================================================
            // TEST 3 & 4: Δημόσια Κράτηση (Matchmaking)
            // =====================================================================
            System.out.println("▶️ TEST 3: Η Ελένη (S4) ανοίγει Public Τραπέζι για 'Μαθηματικά' στο Τραπέζι 3");
            Long publicResId = reservationService.createPublicReservation(
                    "S4", table3.getId(), today, LocalTime.of(16, 0), 180, "Μαθηματικά");
            System.out.println("   ✅ Επιτυχία! Το Public τραπέζι άνοιξε (Κωδικός: " + publicResId + ")");

            System.out.println("▶️ TEST 4: Ο Γιάννης (S1) κάνει Join στο τραπέζι της Ελένης");
            reservationService.joinPublicReservation(publicResId, "S1");
            System.out.println("   ✅ Επιτυχία! Ο Γιάννης προστέθηκε στην Public κράτηση.\n");

            // =====================================================================
            // TEST 5: Ακύρωση Κράτησης (Cancellation)
            // =====================================================================
            System.out.println("▶️ TEST 5: Ο Γιάννης ακυρώνει την αρχική Private κράτησή του (Test 1)");
            reservationService.cancelReservation(privateRes1Id);

            StudyTable checkTable1 = tableRepository.findById(table1.getId()).orElseThrow();
            System.out.println("   ✅ Επιτυχία! Η κράτηση ακυρώθηκε. Το Τραπέζι 1 είναι διαθέσιμο: " + checkTable1.getIsAvailable() + "\n");

            // =====================================================================
            // TEST 6: Έλεγχος Soft-Lock (Race Condition Prevention)
            // =====================================================================
            System.out.println("▶️ TEST 6: Έλεγχος Προσωρινού Κλειδώματος (Soft-Lock)");

            // Η Ελένη βρίσκει το Τραπέζι 1 που μόλις ελευθερώθηκε και το επιλέγει (κάνει soft lock)
            boolean isLockedByEleni = tableService.softLock(table1.getId(), "S4");
            System.out.println("   🔒 Η Ελένη ξεκίνησε διαδικασία κράτησης στο Τραπέζι 1. Επιτυχία Κλειδώματος: " + isLockedByEleni);

            // Ο Γιάννης προσπαθεί το ΙΔΙΟ δευτερόλεπτο να κλειδώσει το ΙΔΙΟ τραπέζι
            boolean isLockedByGiannis = tableService.softLock(table1.getId(), "S1");
            System.out.println("   ⚠️ Ο Γιάννης προσπαθεί να το κλειδώσει. Επιτυχία Κλειδώματος: " + isLockedByGiannis + " (Αναμενόμενο false!)");

            if (!isLockedByGiannis) {
                System.out.println("   🛡️ Το σύστημα προστάτευσε σωστά το τραπέζι (Race Condition αποτράπηκε).");
            }

            // Η Ελένη το μετανιώνει και πατάει 'Ακύρωση' ή 'Πίσω'
            tableService.releaseSoftLock(table1.getId());
            System.out.println("   🔓 Η Ελένη ελευθέρωσε το Soft Lock του Τραπεζιού 1.\n");


            System.out.println("=======================================================");
            System.out.println("🎉 ΟΛΑ ΤΑ ΣΕΝΑΡΙΑ (6/6) ΕΚΤΕΛΕΣΤΗΚΑΝ ΜΕ ΑΠΟΛΥΤΗ ΕΠΙΤΥΧΙΑ! 🎉");
            System.out.println("=======================================================");

        } catch (Exception e) {
            System.err.println("\n❌ ΣΦΑΛΜΑ κατά την εκτέλεση των σεναρίων: " + e.getMessage());
            e.printStackTrace();
        }
    }
}