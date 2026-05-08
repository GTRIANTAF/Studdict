package com.studdict.repository;

import com.studdict.model.StudySubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<StudySubject, Long> {

    // UC2: Checks if a subject already exists (ignoring upper/lower case differences)
    Optional<StudySubject> findByNameIgnoreCase(String name);
}