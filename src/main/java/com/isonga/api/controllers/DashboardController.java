package com.isonga.api.controllers;

import com.isonga.api.models.User;
import com.isonga.api.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<?> getDashboardSummary(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User authenticatedUser)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        Map<String, Object> summary = dashboardService.getSummary(authenticatedUser);
        return ResponseEntity.ok(Map.of("success", true, "data", summary));
    }

    @GetMapping("/admin-summary")
    public ResponseEntity<?> getAdminDashboardSummary(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User authenticatedUser)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        if (authenticatedUser.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access denied: Admins only."));
        }

        Map<String, Object> summary = dashboardService.getAdminSummary();
        return ResponseEntity.ok(Map.of("success", true, "data", summary));
    }
}
