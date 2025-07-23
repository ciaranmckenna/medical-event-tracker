package com.ciaranmckenna.medical_event_tracker.entity;

/**
 * Enumeration representing the different categories of medical events.
 * Used to classify the type of medical incident being recorded.
 */
public enum MedicalEventCategory {
    /**
     * Physical or psychological symptoms observed in the patient
     */
    SYMPTOM,
    
    /**
     * Medication administration or dosage changes
     */
    MEDICATION,
    
    /**
     * Medical appointments, consultations, or procedures
     */
    APPOINTMENT,
    
    /**
     * Laboratory tests, diagnostic imaging, or other medical tests
     */
    TEST,
    
    /**
     * Emergency situations or urgent medical events
     */
    EMERGENCY,
    
    /**
     * General observations or notes about patient condition
     */
    OBSERVATION,
    
    /**
     * Adverse reactions to medications or treatments
     */
    ADVERSE_REACTION
}