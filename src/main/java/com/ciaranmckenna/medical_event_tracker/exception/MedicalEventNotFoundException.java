package com.ciaranmckenna.medical_event_tracker.exception;

import java.util.UUID;

/**
 * Exception thrown when a medical event is not found.
 */
public class MedicalEventNotFoundException extends RuntimeException {

    public MedicalEventNotFoundException(UUID id) {
        super("Medical event not found with id: " + id);
    }

    public MedicalEventNotFoundException(String message) {
        super(message);
    }

    public MedicalEventNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}