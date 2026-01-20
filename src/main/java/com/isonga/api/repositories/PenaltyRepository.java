package com.isonga.api.repositories;

import com.isonga.api.models.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, String> {
    List<Penalty> findByUserIdNumber(String userIdNumber);
    List<Penalty> findByStatus(Penalty.Status status);
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Penalty p WHERE p.userIdNumber = :userIdNumber")
    double sumByUserIdNumber(@Param("userIdNumber") String userIdNumber);
}