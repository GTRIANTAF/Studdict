package com.studdict.controller;

import com.studdict.model.StudyTable;
import com.studdict.service.StudyTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "*") // Επιτρέπει στο TypeScript frontend να κάνει requests!
public class StudyTableController {

    @Autowired
    private StudyTableService tableService;

    // UC1: Εύρεση ελεύθερων τραπεζιών (Private)
    @GetMapping("/available")
    public List<StudyTable> getAvailableTables(
            @RequestParam Long venueId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            @RequestParam int duration,
            @RequestParam int minCapacity) {

        return tableService.getAvailableTables(venueId, date, time, duration, minCapacity);
    }

    // UC2: ΑΝΑΖΗΤΗΣΗ ΤΡΑΠΕΖΙΩΝ (Matchmaking)
    @GetMapping("/matchmaking")
    public List<StudyTable> getMatchmakingTables(
            @RequestParam Long venueId,
            @RequestParam String subjectName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            @RequestParam int duration,
            @RequestParam int minCapacity) {

        return tableService.findMatchmakingTables(venueId, subjectName, date, time, duration, minCapacity);
    }

    // Προσωρινό κλείδωμα (Soft Lock)
    @PostMapping("/{tableId}/lock")
    public boolean requestSoftLock(@PathVariable Integer tableId, @RequestParam String studentId) {
        return tableService.requestSoftLock(tableId, studentId);
    }

    @PostMapping("/{tableId}/unlock")
    public void releaseSoftLock(@PathVariable Integer tableId) {
        tableService.releaseSoftLock(tableId);
    }
}