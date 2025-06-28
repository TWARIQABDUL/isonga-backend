package com.isonga.api.controllers;

import com.isonga.api.dto.LoginRequest;
import com.isonga.api.dto.LoginResponse;
import com.isonga.api.dto.RegisterRequest;
import com.isonga.api.models.User;
import com.isonga.api.services.UserService;
import com.isonga.api.utils.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

// import com.isonga.api.models.Role;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Optional<User> optionalUser = userService.findByEmail(request.getEmail());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(401).body("Invalid email or password");
            }

            User user = optionalUser.get();

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(401).body("Invalid email or password");
            }

            String token = jwtUtil.generateToken(user.getEmail());

            LoginResponse response = new LoginResponse(
                    token,
                    user.getRole().name(),
                    user.getEmail(),
                    user.getFullName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Login failed: " + e.getMessage());
        }
    }

}
