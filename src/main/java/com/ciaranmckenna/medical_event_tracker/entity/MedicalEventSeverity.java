package com.ciaranmckenna.medical_event_tracker.entity;

/**
 * Enumeration representing the severity levels of medical events.
 * Used to categorize the urgency and importance of medical incidents.
 */
public enum MedicalEventSeverity {
    /**
     * Minor symptoms or observations that don't require immediate attention
     */
    MILD,
    
    /**
     * Noticeable symptoms that should be monitored closely
     */
    MODERATE,
    
    /**
     * Concerning symptoms that may require medical consultation
     */
    SEVERE,
    
    /**
     * Critical symptoms requiring immediate medical attention
     */
    CRITICAL
}