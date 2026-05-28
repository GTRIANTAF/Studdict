package com.studdict.repository;

import com.studdict.model.StudyTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyTableRepository extends JpaRepository<StudyTable, Integer> {

    // UC1 & UC2: Finds all available tables inside a specific Venue
    // Note: We use "Venue_VenueId" to navigate the relationship to the Venue class
    List<StudyTable> findByVenue_VenueIdAndIsAvailableTrue(Long venueId);
    
    // Finds all tables for a venue regardless of availability status
    List<StudyTable> findByVenue_VenueId(Long venueId);

    // UC5: Used during check-in to find which table the scanned QR code belongs to
    Optional<StudyTable> findByQrCodeString(String qrCodeString);
}