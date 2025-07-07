package com.isonga.api.controllers;

// import com.isonga.api.models.Activity;
import com.isonga.api.models.User;
import com.isonga.api.services.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    // ✅ User fetches their own activities
    @GetMapping
    public ResponseEntity<?> getUserActivities(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        var activities = activityService.getUserActivities(user.getIdNumber());
        return ResponseEntity.ok(Map.of("success", true, "data", activities));
    }

    // ✅ Admin fetches activities by user ID
    @GetMapping("/{idNumber}")
    public ResponseEntity<?> getActivitiesByUserId(
            @PathVariable String idNumber,
            Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        // ✅ Restrict to admins
        if (user.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access denied: Admins only."));
        }

        var activities = activityService.getUserActivities(idNumber);
        return ResponseEntity.ok(Map.of("success", true, "data", activities));
    }
}
