package com.isonga.api.repositories;

import com.isonga.api.models.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, String> {
    List<Loan> findByUserIdNumber(String userIdNumber);
    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM Loan l WHERE l.userIdNumber = :userIdNumber")
double sumByUserIdNumber(@Param("userIdNumber") String userIdNumber);

}
