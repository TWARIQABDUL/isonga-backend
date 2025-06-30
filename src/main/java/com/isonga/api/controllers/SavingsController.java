package com.isonga.api.controllers;

import com.isonga.api.dto.SavingsRequest;
import com.isonga.api.models.Savings;
import com.isonga.api.models.User;
// import com.isonga.api.repositories.UserRepository;
import com.isonga.api.services.SavingsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/savings")
public class SavingsController {

    // @Autowired
    // private UserRepository userRepository;

    @Autowired
    private SavingsService savingsService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SavingsRequest request, Authentication authentication) {
        // Extract authenticated user from JWT
        User authenticatedUser = (User) authentication.getPrincipal();
        String tokenId = authenticatedUser.getIdNumber();
        String requestId = request.getUserIdNumber();

        // ✅ Reject if the client tries to send a mismatching ID
        if (requestId != null && !requestId.equals(tokenId)) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Access denied: ID in request does not match the authenticated user."
            ));
        }

        // ✅ Forcefully set the correct ID number
        request.setUserIdNumber(tokenId);

        // Proceed to save
        Savings saved = savingsService.save(request);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Savings record added successfully",
                "data", saved
        ));
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
