package com.isonga.api.controllers;

import com.isonga.api.dto.LoanRequest;
import com.isonga.api.dto.LoanStatusUpdateRequest;
import com.isonga.api.models.Loan;
import com.isonga.api.models.User;
import com.isonga.api.services.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<?> requestLoan(@Valid @RequestBody LoanRequest request, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User authenticatedUser)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        String userId = authenticatedUser.getIdNumber();
        Loan loan;
        try {
            loan = loanService.requestLoan(userId, request);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Loan request submitted successfully",
                "data", loan));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyLoans(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User authenticatedUser)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        List<Loan> loans = loanService.getLoansForUser(authenticatedUser.getIdNumber());
        return ResponseEntity.ok(Map.of("success", true, "loans", loans));
    }

    @GetMapping
    public ResponseEntity<?> getAllLoans(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User authenticatedUser)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        if (authenticatedUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access denied: Admins only."));
        }

        return ResponseEntity.ok(Map.of("success", true, "loans", loanService.getAllLoans()));
    }

    @PatchMapping("/{loanId}/status")
    public ResponseEntity<?> updateLoanStatus(
            @PathVariable String loanId,
            @Valid @RequestBody LoanStatusUpdateRequest request,
            Authentication authentication) {

        if (!(authentication.getPrincipal() instanceof User authenticatedUser)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        if (authenticatedUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access denied: Admins only"));
        }

        try {
            Loan updatedLoan = loanService.updateLoanStatus(loanId, request.getStatus());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Loan status updated successfully",
                    "loan", updatedLoan));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ✅ NEW: Mark loan as collected (USER)
    @PatchMapping("/{loanId}/collect")
    public ResponseEntity<?> collectLoan(
            @PathVariable String loanId,
            Authentication authentication) {

        if (!(authentication.getPrincipal() instanceof User authenticatedUser)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }
        if (authenticatedUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access denied: Admins only"));
        }

        try {
            Loan updatedLoan = loanService.collectLoan(loanId, authenticatedUser.getIdNumber());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Loan collected successfully",
                    "loan", updatedLoan));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
