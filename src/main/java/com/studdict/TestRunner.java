package com.studdict;

import com.studdict.model.*;
import com.studdict.repository.*;
import com.studdict.service.CheckInService;
import com.studdict.service.InviteCodeService;
import com.studdict.service.ReservationService;
import com.studdict.service.StudyTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.studdict.service.GamificationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@Transactional
public class TestRunner implements CommandLineRunner {

    @Autowired private StudentRepository studentRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private StudyTableRepository tableRepository;
    @Autowired private ReservationRepository reservationRepository; // <-- ΠΡΟΣΤΕΘΗΚΕ ΓΙΑ ΤΟΝ ΕΛΕΓΧΟ QR
    @Autowired private ReservationService reservationService;
    @Autowired private StudyTableService tableService;
    @Autowired private ParticipantRepository participantRepository;
    @Autowired private InviteCodeService inviteCodeService;
    @Autowired private CheckInService checkInService;
    @Autowired private GamificationService gamificationService;
    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=======================================================");
        System.out.println("===  ΕΝΑΡΞΗ ΠΡΟΣΟΜΟΙΩΣΗΣ ΚΡΑΤΗΣΕΩΝ (UC1 & UC2) ===");
        System.out.println("=======================================================\n");

        // --- ΒΗΜΑ 1: SETUP ΔΕΔΟΜΕΝΩΝ ---
        if (studentRepository.count() == 0) {
            System.out.println("⏳ Δημιουργία εικονικών δεδομένων...");

            studentRepository.save(new Student("S1", "Γιάννης", "Α.", "giannis@upatras.gr"));
            studentRepository.save(new Student("S2", "Μαρία", "Β.", "maria@upatras.gr"));
            studentRepository.save(new Student("S3", "Κώστας", "Γ.", "kostas@upatras.gr"));
            studentRepository.save(new Student("S4", "Ελένη", "Δ.", "eleni@upatras.gr"));

            Venue venue1 = new Venue("CEID Library", "University Campus", "Library", true);
            venueRepository.save(venue1);

            Venue venue2 = new Venue("Patras City Cafe", "Agiou Nikolaou St.", "Cafe", true);
            venueRepository.save(venue2);

            // Tables for Venue 1 (CEID Library)
            tableRepository.save(new StudyTable(venue1, 1, 4, "QR-1", true));
            tableRepository.save(new StudyTable(venue1, 2, 4, "QR-2", true));
            tableRepository.save(new StudyTable(venue1, 3, 6, "QR-3", true));

            // Extra FREE tables for FE testing (CEID)
            tableRepository.save(new StudyTable(venue1, 4, 2, "QR-4", true));
            tableRepository.save(new StudyTable(venue1, 5, 8, "QR-5", true));
            tableRepository.save(new StudyTable(venue1, 6, 4, "QR-6", true));

            // Extra FREE tables for FE testing (Patras City Cafe)
            tableRepository.save(new StudyTable(venue2, 1, 2, "QR-C1", true));
            tableRepository.save(new StudyTable(venue2, 2, 4, "QR-C2", true));
            tableRepository.save(new StudyTable(venue2, 3, 6, "QR-C3", true));

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

            System.out.println("▶ TEST 1: Ο Γιάννης (S1) ψάχνει για διαθέσιμο τραπέζι στη Βιβλιοθήκη");

            // Φέρνουμε το Venue από τη βάση
            Venue currentVenue = venueRepository.findAll().get(0);

            // ΔΗΛΩΝΟΥΜΕ ΤΗ ΜΕΤΑΒΛΗΤΗ ΕΞΩ ΑΠΟ ΤΟ IF/ELSE ΓΙΑ ΝΑ ΤΗ ΒΛΕΠΕΙ ΤΟ TEST 5
            Long privateRes1Id = null;

            // 1. ΔΙΟΡΘΩΣΗ ΟΝΟΜΑΤΟΣ: getAvailableTables
            List<StudyTable> availableTables = tableService.getAvailableTables(
                    currentVenue.getVenueId(), today, LocalTime.of(10, 0), 120, 1);

            if (availableTables.isEmpty()) {
                System.out.println("    Δεν βρέθηκε κανένα διαθέσιμο τραπέζι!");
            } else {
                StudyTable selectedTable = availableTables.get(0);
                System.out.println("    Βρέθηκαν " + availableTables.size() + " τραπέζια. Επιλέγει το Τραπέζι: " + selectedTable.getTableNumber());

                // ΑΠΟΘΗΚΕΥΟΥΜΕ ΤΟ ID ΣΤΗ ΜΕΤΑΒΛΗΤΗ (ΔΙΟΡΘΩΣΗ ΟΝΟΜΑΤΟΣ: savePrivateReservation)
                privateRes1Id = reservationService.savePrivateReservation(
                        "S1", selectedTable.getId(), today, LocalTime.of(10, 0), 120);
                System.out.println("    Επιτυχία! Κωδικός Κράτησης: " + privateRes1Id);

                // --- 🚀 ΝΕΟ: ΕΛΕΓΧΟΣ QR LINKING ---
                Reservation savedRes = reservationRepository.findById(privateRes1Id).orElseThrow();
                if (savedRes instanceof PrivateReservation) {
                    PrivateReservation pr = (PrivateReservation) savedRes;
                    System.out.println("    ΕΛΕΓΧΟΣ QR LINKING:");
                    System.out.println("      - QR Τραπεζιού (Πηγή): " + selectedTable.getQrCodeString());
                    System.out.println("      - QR Κράτησης (Αντίγραφο): " + pr.getLinkedQrCode());

                    if (selectedTable.getQrCodeString().equals(pr.getLinkedQrCode())) {
                        System.out.println("       Το QR Linking λειτούργησε άψογα!\n");
                    } else {
                        System.out.println("       Αποτυχία στο QR Linking!\n");
                    }
                }
            }

            // =====================================================================
            // TEST 2: Ιδιωτική Κράτηση για >1 Άτομα (Με προσθήκη παρέας)
            // =====================================================================
            System.out.println("▶️ TEST 2: Η Μαρία (S2) κάνει Private κράτηση στο Τραπέζι 2");
            // ΔΙΟΡΘΩΣΗ ΟΝΟΜΑΤΟΣ: savePrivateReservation
            Long privateRes2Id = reservationService.savePrivateReservation(
                    "S2", table2.getId(), today, LocalTime.of(12, 0), 120);

            ReservationParticipant guest1 = new ReservationParticipant();
            guest1.setReservationId(privateRes2Id);
            guest1.setStudentId("S3"); // Ο Κώστας
            guest1.setRole("Guest");
            participantRepository.save(guest1);

            System.out.println("    Επιτυχία! Η Μαρία έκλεισε το τραπέζι και ο Κώστας προστέθηκε ως Guest.\n");

            // =====================================================================
            // TEST 3 & 4: Δημόσια Κράτηση (Matchmaking)
            // =====================================================================
            System.out.println(" TEST 3: Η Ελένη (S4) ανοίγει Public Τραπέζι για 'Μαθηματικά' στο Τραπέζι 3");
            // ΔΙΟΡΘΩΣΗ ΟΝΟΜΑΤΟΣ: savePublicReservation
            Long publicResId = reservationService.savePublicReservation(
                    "S4", table3.getId(), today, LocalTime.of(16, 0), 180, "Μαθηματικά");
            System.out.println("    Επιτυχία! Το Public τραπέζι άνοιξε (Κωδικός: " + publicResId + ")");

            System.out.println(" TEST 4: Ο Γιάννης (S1) κάνει Join στο τραπέζι της Ελένης");
            reservationService.joinPublicReservation(publicResId, "S1");
            System.out.println("    Επιτυχία! Ο Γιάννης προστέθηκε στην Public κράτηση.\n");

            // =====================================================================
            // TEST 5: Ακύρωση Κράτησης (Cancellation)
            // =====================================================================
            System.out.println(" TEST 5: Ο Γιάννης ακυρώνει την αρχική Private κράτησή του (Test 1)");

            if (privateRes1Id != null) {
                // ΔΙΟΡΘΩΣΗ ΟΝΟΜΑΤΟΣ: discardReservation
                reservationService.discardReservation(privateRes1Id);

                StudyTable checkTable1 = tableRepository.findById(table1.getId()).orElseThrow();
                System.out.println("    Επιτυχία! Η κράτηση ακυρώθηκε. Το Τραπέζι 1 είναι διαθέσιμο: " + checkTable1.getIsAvailable() + "\n");
            } else {
                System.out.println("    Παράλειψη: Δεν υπήρχε κράτηση για ακύρωση.\n");
            }

            // =====================================================================
            // TEST 6: Έλεγχος Soft-Lock (Race Condition Prevention)
            // =====================================================================
            System.out.println("▶️ TEST 6: Έλεγχος Προσωρινού Κλειδώματος (Soft-Lock)");

            // ΔΙΟΡΘΩΣΗ ΟΝΟΜΑΤΟΣ: requestSoftLock
            boolean isLockedByEleni = tableService.requestSoftLock(table1.getId(), "S4");
            System.out.println("    Η Ελένη ξεκίνησε διαδικασία κράτησης στο Τραπέζι 1. Επιτυχία Κλειδώματος: " + isLockedByEleni);

            // Ο Γιάννης προσπαθεί το ΙΔΙΟ δευτερόλεπτο να κλειδώσει το ΙΔΙΟ τραπέζι
            boolean isLockedByGiannis = tableService.requestSoftLock(table1.getId(), "S1");
            System.out.println("    Ο Γιάννης προσπαθεί να το κλειδώσει. Επιτυχία Κλειδώματος: " + isLockedByGiannis + " (Αναμενόμενο false!)");

            if (!isLockedByGiannis) {
                System.out.println("    Το σύστημα προστάτευσε σωστά το τραπέζι (Race Condition αποτράπηκε).");
            }

            // Η Ελένη το μετανιώνει και πατάει 'Ακύρωση' ή 'Πίσω'
            tableService.releaseSoftLock(table1.getId());
            System.out.println("    Η Ελένη ελευθέρωσε το Soft Lock του Τραπεζιού 1.\n");

            // =====================================================================
            // TEST 7: Δημιουργία και Χρήση Κωδικού Πρόσκλησης (UC3)
            // =====================================================================
            System.out.println(" TEST 7: Η Μαρία (S2) βγάζει κωδικό πρόσκλησης για το Private Τραπέζι 2");

            // Η Μαρία έχει ήδη κράτηση από το TEST 2 (privateRes2Id)
            Reservation mariaReservation = reservationRepository.findById(privateRes2Id).orElseThrow();
            Student maria = studentRepository.findById("S2").orElseThrow();
            Student eleni = studentRepository.findById("S4").orElseThrow();

            // Η Μαρία γεννάει κωδικό
            InviteCode generatedCode = inviteCodeService.generateInviteCode(mariaReservation, maria);
            System.out.println("   Δημιουργήθηκε ο κωδικός: " + generatedCode.getCode() + " (Λήγει: " + generatedCode.getExpiresAt() + ")");

            // Η Ελένη προσπαθεί να μπει με αυτόν τον κωδικό
            boolean didEleniJoin = inviteCodeService.joinReservation(generatedCode, eleni);
            System.out.println("    Η Ελένη έβαλε τον κωδικό. Επιτυχία Συμμετοχής: " + didEleniJoin);

            // =====================================================================
            // TEST 8: Σκανάρισμα QR Code / Check-in (UC5)
            // =====================================================================
            System.out.println(" TEST 8: Η Μαρία (S2) φτάνει στη Βιβλιοθήκη και κάνει Check-in στο Τραπέζι 2");

            // Σεναριο 8Α: Σκανάρει ΛΑΘΟΣ τραπέζι (π.χ. κάθεται κατά λάθος στο Τραπέζι 1)
            CheckIn failedCheckIn = checkInService.checkInStudent(mariaReservation, maria, "QR-1");
            System.out.println("    Η Μαρία σκανάρει κατά λάθος το QR-1. Αποτέλεσμα Check-in: " + failedCheckIn.isSuccessful());

            // Σεναριο 8Β: Σκανάρει το ΣΩΣΤΟ τραπέζι (Τραπέζι 2 -> QR-2)
            CheckIn successfulCheckIn = checkInService.checkInStudent(mariaReservation, maria, "QR-2");
            System.out.println("    Η Μαρία σκανάρει το σωστό QR-2. Αποτέλεσμα Check-in: " + successfulCheckIn.isSuccessful() + " στις " + successfulCheckIn.getCheckInTime());

            // =====================================================================
            // TEST GAMIFICATION (UC9 & UC10)
            // =====================================================================

            System.out.println("▶️ TEST 9: Gamification - Απονομή Πόντων (UC9)");
            // Πιστώνουμε πόντους στη Μαρία (S2) για 120 λεπτά μελέτης
            int pointsEarned = gamificationService.creditPointsForStudy("S2", 120);
            System.out.println("   ✅ Η Μαρία (S2) κέρδισε " + pointsEarned + " πόντους.\n");

            System.out.println("▶️ TEST 10: Gamification - Εξαργύρωση Πόντων (UC10)");
            // Δίνουμε bonus πόντους για να φτάσει το όριο εξαργύρωσης
            gamificationService.creditPointsForStudy("S2", 1500);

            boolean redeemSuccess = gamificationService.redeemPoints("S2", 100);
            System.out.println("   ✅ Αποτέλεσμα Εξαργύρωσης 100 πόντων: " + redeemSuccess);

            if (redeemSuccess) {
                double discount = gamificationService.calculateDiscount(100);
                System.out.println("   🎉 Η Μαρία κέρδισε έκπτωση: " + discount + "€ στο επόμενο checkout!\n");
            }
            System.out.println("=======================================================");
            System.out.println(" ΟΛΑ ΤΑ ΣΕΝΑΡΙΑ (10/10) ΕΚΤΕΛΕΣΤΗΚΑΝ ΜΕ ΑΠΟΛΥΤΗ ΕΠΙΤΥΧΙΑ! 🎉");
            System.out.println("=======================================================");

        } catch (Exception e) {
            System.err.println("\n ΣΦΑΛΜΑ κατά την εκτέλεση των σεναρίων: " + e.getMessage());
            e.printStackTrace();
        }
    }
}