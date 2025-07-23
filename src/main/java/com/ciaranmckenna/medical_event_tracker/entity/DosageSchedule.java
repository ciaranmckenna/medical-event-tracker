package com.ciaranmckenna.medical_event_tracker.entity;

/**
 * Enumeration representing medication dosage schedules.
 * Used to track when medications should be or were administered.
 */
public enum DosageSchedule {
    /**
     * Morning administration (typically 6 AM - 12 PM)
     */
    AM,
    
    /**
     * Evening administration (typically 6 PM - 12 AM)
     */
    PM,
    
    /**
     * Midday administration (typically 12 PM - 6 PM)
     */
    MIDDAY,
    
    /**
     * Bedtime administration
     */
    BEDTIME,
    
    /**
     * As needed basis (PRN - Pro Re Nata)
     */
    AS_NEEDED,
    
    /**
     * Every 4 hours
     */
    EVERY_4_HOURS,
    
    /**
     * Every 6 hours
     */
    EVERY_6_HOURS,
    
    /**
     * Every 8 hours
     */
    EVERY_8_HOURS,
    
    /**
     * Every 12 hours
     */
    EVERY_12_HOURS,
    
    /**
     * Custom schedule not covered by standard options
     */
    CUSTOM
}