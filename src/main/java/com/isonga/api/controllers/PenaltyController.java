package com.isonga.api.controllers;

import com.isonga.api.dto.PenaltyRequest;
import com.isonga.api.models.User;
import com.isonga.api.services.PenaltyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/penalties")
public class PenaltyController {

    @Autowired
    private PenaltyService penaltyService;

    /**
     * USER: View their own penalties.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyPenalties(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User authenticatedUser)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", penaltyService.getUserPenalties(authenticatedUser.getIdNumber())
        ));
    }

    /**
     * ADMIN: View ALL penalties.
     */
    @GetMapping
    public ResponseEntity<?> getAllPenalties(Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access Denied: Admins only"));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", penaltyService.getAllPenalties()
        ));
    }

    /**
     * ADMIN: Create/Issue a penalty to a user.
     */
    @PostMapping
    public ResponseEntity<?> createPenalty(@Valid @RequestBody PenaltyRequest request, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access Denied: Admins only"));
        }
        
        try {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Penalty issued successfully",
                    "data", penaltyService.createPenalty(request)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * ADMIN: Mark a penalty as PAID.
     */
    @PatchMapping("/{id}/pay")
    public ResponseEntity<?> markAsPaid(@PathVariable String id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access Denied: Admins only"));
        }

        try {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Penalty marked as paid",
                    "data", penaltyService.markAsPaid(id)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * ADMIN: Delete a penalty (in case of mistake).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePenalty(@PathVariable String id, Authentication authentication) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access Denied: Admins only"));
        }
        
        try {
            penaltyService.deletePenalty(id);
            return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "message", "Penalty deleted successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // Helper method to check Admin role
    private boolean isAdmin(Authentication authentication) {
        if (authentication.getPrincipal() instanceof User user) {
            return user.getRole() == User.Role.ADMIN;
        }
        return false;
    }
}