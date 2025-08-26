package com.ciaranmckenna.medical_event_tracker.exception;

/**
 * Custom authentication exception for secure error handling.
 * Provides generic messages to prevent information disclosure.
 */
public class AuthenticationException extends RuntimeException {
    
    public AuthenticationException() {
        super("Authentication failed");
    }
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}