package com.studdict.service;

import com.studdict.model.*;
import com.studdict.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReservationService {

    @Autowired private ReservationRepository reservationRepository;
    @Autowired private TableRepository tableRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private SubjectService subjectService;
    @Autowired private ParticipantRepository participantRepository;

    // USE CASE 1: ΙΔΙΩΤΙΚΗ ΚΡΑΤΗΣΗ (Private)
    // UML: PrivateReservation.create(...) & ReservationParticipant.register(...)
    public Long createPrivateReservation(String studentId, Integer tableId, LocalDate date, LocalTime time, int duration) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        StudyTable table = tableRepository.findById(tableId).orElseThrow();

        // 1. Δημιουργία της Κράτησης
        PrivateReservation reservation = new PrivateReservation();
        reservation.setReservationDate(date);
        reservation.setStartTime(time);
        reservation.setDurationMinutes(duration);
        reservation.setStatus("CONFIRMED");
        reservation.setTable(table);

        //Generate το Invite Code (UML: InviteCode.generate())
        reservation = reservationRepository.save(reservation);

        // 2. Εγγραφή του Host (UML: ReservationParticipant.register)
        ReservationParticipant host = new ReservationParticipant();
        host.setReservationId(reservation.getReservationId());
        host.setStudentId(student.getStudentId());
        host.setRole("Host");
        participantRepository.save(host);

        // Οριστικοποίηση κλειδώματος τραπεζιού (Hard Lock)
        table.setIsAvailable(false);
        tableRepository.save(table);

        return reservation.getReservationId();
    }

    // USE CASE 2: ΔΗΜΟΣΙΑ ΚΡΑΤΗΣΗ (Public - Matchmaking)
    // UML: PublicReservation.create(...)
    public Long createPublicReservation(String studentId, Integer tableId, LocalDate date, LocalTime time, int duration, String subjectName) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        StudyTable table = tableRepository.findById(tableId).orElseThrow();

        // Εύρεση ή Δημιουργία του Μαθήματος
        StudySubject subject = subjectService.findOrCreate(subjectName);

        // Δημιουργία της Public Κράτησης
        PublicReservation reservation = new PublicReservation();
        reservation.setReservationDate(date);
        reservation.setStartTime(time);
        reservation.setDurationMinutes(duration);
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
        tableRepository.save(table);

        return reservation.getReservationId();
    }

    // UML: Reservation.addParticipant() -> Για όταν ένας φοιτητής μπαίνει σε ΗΔΗ ΥΠΑΡΧΟΥΣΑ Public κράτηση (UC2)
    public void joinPublicReservation(Long reservationId, String studentId) {
        Student student = studentRepository.findById(studentId).orElseThrow();
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow();

        if (!reservation.getVisibility().equals("Public")) {
            throw new RuntimeException("Δεν μπορείτε να μπείτε σε ιδιωτική κράτηση χωρίς κωδικό πρόσκλησης!");
        }

        ReservationParticipant guest = new ReservationParticipant();
        guest.setReservationId(reservation.getReservationId());
        guest.setStudentId(student.getStudentId());
        guest.setRole("Guest");

        // Εγγραφή του νέου μέλους στην παρέα
        participantRepository.save(guest);
    }

    public void cancelReservation(Long reservationId) {
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
            tableRepository.save(table);
        }
    }
}