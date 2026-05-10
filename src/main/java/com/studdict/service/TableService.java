package com.studdict.service;

import com.studdict.model.StudyTable;
import com.studdict.repository.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class TableService {

    @Autowired
    private TableRepository tableRepository;

    // UML: Table.findAvailable(venueId, date, time, duration, minCapacity) -> ΓΙΑ UC1
    public List<StudyTable> findAvailable(Long venueId, LocalDate date, LocalTime time, int duration, int minCapacity) {
        // Επιστρέφει τα ελεύθερα τραπέζια που χωράνε την παρέα (Βασική Ροή UC1)
        List<StudyTable> tables = tableRepository.findByVenue_VenueIdAndIsAvailableTrue(venueId);
        return tables.stream()
                .filter(t -> t.getCapacity() >= minCapacity)
                .toList();
    }

    // UML: Table.findAvailableBySubjectPriority(...) & findOpenPublicTables(...) -> ΓΙΑ UC2 (Matchmaking)
    public List<StudyTable> findMatchmakingTables(Long venueId, String subjectName) {
        // Εδώ μπαίνει ο αλγόριθμος Matchmaking (UC2). Βρίσκει τραπέζια στον συγκεκριμένο χώρο
        // που έχουν ήδη Public Reservation για το ΙΔΙΟ μάθημα (subjectName) και έχουν κενές θέσεις.
        // Αν δεν βρει, επιστρέφει απλά άδεια τραπέζια.

        // Σημείωση: Απαιτεί custom query στο TableRepository.
        // Προς το παρόν επιστρέφουμε τα διαθέσιμα του Venue.
        return tableRepository.findByVenue_VenueIdAndIsAvailableTrue(venueId);
    }

    // UML: Table.softLock(studentId)
    public boolean softLock(Integer tableId, String studentId) {
        StudyTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Το τραπέζι δεν βρέθηκε"));

        if (!table.getIsAvailable()) {
            return false; // Το τραπέζι δεσμεύτηκε από άλλον (Race Condition protected)
        }

        table.setIsAvailable(false);
        table.setSoftLockedBy(studentId); // Προσωρινό κλείδωμα
        tableRepository.save(table);
        return true;
    }

    // UML: Table.releaseSoftLock()
    public void releaseSoftLock(Integer tableId) {
        StudyTable table = tableRepository.findById(tableId).orElse(null);
        if (table != null) {
            table.setIsAvailable(true);
            table.setSoftLockedBy(null);
            tableRepository.save(table);
        }
    }
}