package model;

public class StudySubject {
    private String subjectId;
    private String name;
    public StudySubject(String subjectId, String name) {
        this.subjectId = subjectId;
        this.name = name;
    }

    // Setters
    public String getSubjectId(){
        return subjectId;
    }

    public String getName(){
        return name;
    }

    // Setters
    public void setSubjectId(){
        this.subjectId = subjectId;
    }

    public void setName(){
        this.name = name;
    }

}
