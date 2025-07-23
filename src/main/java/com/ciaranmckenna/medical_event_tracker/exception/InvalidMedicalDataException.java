package com.ciaranmckenna.medical_event_tracker.exception;

/**
 * Exception thrown when medical data is invalid or violates business rules.
 */
public class InvalidMedicalDataException extends RuntimeException {

    public InvalidMedicalDataException(String message) {
        super(message);
    }

    public InvalidMedicalDataException(String message, Throwable cause) {
        super(message, cause);
    }
}