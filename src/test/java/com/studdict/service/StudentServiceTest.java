package com.studdict.service;

import com.studdict.model.Student;
import com.studdict.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterStudent_Success() {
        // Arrange
        when(studentRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Student student = studentService.registerStudent(
                "John", "Doe", "test@example.com", "password123", "Patras Uni", "CEID"
        );

        // Assert
        assertNotNull(student);
        assertEquals("John", student.getFirstName());
        assertEquals("test@example.com", student.getEmail());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    public void testRegisterStudent_DuplicateEmail_ThrowsException() {
        // Arrange
        Student existingStudent = new Student();
        existingStudent.setEmail("test@example.com");
        when(studentRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingStudent));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            studentService.registerStudent(
                    "Jane", "Doe", "test@example.com", "password123", "Patras Uni", "CEID"
            );
        });

        assertEquals("Το email χρησιμοποιείται ήδη", exception.getMessage());
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    public void testLoginStudent_Success() {
        // Arrange
        Student student = new Student();
        student.setEmail("test@example.com");
        student.setPassword("password123");
        when(studentRepository.findByEmail("test@example.com")).thenReturn(Optional.of(student));

        // Act
        Student loggedInStudent = studentService.loginStudent("test@example.com", "password123");

        // Assert
        assertNotNull(loggedInStudent);
        assertEquals("test@example.com", loggedInStudent.getEmail());
    }

    @Test
    public void testLoginStudent_WrongPassword_ThrowsException() {
        // Arrange
        Student student = new Student();
        student.setEmail("test@example.com");
        student.setPassword("password123");
        when(studentRepository.findByEmail("test@example.com")).thenReturn(Optional.of(student));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            studentService.loginStudent("test@example.com", "wrongpassword");
        });

        assertEquals("Λάθος email ή κωδικός", exception.getMessage());
    }
}
