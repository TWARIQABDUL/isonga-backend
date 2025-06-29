package com.isonga.api.controllers;

import com.isonga.api.dto.SavingsRequest;
import com.isonga.api.models.Savings;
import com.isonga.api.models.User;

import com.isonga.api.repositories.UserRepository;
import com.isonga.api.services.SavingsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Validated
@RestController
@RequestMapping("/api/savings")
public class SavingsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SavingsService savingsService;

    // ✅ @Valid is placed before @RequestBody to trigger validation from DTO
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SavingsRequest request, Authentication authentication) {
        // String email = authentication.getName(); // Extracted from JWT
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        System.out.println("Extracted email from JWT: [" + email + "]");

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Authenticated user not found."));
        }

        // User user = optionalUser.get();

        // ✅ Instead of relying on client input, override the ID number
        request.setUserIdNumber(user.getIdNumber());

        Savings saved = savingsService.save(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Savings record added successfully",
                "data", saved));
    }

    @GetMapping
    public ResponseEntity<List<Savings>> getAll() {
        return ResponseEntity.ok(savingsService.findAll());
    }

    @GetMapping("/{idNumber}")
    public ResponseEntity<List<Savings>> getByUserId(@PathVariable String idNumber) {
        return ResponseEntity.ok(savingsService.findByUserIdNumber(idNumber));
    }
}
