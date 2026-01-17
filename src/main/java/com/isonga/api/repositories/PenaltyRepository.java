package com.isonga.api.repositories;

import com.isonga.api.models.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, String> {
    List<Penalty> findByUserIdNumber(String userIdNumber);
    List<Penalty> findByStatus(Penalty.Status status);
}