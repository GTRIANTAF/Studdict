package com.studdict.service;

import com.studdict.model.Venue;
import com.studdict.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VenueService {

    @Autowired
    private VenueRepository venueRepository;

    public List<Venue> findActiveVenues() {
        return venueRepository.findByIsActiveTrue();
    }

    public Venue findVenueById(Long venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Ο χώρος δεν βρέθηκε ή δεν υπάρχει!"));
    }
}