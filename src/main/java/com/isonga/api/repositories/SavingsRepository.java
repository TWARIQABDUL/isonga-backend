package com.isonga.api.repositories;

import com.isonga.api.models.Savings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SavingsRepository extends JpaRepository<Savings, Long> {
    List<Savings> findByUserIdNumber(String userIdNumber);

    // ✅ Sum of (Amount + Ingoboka) to show the full money the user has put in
    @Query("SELECT COALESCE(SUM(s.amount + s.ingoboka), 0) FROM Savings s WHERE s.userIdNumber = :userIdNumber")
    double sumByUserIdNumber(@Param("userIdNumber") String userIdNumber);

    @Query("SELECT COALESCE(SUM(s.ingoboka), 0) FROM Savings s WHERE s.userIdNumber = :userIdNumber")
    double sumIngobokaByUserIdNumber(@Param("userIdNumber") String userIdNumber);

    @Query(value = """
                SELECT
                    DATE_FORMAT(date_received, '%b') AS month,
                    SUM(amount) AS amount,
                    SUM(ingoboka) AS ingoboka
                FROM savings
                WHERE user_id_number = :userIdNumber
                GROUP BY
                    YEAR(date_received),
                    MONTH(date_received),
                    DATE_FORMAT(date_received, '%b')  -- <--- ADDED THIS LINE
                ORDER BY MONTH(date_received)
            """, nativeQuery = true)
    List<Map<String, Object>> findMonthlySavingsSummary(@Param("userIdNumber") String userIdNumber);

    @Query(value = """
                SELECT
                    u.full_name,
                    u.id_number,
                    SUM(s.amount) AS total_amount,
                    s.date_received,
                    SUM(s.ingoboka) AS total_ingoboka
                    -- Removed s.id because it conflicts with grouping by day
                FROM users u
                JOIN savings s ON s.user_id_number = u.id_number
                GROUP BY u.full_name, u.id_number, s.date_received
                ORDER BY s.date_received DESC
            """, nativeQuery = true)
    List<Map<String, Object>> findDailyReport();

    @Query(value = """
                SELECT s.*, u.full_name
                FROM savings s
                JOIN users u ON s.user_id_number = u.id_number
                WHERE (:userIdNumber IS NULL OR s.user_id_number = :userIdNumber)
                AND (:year IS NULL OR YEAR(s.date_received) = :year)
                AND (:month IS NULL OR MONTH(s.date_received) = :month)
                AND (:week IS NULL OR WEEK(s.date_received, 1) = :week)
                ORDER BY s.date_received DESC
            """, nativeQuery = true)
    List<Map<String, Object>> findSavingsReport(
            @Param("userIdNumber") String userIdNumber,
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("week") Integer week);

    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Savings s")
    double sumTotalAmount();

    @Query("SELECT COALESCE(SUM(s.ingoboka), 0) FROM Savings s")
    double sumIngobokaAmount();

    @Query("SELECT COALESCE(SUM(s.ingoboka), 0) FROM Savings s WHERE s.userIdNumber = :userIdNumber")
    double sumUserIngobokaAmount(@Param("userIdNumber") String userIdNumber);

    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM savings " + // Use table name 'savings'
            "WHERE user_id_number = :userIdNumber " + // Use column name 'user_id_number'
            "AND MONTH(date_received) = MONTH(CURRENT_DATE) " +
            "AND YEAR(date_received) = YEAR(CURRENT_DATE)", nativeQuery = true) // ADD THIS
    double monthlyContribution(@Param("userIdNumber") String userIdNumber);

}
