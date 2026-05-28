package com.studdict.service;

import com.studdict.model.*;
import com.studdict.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReservationService {

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private StudyTableRepository studyTableRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private SubjectService subjectService;
    @Autowired private ParticipantRepository participantRepository;

    // USE CASE 1: ΙΔΙΩΤΙΚΗ ΚΡΑΤΗΣΗ (Private)
    // UML: PrivateReservation.create(...) & ReservationParticipant.register(...)
    public Long savePrivateReservation(String studentId, Integer tableId, LocalDate date, LocalTime time, int duration, int numberOfPeople) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        StudyTable table = studyTableRepository.findById(tableId).orElseThrow();

        // 1. Δημιουργία της Κράτησης
        PrivateReservation reservation = new PrivateReservation();
        reservation.setReservationDate(date);
        reservation.setStartTime(time);
        reservation.setDurationMinutes(duration);
        reservation.setNumberOfPeople(numberOfPeople);
        reservation.setStatus("CONFIRMED");
        reservation.setTable(table);

        //Generate το Invite Code (UML: InviteCode.generate())
        reservation.setLinkedQrCode(table.getQrCodeString());
        reservation = reservationRepository.save(reservation);

        // 2. Εγγραφή του Host (UML: ReservationParticipant.register)
        ReservationParticipant host = new ReservationParticipant();
        host.setReservationId(reservation.getReservationId());
        host.setStudentId(student.getStudentId());
        host.setRole("Host");
        participantRepository.save(host);

        // Οριστικοποίηση κλειδώματος τραπεζιού (Hard Lock)
        table.setIsAvailable(false);
        studyTableRepository.save(table);

        return reservation.getReservationId();
    }

    // USE CASE 2: ΔΗΜΟΣΙΑ ΚΡΑΤΗΣΗ (Public - Matchmaking)
    // UML: PublicReservation.create(...)
    public Long savePublicReservation(String studentId, Integer tableId, LocalDate date, LocalTime time, int duration, int numberOfPeople, String subjectName) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        StudyTable table = studyTableRepository.findById(tableId).orElseThrow();

        // Αν το τραπέζι είναι ήδη δεσμευμένο, ελέγχουμε αν υπάρχει ενεργή Public Κράτηση για να κάνουμε JOIN!
        if (!table.getIsAvailable()) {
            List<Reservation> activeReservations = reservationRepository.findByTable_TableIdAndStatus(tableId, "CONFIRMED");
            if (!activeReservations.isEmpty()) {
                Reservation existingRes = activeReservations.get(0);
                if (existingRes.getVisibility().equals("Public")) {
                    joinPublicReservation(existingRes.getReservationId(), studentId);
                    return existingRes.getReservationId();
                } else {
                    throw new RuntimeException("Το τραπέζι είναι ήδη δεσμευμένο σε Ιδιωτική κράτηση.");
                }
            }
        }

        // Εύρεση ή Δημιουργία του Μαθήματος
        StudySubject subject = subjectService.findOrCreate(subjectName);

        // Δημιουργία της ΝΕΑΣ Public Κράτησης (αν το τραπέζι ήταν ελεύθερο)
        PublicReservation reservation = new PublicReservation();
        reservation.setReservationDate(date);
        reservation.setStartTime(time);
        reservation.setDurationMinutes(duration);
        reservation.setNumberOfPeople(numberOfPeople);
        reservation.setStatus("CONFIRMED");
        reservation.setStudySubject(subject);
        reservation.setTable(table);

        reservation = reservationRepository.save(reservation);

        // Εγγραφή του Host
        ReservationParticipant host = new ReservationParticipant();
        host.setReservationId(reservation.getReservationId());
        host.setStudentId(student.getStudentId());
        host.setRole("Host");
        participantRepository.save(host);

        table.setIsAvailable(false);
        studyTableRepository.save(table);

        // Notify the Live Board!
        publishReservation(reservation.getReservationId(), subject.getSubjectId(), table.getId());

        return reservation.getReservationId();
    }

    // UML: Reservation.addParticipant() -> Για όταν ένας φοιτητής μπαίνει σε ΗΔΗ ΥΠΑΡΧΟΥΣΑ Public κράτηση (UC2)
    public void joinPublicReservation(Long reservationId, String studentId) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();

        if (!reservation.getVisibility().equals("Public")) {
            throw new RuntimeException("Δεν μπορείτε να μπείτε σε ιδιωτική κράτηση χωρίς κωδικό πρόσκλησης!");
        }

        // ΕΛΕΓΧΟΣ ΧΩΡΗΤΙΚΟΤΗΤΑΣ
        long currentParticipants = participantRepository.countByReservationId(reservationId);
        int maxCapacity = reservation.getTable().getCapacity();

        if (currentParticipants >= maxCapacity) {
            throw new RuntimeException("Το τραπέζι είναι ήδη πλήρες!");
        }

        ReservationParticipant guest = new ReservationParticipant();
        guest.setReservationId(reservation.getReservationId());
        guest.setStudentId(student.getStudentId());
        guest.setRole("Guest");

        participantRepository.save(guest);
    }

    public void discardReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Η κράτηση δεν βρέθηκε"));

        // Αλλαγή κατάστασης
        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);

        // Απελευθέρωση του Τραπεζιού
        StudyTable table = reservation.getTable();
        if (table != null) {
            table.setIsAvailable(true);
            table.setSoftLockedBy(null);
            table.setSoftLockExpiration(null);
            studyTableRepository.save(table);
        }
    }

    // --- LIVE BOARD METHODS ---

    // Matches the UML Sequence Diagram exactly
    public void publishReservation(Long reservationId, Long subjectId, Integer tableId) {
        System.out.println("✅ [LiveBoard] Published! Reservation: " + reservationId + ", Subject: " + subjectId + ", Table: " + tableId);
    }

    public List<Reservation> getPublishedReservations() {
        return reservationRepository.findByVisibilityAndStatus("Public", "CONFIRMED");
    }

    public List<Reservation> getReservationsByStudent(String studentId) {
        return reservationRepository.findActiveReservationsByStudent(studentId);
    }
}
