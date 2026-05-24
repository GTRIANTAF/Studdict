package com.studdict.mobile.model;

public class RegisterRequest {
    public String firstName;
    public String lastName;
    public String email;
    public String password;
    public String university;
    public String department;

    public RegisterRequest(String firstName, String lastName, String email, String password, String university, String department) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.university = university;
        this.department = department;
    }
}
