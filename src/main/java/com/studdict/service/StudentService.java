package com.studdict.service;

import com.studdict.model.Student;
import com.studdict.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    // UC11: Δημιουργία Λογαριασμού (Register)
    public Student registerStudent(String studentId, String firstName, String lastName, String email, String password, String university, String department) {
        // Έλεγχος αν το email υπάρχει ήδη
        Optional<Student> existingStudent = studentRepository.findByEmail(email);
        if (existingStudent.isPresent()) {
            throw new RuntimeException("Το email χρησιμοποιείται ήδη");
        }

        Student student = new Student(studentId, firstName, lastName, email, password, university, department);
        return studentRepository.save(student);
    }

    // UC12: Σύνδεση Χρήστη (Login)
    public Student loginStudent(String email, String password) {
        // Εύρεση βάσει email
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Λάθος email ή κωδικός"));

        // Επαλήθευση κωδικού (Σε πραγματικό σύστημα εδώ ελέγχουμε το Hash του κωδικού)
        if (student.getPassword() == null || !student.getPassword().equals(password)) {
            throw new RuntimeException("Λάθος email ή κωδικός");
        }

        return student;
    }
}