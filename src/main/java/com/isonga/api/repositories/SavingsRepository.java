package com.isonga.api.repositories;

import com.isonga.api.models.Savings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsRepository extends JpaRepository<Savings, Integer> {
    List<Savings> findByUserIdNumber(String userIdNumber);
    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Savings s WHERE s.userIdNumber = :userIdNumber")
double sumByUserIdNumber(@Param("userIdNumber") String userIdNumber);

}

