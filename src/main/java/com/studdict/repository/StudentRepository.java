package com.studdict.repository;

import com.studdict.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    // UC11 / UC12: Used for Login to find a user by their email address
    Optional<Student> findByEmail(String email);
}