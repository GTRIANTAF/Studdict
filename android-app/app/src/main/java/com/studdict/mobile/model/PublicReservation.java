package com.studdict.mobile.model;

import com.google.gson.annotations.SerializedName;

public class PublicReservation {
    @SerializedName("reservationId")
    private Long reservationId;

    @SerializedName("studySubject")
    private StudySubject studySubject;

    @SerializedName("table")
    private StudyTable table;

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public StudySubject getStudySubject() { return studySubject; }
    public void setStudySubject(StudySubject studySubject) { this.studySubject = studySubject; }

    public StudyTable getTable() { return table; }
    public void setTable(StudyTable table) { this.table = table; }
}
