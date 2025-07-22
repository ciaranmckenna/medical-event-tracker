package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuthResponse(
    UUID id,
    String username,
    String email,
    String firstName,
    String lastName,
    User.Role role,
    String token,
    LocalDateTime expiresAt,
    String tokenType
) {
    public static AuthResponse of(User user, String token, LocalDateTime expiresAt) {
        return new AuthResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            token,
            expiresAt,
            "Bearer"
        );
    }
}