package com.studdict.repository;

import com.studdict.model.PointsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, String> {
    List<PointsTransaction> findByStudentIdOrderByTimestampDesc(String studentId);
}