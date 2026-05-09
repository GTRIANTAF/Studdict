package com.studdict.model;

import jakarta.persistence.*;

@Entity
@Table(name = "study_subjects")
public class StudySubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    public StudySubject() {}

    public StudySubject(String name) {
        this.name = name;
    }

    // Getters
    public Long getSubjectId() {
        return subjectId;
    }

    public String getName() {
        return name;
    }

    // Setters ()
    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public void setName(String name) {
        this.name = name;
    }
}