package com.studdict.mobile.model;

import com.google.gson.annotations.SerializedName;

public class StudySubject {
    @SerializedName("subjectId")
    private Long subjectId;

    @SerializedName("name")
    private String name;

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
