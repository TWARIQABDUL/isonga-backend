package com.isonga.api.repositories;

import com.isonga.api.models.Savings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsRepository extends JpaRepository<Savings, Integer> {
    List<Savings> findByUserIdNumber(String userIdNumber);
}
