package com.ciaranmckenna.medical_event_tracker.exception;

public class DuplicatePatientException extends RuntimeException {
    
    public DuplicatePatientException(String message) {
        super(message);
    }
    
    public DuplicatePatientException(String firstName, String lastName, String dateOfBirth) {
        super("Patient with name '" + firstName + " " + lastName + "' and date of birth '" + dateOfBirth + "' already exists");
    }
}