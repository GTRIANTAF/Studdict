package com.studdict.service;

import com.studdict.model.Student;
import com.studdict.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * UC11 - Δημιουργία Λογαριασμού.
     * Η φόρμα εγγραφής συλλέγει: Ονοματεπώνυμο, Email, Κωδικό, Πανεπιστήμιο, Τμήμα.
     * Το studentId δεν δηλώνεται από τον χρήστη -> παράγεται αυτόματα.
     */
    public Student registerStudent(String firstName, String lastName, String email,
                                   String password, String university, String department) {

        // Έλεγχος εγκυρότητας δεδομένων (UC11 βήμα 4)
        requireNotBlank(firstName, "Το όνομα είναι υποχρεωτικό.");
        requireNotBlank(lastName, "Το επώνυμο είναι υποχρεωτικό.");
        requireNotBlank(email, "Το email είναι υποχρεωτικό.");
        requireNotBlank(password, "Ο κωδικός είναι υποχρεωτικός.");
        requireNotBlank(university, "Το πανεπιστήμιο είναι υποχρεωτικό.");
        requireNotBlank(department, "Το τμήμα είναι υποχρεωτικό.");

        String normalizedEmail = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new RuntimeException("Μη έγκυρη μορφή email.");
        }

        // Έλεγχος αν το email υπάρχει ήδη (Alt 1)
        Optional<Student> existingStudent = studentRepository.findByEmail(normalizedEmail);
        if (existingStudent.isPresent()) {
            throw new RuntimeException("Το email χρησιμοποιείται ήδη");
        }

        String studentId = UUID.randomUUID().toString();

        Student student = new Student(studentId, firstName.trim(), lastName.trim(),
                normalizedEmail, password, university.trim(), department.trim());
        return studentRepository.save(student);
    }

    /**
     * UC12 - Σύνδεση Χρήστη (Login).
     */
    public Student loginStudent(String email, String password) {
        if (email == null || password == null) {
            throw new RuntimeException("Λάθος email ή κωδικός");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Student student = studentRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Λάθος email ή κωδικός"));

        // Επαλήθευση κωδικού (αν αλλάξει σε hashing, εδώ γίνεται hash-verify αντί για equals)
        if (student.getPassword() == null || !student.getPassword().equals(password)) {
            throw new RuntimeException("Λάθος email ή κωδικός");
        }

        return student;
    }

    private void requireNotBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(message);
        }
    }
}