package com.studdict.mobile.model;

public class ReservationRequest {
    private final String studentId;
    private final int tableId;
    private final String reservationDate;
    private final String startTime;
    private final int durationMinutes;
    private final int numberOfPeople;
    private final String subjectName;

    public ReservationRequest(
            String studentId,
            int tableId,
            String reservationDate,
            String startTime,
            int durationMinutes,
            int numberOfPeople,
            String subjectName
    ) {
        this.studentId = studentId;
        this.tableId = tableId;
        this.reservationDate = reservationDate;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.numberOfPeople = numberOfPeople;
        this.subjectName = subjectName;
    }
}