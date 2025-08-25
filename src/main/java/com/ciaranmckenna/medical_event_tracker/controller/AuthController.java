package com.ciaranmckenna.medical_event_tracker.controller;

import com.ciaranmckenna.medical_event_tracker.dto.AuthResponse;
import com.ciaranmckenna.medical_event_tracker.dto.LoginRequest;
import com.ciaranmckenna.medical_event_tracker.dto.RegisterRequest;
import com.ciaranmckenna.medical_event_tracker.dto.UserProfileResponse;
import com.ciaranmckenna.medical_event_tracker.exception.AuthenticationException;
import com.ciaranmckenna.medical_event_tracker.exception.UserRegistrationException;
import com.ciaranmckenna.medical_event_tracker.service.UserService;
import com.ciaranmckenna.medical_event_tracker.util.AuthenticationHelper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationHelper authenticationHelper;

    public AuthController(UserService userService, AuthenticationHelper authenticationHelper) {
        this.userService = userService;
        this.authenticationHelper = authenticationHelper;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse authResponse = userService.registerUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        } catch (IllegalArgumentException e) {
            // Let GlobalExceptionHandler handle with secure error message
            throw new UserRegistrationException("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = userService.authenticateUser(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            // Let GlobalExceptionHandler provide secure error message
            throw new AuthenticationException("Login failed");
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile() {
        try {
            var user = authenticationHelper.getCurrentUser();
            UserProfileResponse profile = userService.getUserProfile(user.getId());
            return ResponseEntity.ok(profile);
        } catch (AuthenticationException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(@Valid @RequestBody RegisterRequest updateRequest) {
        try {
            var user = authenticationHelper.getCurrentUser();
            UserProfileResponse updatedProfile = userService.updateUserProfile(user.getId(), updateRequest);
            return ResponseEntity.ok(updatedProfile);
        } catch (IllegalArgumentException e) {
            throw new UserRegistrationException("Profile update failed: " + e.getMessage());
        } catch (AuthenticationException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteUserProfile() {
        try {
            var user = authenticationHelper.getCurrentUser();
            userService.deleteUser(user.getId());
            return ResponseEntity.noContent().build();
        } catch (AuthenticationException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<Boolean> checkUsernameAvailability(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(!exists);
    }

    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmailAvailability(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(!exists);
    }
}