package com.ciaranmckenna.medical_event_tracker.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO representing a timeline analysis of medical events and medication dosages.
 * Provides chronological data for visualization and correlation analysis.
 */
public record TimelineAnalysis(
        
        @NotNull(message = "Patient ID is required for timeline analysis")
        UUID patientId,
        
        @NotNull(message = "Period start date is required for timeline analysis")
        LocalDateTime periodStart,
        
        @NotNull(message = "Period end date is required for timeline analysis")
        LocalDateTime periodEnd,
        
        @NotNull(message = "Data points list is required for timeline analysis")
        List<TimelineDataPoint> dataPoints,
        
        @NotNull(message = "Generation timestamp is required for timeline analysis")
        LocalDateTime generatedAt
) {
    
    /**
     * Gets all dosage data points from the timeline.
     * 
     * @return list of dosage-related data points
     */
    public List<TimelineDataPoint> getDosagePoints() {
        return dataPoints.stream()
                .filter(TimelineDataPoint::isDosage)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all medical event data points from the timeline.
     * 
     * @return list of medical event-related data points
     */
    public List<TimelineDataPoint> getEventPoints() {
        return dataPoints.stream()
                .filter(TimelineDataPoint::isMedicalEvent)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all high-severity event data points from the timeline.
     * 
     * @return list of severe or critical event data points
     */
    public List<TimelineDataPoint> getHighSeverityEvents() {
        return dataPoints.stream()
                .filter(TimelineDataPoint::isHighSeverity)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculates the total number of dosages in the timeline.
     * 
     * @return count of dosage data points
     */
    public long getTotalDosages() {
        return getDosagePoints().size();
    }
    
    /**
     * Calculates the total number of medical events in the timeline.
     * 
     * @return count of medical event data points
     */
    public long getTotalEvents() {
        return getEventPoints().size();
    }
    
    /**
     * Calculates the analysis period duration in days.
     * 
     * @return number of days between period start and end
     */
    public long getAnalysisPeriodDays() {
        return java.time.Duration.between(periodStart, periodEnd).toDays();
    }
    
    /**
     * Checks if there are data points in the timeline.
     * 
     * @return true if dataPoints list is not empty
     */
    public boolean hasData() {
        return dataPoints != null && !dataPoints.isEmpty();
    }
    
    /**
     * Gets data points sorted chronologically.
     * 
     * @return list of data points sorted by timestamp
     */
    public List<TimelineDataPoint> getChronologicalDataPoints() {
        return dataPoints.stream()
                .sorted((dp1, dp2) -> dp1.timestamp().compareTo(dp2.timestamp()))
                .collect(Collectors.toList());
    }
}