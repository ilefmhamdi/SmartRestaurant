package com.noir.controller;

import com.noir.dto.request.ChangePasswordRequest;
import com.noir.dto.request.LoginRequest;
import com.noir.dto.request.SignupRequest;
import com.noir.dto.request.UpdateProfileRequest;
import com.noir.model.User;
import com.noir.service.ClientAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ClientAuthService authService;
    public AuthController(ClientAuthService authService) { this.authService = authService; }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        return ResponseEntity.status(201).body(
                Map.of("success", true, "message", "Account created successfully",
                        "data", authService.signup(req)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(
                Map.of("success", true, "message", "Login successful",
                        "data", authService.login(req)));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(
                Map.of("success", true, "message", "Logout successful",
                        "data", Map.of("loggedOut", true)));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest req) {
        User client = (User) req.getAttribute("client");
        return ResponseEntity.ok(Map.of("success", true, "data", client));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest req, HttpServletRequest request) {
        User client = (User) request.getAttribute("client");
        User updated = authService.updateProfile(client, req);
        return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully", "data", updated));
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest req, HttpServletRequest request) {
        User client = (User) request.getAttribute("client");
        authService.changePassword(client, req);
        return ResponseEntity.ok(Map.of("success", true, "message", "Password changed successfully"));
    }
}