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

    public Long getLicenseId() {
        return licenseId;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public EBook getEbook() {
        return ebook;
    }

    public void setEbook(EBook ebook) {
        this.ebook = ebook;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public void setLicenseId(Long licenseId) {
        this.licenseId = licenseId;
    }
}