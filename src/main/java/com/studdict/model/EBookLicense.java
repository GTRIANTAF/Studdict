package com.studdict.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ebook_licenses")
public class EBookLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "license_id")
    private Long licenseId;

    @Column(name = "is_available")
    private boolean isAvailable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ebook_id", nullable = false)
    private EBook ebook;

    public EBookLicense() {}

    // Add Getters and Setters
}