package com.isonga.api.repositories;

import com.isonga.api.models.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, String> {
    List<Activity> findByUserIdNumber(String userIdNumber);
}
