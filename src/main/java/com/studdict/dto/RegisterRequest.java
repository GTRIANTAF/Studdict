package com.studdict.dto;

public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String university;
    private String department;

    // Getters & Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
