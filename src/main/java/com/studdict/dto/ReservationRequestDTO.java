package com.studdict.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationRequestDTO {
    private String studentId;
    private Integer tableId;
    private LocalDate date;
    private LocalTime time;
    private int duration;
    private int minCapacity;
    private String subjectName; // Για το UC2 (Public)

    // Getters and Setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public Integer getTableId() { return tableId; }
    public void setTableId(Integer tableId) { this.tableId = tableId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public int getMinCapacity() { return minCapacity; }
    public void setMinCapacity(int minCapacity) { this.minCapacity = minCapacity; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
}