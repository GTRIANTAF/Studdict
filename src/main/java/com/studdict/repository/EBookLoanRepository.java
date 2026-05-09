package com.studdict.repository;

import com.studdict.model.EBookLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EBookLoanRepository extends JpaRepository<EBookLoan, Long> {
}