package com.studdict.mobile.model;

public class ReservationRequest {
    private final String studentId;
    private final int tableId;
    private final String date;
    private final String time;
    private final int duration;
    private final int minCapacity;
    private final String subjectName;

    public ReservationRequest(
            String studentId,
            int tableId,
            String date,
            String time,
            int duration,
            int minCapacity,
            String subjectName
    ) {
        this.studentId = studentId;
        this.tableId = tableId;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.minCapacity = minCapacity;
        this.subjectName = subjectName;
    }
}
