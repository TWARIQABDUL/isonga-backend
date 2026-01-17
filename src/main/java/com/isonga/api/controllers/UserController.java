package com.isonga.api.controllers;

import com.isonga.api.dto.UpdateUserRequest;
import com.isonga.api.models.User;
// import com.isonga.api.repositories.UserRepository;
import org.springframework.security.core.Authentication;

import com.isonga.api.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Create a user
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get all users
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Get user by ID number
    @GetMapping("/{idNumber}")
    public ResponseEntity<?> getUserByIdNumber(@PathVariable String idNumber) {
        return userService.getUserByIdNumber(idNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Search users by ID or Name
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        List<User> results = userService.searchUsers(query);
        return ResponseEntity.ok(Map.of("success", true, "data", results));
    }

    // @GetMapping("/profile")
    // public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
    //     if (!(authentication.getPrincipal() instanceof User authenticatedUser)) {
    //         return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
    //     }

    //     // System.out.println("User Email: " + authenticatedUser.getEmail());
    //     return ResponseEntity.ok(authenticatedUser);
    // }

    @PatchMapping("/update")
    public ResponseEntity<?> updateUserInfo(
            @RequestBody UpdateUserRequest updateRequest,
            Authentication authentication) {

        if (!(authentication.getPrincipal() instanceof User authenticatedUser)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            User updatedUser = userService.updateUser(authenticatedUser.getEmail(), updateRequest);
            return ResponseEntity.ok(Map.of("success", true, "user", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUserById(@PathVariable Long id, @RequestBody UpdateUserRequest updateRequest) {
        try {
            User updatedUser = userService.updateUser(id, updateRequest);
            return ResponseEntity.ok(Map.of("success", true, "user", updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * ONE-TIME USE: Notify all users created before email system was active.
     */
    @PostMapping("/notify-all-existing")
    public ResponseEntity<?> notifyExistingUsers(Authentication authentication) {
        // Security Check: Only Admin can do this
        if (authentication.getPrincipal() instanceof User user && user.getRole() == User.Role.ADMIN) {
            
            // Run in background so request doesn't timeout
            new Thread(() -> userService.notifyAllExistingUsers()).start();
            
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Bulk email notification started in background."
            ));
        }
        return ResponseEntity.status(403).body(Map.of("success", false, "message", "Access Denied"));
    }

}
