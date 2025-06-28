package com.isonga.api.repositories;

import com.isonga.api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByIdNumber(String idNumber);

    boolean existsByIdNumber(String idNumber);

    boolean existsByEmail(String email);
}
