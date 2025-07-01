package com.isonga.api.repositories;

import com.isonga.api.models.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, String> {
    List<Loan> findByUserIdNumber(String userIdNumber);
}
