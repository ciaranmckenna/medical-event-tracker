package com.ciaranmckenna.medical_event_tracker.service;

import com.ciaranmckenna.medical_event_tracker.dto.AuthResponse;
import com.ciaranmckenna.medical_event_tracker.dto.LoginRequest;
import com.ciaranmckenna.medical_event_tracker.dto.RegisterRequest;
import com.ciaranmckenna.medical_event_tracker.dto.UserProfileResponse;
import com.ciaranmckenna.medical_event_tracker.entity.User;

import java.util.UUID;

public interface UserService {

    AuthResponse registerUser(RegisterRequest registerRequest);

    AuthResponse authenticateUser(LoginRequest loginRequest);

    UserProfileResponse getUserProfile(UUID userId);

    UserProfileResponse updateUserProfile(UUID userId, RegisterRequest updateRequest);

    void deleteUser(UUID userId);

    User findByUsername(String username);

    User findByUsernameOrEmail(String usernameOrEmail);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}