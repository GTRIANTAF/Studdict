package model;

public class PublicReservation extends Reservation{
    private StudySubject studySubject;

    public PublicReservation() {
        this.visibility = "Public";
    }
    public StudySubject getStudySubject() { return studySubject; }
    public void setStudySubject(StudySubject studySubject) { this.studySubject = studySubject; }
}
