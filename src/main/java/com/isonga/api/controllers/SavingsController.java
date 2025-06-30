package com.isonga.api.controllers;

import com.isonga.api.dto.SavingsRequest;
import com.isonga.api.models.Savings;
import com.isonga.api.models.User;
import com.isonga.api.services.SavingsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/savings")
public class SavingsController {

    @Autowired
    private SavingsService savingsService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SavingsRequest request, Authentication authentication) {
        // ✅ Safely cast principal and validate
        if (!(authentication.getPrincipal() instanceof User authenticatedUser)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        String tokenId = authenticatedUser.getIdNumber();
        String requestId = request.getUserIdNumber();

        // ✅ Reject spoofing if user tries to manually inject another ID number
        if (requestId != null && !requestId.equals(tokenId)) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Access denied: ID in request does not match the authenticated user."
            ));
        }

        // ✅ Enforce that only the authenticated user's ID is used
        request.setUserIdNumber(tokenId);

        // Save savings for the current authenticated user
        Savings saved = savingsService.save(request);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Savings record added successfully",
                "data", saved
        ));
    }

    @GetMapping
    public ResponseEntity<?> getAll(Authentication authentication) {
        // ✅ Restrict GET /api/savings to ADMIN only
        if (!(authentication.getPrincipal() instanceof User authenticatedUser)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        if (authenticatedUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access denied: Admins only."));
        }

        return ResponseEntity.ok(savingsService.findAll());
    }

    @GetMapping("/{idNumber}")
    public ResponseEntity<?> getByUserId(@PathVariable String idNumber, Authentication authentication) {
        // ✅ Restrict users to view only their own savings
        if (!(authentication.getPrincipal() instanceof User authenticatedUser)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        if (!authenticatedUser.getIdNumber().equals(idNumber)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access denied: Cannot view savings of another user."));
        }

        return ResponseEntity.ok(savingsService.findByUserIdNumber(idNumber));
    }
}
