package com.studdict.repository;

import com.studdict.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // UC8 / UC6: Με το νέο μοντέλο η Order συνδέεται με το StudyTable (όχι με την Reservation).
    // Επιστρέφει όλες τις παραγγελίες ενός τραπεζιού, ώστε το Check-out (UC6) να υπολογίζει τον λογαριασμό.
    @Query("SELECT o FROM Order o WHERE o.table.tableId = :tableId")
    List<Order> findByTableId(@Param("tableId") int tableId);
}