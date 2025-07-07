package com.isonga.api.services;

import com.isonga.api.models.User;
import com.isonga.api.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        if (userRepository.existsByIdNumber(user.getIdNumber())) {
            throw new IllegalArgumentException("User with this ID number already exists");
        }
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByIdNumber(String idNumber) {
        return userRepository.findByIdNumber(idNumber);
    }

    public boolean existsByIdNumber(String idNumber) {
        return userRepository.findByIdNumber(idNumber).isPresent();
    }

    public List<User> searchUsers(String query) {
        return userRepository.findByIdNumberContainingIgnoreCaseOrFullNameContainingIgnoreCase(query, query);
    }
    public Optional<User> getUserByEmail(String email){
        return userRepository.getUserByEmail(email);
    }

}
