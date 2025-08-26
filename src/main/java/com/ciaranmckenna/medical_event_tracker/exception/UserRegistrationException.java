package com.ciaranmckenna.medical_event_tracker.exception;

/**
 * Exception for user registration failures.
 * Provides secure error messages without exposing sensitive information.
 */
public class UserRegistrationException extends RuntimeException {
    
    public UserRegistrationException() {
        super("User registration failed");
    }
    
    public UserRegistrationException(String message) {
        super(message);
    }
    
    public UserRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}