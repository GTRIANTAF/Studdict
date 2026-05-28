package com.studdict.service;

import com.studdict.model.Reservation;
import com.studdict.model.StudyTable;
import com.studdict.repository.ParticipantRepository;
import com.studdict.repository.ReservationRepository;
import com.studdict.repository.StudyTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class StudyTableService {

    @Autowired private StudyTableRepository studyTableRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private ParticipantRepository participantRepository;

    // UML: Table.findAvailable(venueId, date, time, duration, minCapacity) -> ΓΙΑ UC1
    public List<StudyTable> getAvailableTables(Long venueId, LocalDate date, LocalTime time, int duration, int minCapacity) {
        List<StudyTable> tables = studyTableRepository.findByVenue_VenueIdAndIsAvailableTrue(venueId);
        LocalTime requestedEnd = time.plusMinutes(duration);

        return tables.stream()
                .filter(t -> t.getCapacity() >= minCapacity)
                .filter(t -> {
                    List<Reservation> existingReservations = reservationRepository.findByTable_TableIdAndStatus(t.getId(), "CONFIRMED");
                    for (Reservation r : existingReservations) {
                        if (r.getReservationDate() != null && r.getReservationDate().equals(date)) {
                            LocalTime rStart = r.getStartTime();
                            LocalTime rEnd = rStart.plusMinutes(r.getDurationMinutes());
                            
                            // Overlap condition: requestedStart < rEnd AND requestedEnd > rStart
                            if (time.isBefore(rEnd) && requestedEnd.isAfter(rStart)) {
                                return false; // Not available
                            }
                        }
                    }
                    return true;
                })
                .toList();
    }

    // UML: Table.findAvailableBySubjectPriority(...) & findOpenPublicTables(...) -> ΓΙΑ UC2 (Matchmaking)
    public List<StudyTable> findMatchmakingTables(Long venueId, String subjectName) {
        List<StudyTable> recommendedTables = new ArrayList<>();

        // 1. ΑΝΑΖΗΤΗΣΗ ΓΙΑ MATCH (Υπάρχουσες Δημόσιες Κρατήσεις για το ίδιο μάθημα)
        List<Reservation> matchingReservations = reservationRepository.findMatchmakingReservations(venueId, subjectName);

        for (Reservation res : matchingReservations) {
            long currentParticipants = participantRepository.countByReservationId(res.getReservationId());

            // Αν το τραπέζι έχει ακόμα κενές θέσεις, το προτείνουμε!
            if (currentParticipants < res.getTable().getCapacity()) {
                recommendedTables.add(res.getTable());
            }
        }

        // 2. FALLBACK (Εναλλακτική)
        // Αν δεν βρέθηκε κανένα τραπέζι με το ίδιο μάθημα, επιστρέφουμε όλα τα εντελώς ελεύθερα τραπέζια
        if (recommendedTables.isEmpty()) {
            return studyTableRepository.findByVenue_VenueIdAndIsAvailableTrue(venueId);
        }

        return recommendedTables;
    }

    // UML: Table.softLock(studentId)
    public boolean requestSoftLock(Integer tableId, String studentId) {
        StudyTable table = studyTableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Το τραπέζι δεν βρέθηκε"));

        if (!table.getIsAvailable()) {
            return false; // Το τραπέζι δεσμεύτηκε από άλλον (Race Condition protected)
        }

        table.setIsAvailable(false);
        table.setSoftLockedBy(studentId); // Προσωρινό κλείδωμα
        studyTableRepository.save(table);
        return true;
    }

    // UML: Table.releaseSoftLock()
    public void releaseSoftLock(Integer tableId) {
        StudyTable table = studyTableRepository.findById(tableId).orElse(null);
        if (table != null) {
            table.setIsAvailable(true);
            table.setSoftLockedBy(null);
            studyTableRepository.save(table);
        }
    }
}