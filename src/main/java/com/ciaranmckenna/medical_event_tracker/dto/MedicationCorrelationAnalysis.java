package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO representing correlation analysis between medication dosages and medical events.
 * Provides insights into how medications may correlate with medical incidents.
 */
public record MedicationCorrelationAnalysis(
        
        @NotNull(message = "Medication ID is required for correlation analysis")
        UUID medicationId,
        
        @NotNull(message = "Patient ID is required for correlation analysis") 
        UUID patientId,
        
        @NotNull(message = "Medication name is required for correlation analysis")
        String medicationName,
        
        @NotNull(message = "Total dosages count is required")
        Long totalDosages,
        
        @NotNull(message = "Total events after dosage count is required")
        Long totalEventsAfterDosage,
        
        @NotNull(message = "Correlation percentage is required")
        Double correlationPercentage,
        
        @NotNull(message = "Correlation strength is required")
        Double correlationStrength,
        
        @NotNull(message = "Events by category count is required")
        Map<MedicalEventCategory, Long> eventsByCategoryCount,
        
        @NotNull(message = "Events by severity count is required")
        Map<MedicalEventSeverity, Long> eventsBySeverityCount,
        
        @NotNull(message = "Analysis generation timestamp is required")
        LocalDateTime analysisGeneratedAt
) {
    
    /**
     * Checks if the correlation indicates a strong relationship between medication and events.
     * 
     * @return true if correlation strength is above 0.7 (70%)
     */
    public boolean hasStrongCorrelation() {
        return correlationStrength != null && correlationStrength >= 0.7;
    }
    
    /**
     * Checks if there are concerning adverse reactions in the correlation data.
     * 
     * @return true if adverse reactions represent more than 20% of total events
     */
    public boolean hasConcerningAdverseReactions() {
        if (eventsByCategoryCount == null || totalEventsAfterDosage == null || totalEventsAfterDosage == 0) {
            return false;
        }
        
        Long adverseReactions = eventsByCategoryCount.getOrDefault(MedicalEventCategory.ADVERSE_REACTION, 0L);
        return ((double) adverseReactions / totalEventsAfterDosage) > 0.2;
    }
    
    /**
     * Gets the most common event category following medication administration.
     * 
     * @return the category with the highest count, or null if no events
     */
    public MedicalEventCategory getMostCommonEventCategory() {
        if (eventsByCategoryCount == null || eventsByCategoryCount.isEmpty()) {
            return null;
        }
        
        return eventsByCategoryCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    /**
     * Gets the most common event severity following medication administration.
     * 
     * @return the severity with the highest count, or null if no events
     */
    public MedicalEventSeverity getMostCommonEventSeverity() {
        if (eventsBySeverityCount == null || eventsBySeverityCount.isEmpty()) {
            return null;
        }
        
        return eventsBySeverityCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}