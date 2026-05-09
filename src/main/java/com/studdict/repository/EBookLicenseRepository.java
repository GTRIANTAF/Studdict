package com.studdict.repository;

import com.studdict.model.EBookLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EBookLicenseRepository extends JpaRepository<EBookLicense, Long> {
}