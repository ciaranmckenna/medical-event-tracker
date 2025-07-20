package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.dto.AuthResponse;
import com.ciaranmckenna.medical_event_tracker.dto.LoginRequest;
import com.ciaranmckenna.medical_event_tracker.dto.RegisterRequest;
import com.ciaranmckenna.medical_event_tracker.dto.UserProfileResponse;
import com.ciaranmckenna.medical_event_tracker.entity.User;
import com.ciaranmckenna.medical_event_tracker.repository.UserRepository;
import com.ciaranmckenna.medical_event_tracker.service.JwtService;
import com.ciaranmckenna.medical_event_tracker.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpirationMs;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User(
            registerRequest.username(),
            registerRequest.email(),
            passwordEncoder.encode(registerRequest.password()),
            registerRequest.firstName(),
            registerRequest.lastName()
        );

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtExpirationMs / 1000);

        return AuthResponse.of(savedUser, token, expiresAt);
    }

    @Override
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        User user = userRepository.findByUsernameOrEmail(loginRequest.usernameOrEmail(), loginRequest.usernameOrEmail())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.getEnabled()) {
            throw new BadCredentialsException("User account is disabled");
        }

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtExpirationMs / 1000);

        return AuthResponse.of(user, token, expiresAt);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return UserProfileResponse.of(user);
    }

    @Override
    public UserProfileResponse updateUserProfile(UUID userId, RegisterRequest updateRequest) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (userRepository.existsByUsernameOrEmailAndNotId(updateRequest.username(), updateRequest.email(), userId)) {
            throw new IllegalArgumentException("Username or email is already taken by another user");
        }

        user.setUsername(updateRequest.username());
        user.setEmail(updateRequest.email());
        user.setFirstName(updateRequest.firstName());
        user.setLastName(updateRequest.lastName());

        if (updateRequest.password() != null && !updateRequest.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(updateRequest.password()));
        }

        User savedUser = userRepository.save(user);
        return UserProfileResponse.of(savedUser);
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}