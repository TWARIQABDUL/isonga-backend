package com.isonga.api.repositories;

import com.isonga.api.models.Savings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SavingsRepository extends JpaRepository<Savings, Integer> {
    List<Savings> findByUserIdNumber(String userIdNumber);
    
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Savings s WHERE s.userIdNumber = :userIdNumber")
    double sumByUserIdNumber(@Param("userIdNumber") String userIdNumber);
    
  @Query(value = """
    SELECT 
        DATE_FORMAT(date_received, '%b') AS month,
        SUM(amount) AS amount,
        SUM(target) AS target
    FROM savings
    WHERE user_id_number = :userIdNumber
    GROUP BY 
        YEAR(date_received), 
        MONTH(date_received),
        DATE_FORMAT(date_received, '%b')  -- <--- ADDED THIS LINE
    ORDER BY MONTH(date_received)
""", nativeQuery = true)
List<Map<String, Object>> findMonthlySavingsSummary(@Param("userIdNumber") String userIdNumber);

@Query(
    value = """
        SELECT 
            u.full_name,
            u.id_number,
            SUM(s.amount) AS total_amount,
            s.date_received
            -- Removed s.id because it conflicts with grouping by day
        FROM users u
        JOIN savings s ON s.user_id_number = u.id_number
        GROUP BY u.full_name, u.id_number, s.date_received
        ORDER BY s.date_received DESC
    """, nativeQuery = true)
List<Map<String, Object>> findDailyReport();

}
