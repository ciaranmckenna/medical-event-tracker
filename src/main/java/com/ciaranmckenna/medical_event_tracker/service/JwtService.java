package com.ciaranmckenna.medical_event_tracker.service;

import com.ciaranmckenna.medical_event_tracker.entity.User;
import io.jsonwebtoken.Claims;

import java.time.LocalDateTime;

public interface JwtService {

    String generateToken(User user);

    String generateRefreshToken(User user);

    Claims extractAllClaims(String token);

    String extractUsername(String token);

    LocalDateTime extractExpiration(String token);

    boolean isTokenValid(String token, String username);

    boolean isTokenExpired(String token);
}