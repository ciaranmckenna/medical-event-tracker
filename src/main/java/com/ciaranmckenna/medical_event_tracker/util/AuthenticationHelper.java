package com.ciaranmckenna.medical_event_tracker.util;

import com.ciaranmckenna.medical_event_tracker.entity.User;
import com.ciaranmckenna.medical_event_tracker.exception.AuthenticationException;
import com.ciaranmckenna.medical_event_tracker.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for authentication-related operations.
 * Reduces code duplication in controllers for user authentication tasks.
 */
@Component
public class AuthenticationHelper {

    private final UserService userService;

    public AuthenticationHelper(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get the currently authenticated user.
     * 
     * @return the authenticated User entity
     * @throws AuthenticationException if user is not authenticated or not found
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        String username = authentication.getName();
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Invalid authentication context");
        }

        try {
            return userService.findByUsername(username);
        } catch (Exception e) {
            throw new AuthenticationException("Failed to retrieve user information");
        }
    }

    /**
     * Get the current username from authentication context.
     * 
     * @return the current username
     * @throws AuthenticationException if user is not authenticated
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("User is not authenticated");
        }

        String username = authentication.getName();
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Invalid username in authentication context");
        }

        return username;
    }

    /**
     * Check if the current user has a specific role.
     * 
     * @param role the role to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Check if the current user is a primary user.
     * 
     * @return true if user is a primary user
     */
    public boolean isPrimaryUser() {
        return hasRole("PRIMARY_USER");
    }

    /**
     * Check if the current user is an admin.
     * 
     * @return true if user is an admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if the current user has write permissions.
     * 
     * @return true if user can perform write operations
     */
    public boolean canWrite() {
        return isPrimaryUser() || isAdmin();
    }

    /**
     * Ensure the current user has write permissions.
     * 
     * @throws AuthenticationException if user doesn't have write permissions
     */
    public void requireWritePermission() {
        if (!canWrite()) {
            throw new AuthenticationException("Insufficient permissions for write operations");
        }
    }

    /**
     * Get the current user's ID.
     * 
     * @return the current user's UUID
     * @throws AuthenticationException if user is not authenticated
     */
    public java.util.UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Check if the current user owns the specified patient.
     * This is a security check to ensure users can only access their own patients.
     * 
     * @param patient the patient to check ownership for
     * @return true if current user owns the patient
     * @throws AuthenticationException if user is not authenticated
     */
    public boolean ownsPatient(com.ciaranmckenna.medical_event_tracker.entity.Patient patient) {
        if (patient == null) {
            return false;
        }
        
        User currentUser = getCurrentUser();
        return patient.getUser().getId().equals(currentUser.getId());
    }

    /**
     * Ensure the current user owns the specified patient.
     * 
     * @param patient the patient to check ownership for
     * @throws AuthenticationException if user doesn't own the patient
     */
    public void requirePatientOwnership(com.ciaranmckenna.medical_event_tracker.entity.Patient patient) {
        if (!ownsPatient(patient)) {
            throw new AuthenticationException("Access denied: patient belongs to another user");
        }
    }
}