package com.ciaranmckenna.medical_event_tracker.exception;

import java.util.UUID;

/**
 * Exception thrown when a medication dosage is not found.
 */
public class MedicationDosageNotFoundException extends RuntimeException {

    public MedicationDosageNotFoundException(UUID id) {
        super("Medication dosage not found with id: " + id);
    }

    public MedicationDosageNotFoundException(String message) {
        super(message);
    }

    public MedicationDosageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}