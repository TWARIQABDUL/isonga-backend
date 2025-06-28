package com.isonga.api.controllers;

import com.isonga.api.dto.RegisterRequest;
import com.isonga.api.models.User;
import com.isonga.api.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
// import com.isonga.api.models.Role;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            if (userService.existsByIdNumber(request.getIdNumber())) {
                return ResponseEntity.badRequest().body("User with this ID already exists.");
            }

            User user = new User();
            user.setIdNumber(request.getIdNumber());
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());
            user.setCell(request.getCell());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));

            // user.setRole(request.getRole().toUpperCase()); // "USER" or "ADMIN"

            User savedUser = userService.createUser(user);

            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Registration failed: " + e.getMessage());
        }
    }

}
