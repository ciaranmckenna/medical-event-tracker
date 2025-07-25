package com.ciaranmckenna.medical_event_tracker.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTO representing an analysis of medication impact on patient symptoms and events.
 * Provides detailed metrics about medication effectiveness and side effects.
 */
public record MedicationImpactAnalysis(
        
        @NotNull(message = "Medication ID is required for impact analysis")
        UUID medicationId,
        
        @NotNull(message = "Patient ID is required for impact analysis")
        UUID patientId,
        
        @NotNull(message = "Medication name is required for impact analysis")
        String medicationName,
        
        @NotNull(message = "Analysis period start is required")
        LocalDateTime analysisPeriodStart,
        
        @NotNull(message = "Analysis period end is required")
        LocalDateTime analysisPeriodEnd,
        
        @NotNull(message = "Total dosages count is required")
        Long totalDosages,
        
        @NotNull(message = "Events within 24 hours count is required")
        Long eventsWithin24Hours,
        
        @NotNull(message = "Event rate percentage is required")
        Double eventRatePercentage,
        
        @NotNull(message = "Symptom events count is required")
        Long symptomEvents,
        
        @NotNull(message = "Adverse reaction events count is required")
        Long adverseReactionEvents,
        
        @NotNull(message = "Symptom reduction percentage is required")
        Double symptomReductionPercentage,
        
        @NotNull(message = "Effectiveness score is required")
        Double effectivenessScore,
        
        @NotNull(message = "Weekly trends data is required")
        Map<String, List<Long>> weeklyTrends,
        
        @NotNull(message = "Analysis generation timestamp is required")
        LocalDateTime analysisGeneratedAt
) {
    
    /**
     * Checks if the medication shows high effectiveness.
     * 
     * @return true if effectiveness score is above 0.7 (70%)
     */
    public boolean isHighlyEffective() {
        return effectivenessScore != null && effectivenessScore >= 0.7;
    }
    
    /**
     * Checks if the medication has concerning side effects.
     * 
     * @return true if adverse reactions represent more than 25% of total events
     */
    public boolean hasConcerningSideEffects() {
        if (eventsWithin24Hours == null || eventsWithin24Hours == 0 || adverseReactionEvents == null) {
            return false;
        }
        return ((double) adverseReactionEvents / eventsWithin24Hours) > 0.25;
    }
    
    /**
     * Checks if the medication shows good symptom control.
     * 
     * @return true if symptom reduction percentage is above 50%
     */
    public boolean showsGoodSymptomControl() {
        return symptomReductionPercentage != null && symptomReductionPercentage >= 50.0;
    }
    
    /**
     * Calculates the adverse reaction rate as a percentage.
     * 
     * @return percentage of adverse reactions relative to total events
     */
    public double getAdverseReactionRate() {
        if (eventsWithin24Hours == null || eventsWithin24Hours == 0 || adverseReactionEvents == null) {
            return 0.0;
        }
        return ((double) adverseReactionEvents / eventsWithin24Hours) * 100.0;
    }
    
    /**
     * Calculates the symptom event rate as a percentage.
     * 
     * @return percentage of symptom events relative to total events
     */
    public double getSymptomEventRate() {
        if (eventsWithin24Hours == null || eventsWithin24Hours == 0 || symptomEvents == null) {
            return 0.0;
        }
        return ((double) symptomEvents / eventsWithin24Hours) * 100.0;
    }
    
    /**
     * Gets the analysis period duration in days.
     * 
     * @return number of days in the analysis period
     */
    public long getAnalysisPeriodDays() {
        if (analysisPeriodStart == null || analysisPeriodEnd == null) {
            return 0;
        }
        return java.time.Duration.between(analysisPeriodStart, analysisPeriodEnd).toDays();
    }
    
    /**
     * Calculates the average dosages per day during the analysis period.
     * 
     * @return average daily dosage count
     */
    public double getAverageDosagesPerDay() {
        long periodDays = getAnalysisPeriodDays();
        if (periodDays == 0 || totalDosages == null) {
            return 0.0;
        }
        return (double) totalDosages / periodDays;
    }
    
    /**
     * Gets the medication effectiveness category based on the effectiveness score.
     * 
     * @return effectiveness category as string
     */
    public String getEffectivenessCategory() {
        if (effectivenessScore == null) {
            return "UNKNOWN";
        }
        
        if (effectivenessScore >= 0.8) {
            return "EXCELLENT";
        } else if (effectivenessScore >= 0.6) {
            return "GOOD";
        } else if (effectivenessScore >= 0.4) {
            return "MODERATE";
        } else if (effectivenessScore >= 0.2) {
            return "POOR";
        } else {
            return "INEFFECTIVE";
        }
    }
}