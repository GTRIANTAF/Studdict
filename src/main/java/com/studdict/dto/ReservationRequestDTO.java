package com.studdict.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationRequestDTO {
    private String studentId;
    private Integer tableId;

    private LocalDate date;
    private LocalDate reservationDate;

    private LocalTime time;
    private LocalTime startTime;

    private int duration;
    private int durationMinutes;

    private int minCapacity;
    private int numberOfPeople;

    private String subjectName;

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public LocalDate getDate() {
        return reservationDate != null ? reservationDate : date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public LocalTime getTime() {
        return startTime != null ? startTime : time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return durationMinutes != 0 ? durationMinutes : duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getNumberOfPeople() {
        return numberOfPeople != 0 ? numberOfPeople : minCapacity;
    }

    public void setMinCapacity(int minCapacity) {
        this.minCapacity = minCapacity;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}