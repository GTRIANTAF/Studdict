package com.studdict.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "public_reservations")
public class PublicReservation extends Reservation {

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private StudySubject studySubject; // Optional subject for matchmaking

    public PublicReservation() {
        this.visibility = "Public";
    }

    public void setStudySubject(StudySubject studySubject) { this.studySubject = studySubject; }
}