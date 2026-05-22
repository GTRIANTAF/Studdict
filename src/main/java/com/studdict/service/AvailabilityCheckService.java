package com.studdict.service;

import com.studdict.model.StudyTable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/availability")
public class AvailabilityCheckService {

    private final StudyTableService studyTableService;

    public AvailabilityCheckService(StudyTableService studyTableService) {
        this.studyTableService = studyTableService;
    }

    @GetMapping("/tables")
    public List<StudyTable> checkAvailability(
            @RequestParam Long venueId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam int duration,
            @RequestParam int minCapacity) {

        return studyTableService.getAvailableTables(venueId, date, time, duration, minCapacity);
    }
}
