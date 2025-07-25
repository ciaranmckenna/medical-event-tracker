package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO representing a comprehensive dashboard summary for a patient.
 * Provides high-level statistics and metrics for dashboard display.
 */
public record DashboardSummary(
        
        @NotNull(message = "Patient ID is required for dashboard summary")
        UUID patientId,
        
        @NotNull(message = "Total events count is required")
        Long totalEvents,
        
        @NotNull(message = "Total dosages count is required")
        Long totalDosages,
        
        @NotNull(message = "Events by category breakdown is required")
        Map<MedicalEventCategory, Long> eventsByCategory,
        
        @NotNull(message = "Events by severity breakdown is required")
        Map<MedicalEventSeverity, Long> eventsBySeverity,
        
        @NotNull(message = "Recent events count is required")
        Long recentEventsLast7Days,
        
        @NotNull(message = "Generation timestamp is required")
        LocalDateTime generatedAt
) {
    
    /**
     * Calculates the average events per day based on total events.
     * Assumes data spans 30 days for calculation.
     * 
     * @return average events per day over 30-day period
     */
    public double getAverageEventsPerDay() {
        return totalEvents != null ? totalEvents / 30.0 : 0.0;
    }
    
    /**
     * Calculates the average dosages per day based on total dosages.
     * Assumes data spans 30 days for calculation.
     * 
     * @return average dosages per day over 30-day period
     */
    public double getAverageDosagesPerDay() {
        return totalDosages != null ? totalDosages / 30.0 : 0.0;
    }
    
    /**
     * Checks if there has been increased activity in the last 7 days.
     * 
     * @return true if recent events represent more than 30% of total events
     */
    public boolean hasIncreasedRecentActivity() {
        if (totalEvents == null || totalEvents == 0 || recentEventsLast7Days == null) {
            return false;
        }
        return ((double) recentEventsLast7Days / totalEvents) > 0.3;
    }
    
    /**
     * Gets the most common event category.
     * 
     * @return the category with the highest count, or null if no events
     */
    public MedicalEventCategory getMostCommonEventCategory() {
        if (eventsByCategory == null || eventsByCategory.isEmpty()) {
            return null;
        }
        
        return eventsByCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    /**
     * Gets the most common event severity.
     * 
     * @return the severity with the highest count, or null if no events
     */
    public MedicalEventSeverity getMostCommonEventSeverity() {
        if (eventsBySeverity == null || eventsBySeverity.isEmpty()) {
            return null;
        }
        
        return eventsBySeverity.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
    
    /**
     * Calculates the percentage of critical or severe events.
     * 
     * @return percentage of high-severity events
     */
    public double getHighSeverityEventPercentage() {
        if (eventsBySeverity == null || totalEvents == null || totalEvents == 0) {
            return 0.0;
        }
        
        Long severeEvents = eventsBySeverity.getOrDefault(MedicalEventSeverity.SEVERE, 0L);
        Long criticalEvents = eventsBySeverity.getOrDefault(MedicalEventSeverity.CRITICAL, 0L);
        
        return ((double) (severeEvents + criticalEvents) / totalEvents) * 100.0;
    }
}