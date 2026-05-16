package com.studdict.service;

import com.studdict.model.StudySubject;
import com.studdict.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    // UML: StudySubject.findOrCreate(name)
    public StudySubject findOrCreate(String subjectName) {
        // Ψάχνει αν το μάθημα υπάρχει ήδη στη βάση. Αν δεν υπάρχει, το δημιουργεί (UC2 Matchmaking)
        return subjectRepository.findByNameIgnoreCase(subjectName)
                .orElseGet(() -> {
                    StudySubject newSubject = new StudySubject(subjectName);
                    return subjectRepository.save(newSubject);
                });
    }
}