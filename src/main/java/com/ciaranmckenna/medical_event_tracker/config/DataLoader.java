package com.ciaranmckenna.medical_event_tracker.config;

import com.ciaranmckenna.medical_event_tracker.entity.User;
import com.ciaranmckenna.medical_event_tracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Data loader component that seeds the database with initial users for development.
 * This ensures test users are always available when starting the application.
 */
@Component
@Profile("!test") // Don't run during tests
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        loadTestUsers();
    }

    private void loadTestUsers() {
        logger.info("Loading test users into database...");

        // Check if users already exist to avoid duplicates
        if (userRepository.count() > 0) {
            logger.info("Users already exist in database, skipping test user creation");
            return;
        }

        // Create test users with encoded passwords
        String encodedPassword = passwordEncoder.encode("Password123!");

        // Primary User
        User testUser = createUser(
                "testuser",
                "test@example.com",
                encodedPassword,
                "Test",
                "User",
                User.Role.PRIMARY_USER
        );

        // Admin User
        User adminUser = createUser(
                "admin",
                "admin@example.com",
                encodedPassword,
                "Admin",
                "User",
                User.Role.ADMIN
        );

        // Secondary User (Doctor)
        User doctorUser = createUser(
                "doctor",
                "doctor@example.com",
                encodedPassword,
                "Dr. Jane",
                "Smith",
                User.Role.SECONDARY_USER
        );

        // Another Primary User
        User primaryUser = createUser(
                "primaryuser",
                "primaryuser@example.com",
                encodedPassword,
                "Primary",
                "User",
                User.Role.PRIMARY_USER
        );

        // Save all users
        try {
            userRepository.save(testUser);
            userRepository.save(adminUser);
            userRepository.save(doctorUser);
            userRepository.save(primaryUser);

            logger.info("Successfully loaded {} test users:", userRepository.count());
            logger.info("  - testuser (PRIMARY_USER) - Password: Password123!");
            logger.info("  - admin (ADMIN) - Password: Password123!");
            logger.info("  - doctor (SECONDARY_USER) - Password: Password123!");
            logger.info("  - primaryuser (PRIMARY_USER) - Password: Password123!");

        } catch (Exception e) {
            logger.error("Error loading test users: {}", e.getMessage(), e);
        }
    }

    private User createUser(String username, String email, String encodedPassword,
                           String firstName, String lastName, User.Role role) {
        User user = new User();
        // Don't set ID - let Hibernate generate it
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}