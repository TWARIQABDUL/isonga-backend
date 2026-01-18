package com.isonga.api.services;

import com.isonga.api.dto.UpdateUserRequest;
import com.isonga.api.models.User;
import com.isonga.api.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private EmailService emailService;

    private final UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public User createUser(User user) {
        if (userRepository.existsByIdNumber(user.getIdNumber())) {
            throw new IllegalArgumentException("User with this ID number already exists");
        }
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        emailService.sendCredentialEmail(user.getEmail(), user.getFullName(),user.getPassword());
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

    public Optional<User> getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }

    public User updateUser(String email, UpdateUserRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getFullName() != null)
            user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null)
            user.setPhoneNumber(request.getPhoneNumber());
        if (request.getCell() != null)
            user.setCell(request.getCell());


        return userRepository.save(user);
    }

    public User updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getFullName() != null)
            user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null)
            user.setPhoneNumber(request.getPhoneNumber());
        if (request.getCell() != null)
            user.setCell(request.getCell());

        return userRepository.save(user);
    }

   /**
     * Admin tool: Notify only users who haven't been notified yet.
     */
    public void notifyAllExistingUsers() {
        List<User> users = userRepository.findAll();
        int count = 0;

        for (User user : users) {
            // ✅ CHECK: Only send if email exists AND notification hasn't been sent yet
            if (user.getEmail() != null && !user.getEmail().isEmpty() && !user.isAccountNotificationSent()) {
                
                try {
                    emailService.sendAccountActiveEmail(
                            user.getEmail(),
                            user.getFullName(),
                            user.getIdNumber()
                    );
                    
                    // ✅ UPDATE: Mark as sent and save
                    user.setAccountNotificationSent(true);
                    userRepository.save(user);
                    
                    count++;
                    System.out.println("Notification sent to: " + user.getEmail());
                    
                } catch (Exception e) {
                    System.err.println("Failed to notify " + user.getEmail() + ": " + e.getMessage());
                }
            }
        }
        System.out.println("Batch notification complete. Sent " + count + " emails.");
    }
    // ... existing imports & class ...

    /**
     * Resends the "Account Active" email to a specific user.
     */
    public void resendNotification(String idNumber) {
        User user = userRepository.findByIdNumber(idNumber)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + idNumber));

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new RuntimeException("User has no email address");
        }

        // Send the email
        emailService.sendAccountActiveEmail(
                user.getEmail(),
                user.getFullName(),
                user.getIdNumber()
        );

        // Ensure flag is true
        if (!user.isAccountNotificationSent()) {
            user.setAccountNotificationSent(true);
            userRepository.save(user);
        }
    }
}
