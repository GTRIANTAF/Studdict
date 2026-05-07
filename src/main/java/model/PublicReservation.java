package model;

import jakarta.persistence.*;

@Entity
@Table(name = "public_reservations")
public class PublicReservation extends Reservation {

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private StudySubject studySubject; // Optional subject for matchmaking

    public PublicReservation() {
        this.visibility = "Public";
    }

    public StudySubject getStudySubject() { return studySubject; }
    public void setStudySubject(StudySubject studySubject) { this.studySubject = studySubject; }
}