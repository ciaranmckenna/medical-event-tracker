package com.ciaranmckenna.medical_event_tracker.exception;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException {
    
    public PatientNotFoundException(String message) {
        super(message);
    }
    
    public PatientNotFoundException(UUID patientId) {
        super("Patient not found with ID: " + patientId);
    }
    
    public PatientNotFoundException(UUID patientId, String userId) {
        super("Patient not found with ID: " + patientId + " for user: " + userId);
    }
}