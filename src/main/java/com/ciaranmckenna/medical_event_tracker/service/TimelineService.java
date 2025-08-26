package com.ciaranmckenna.medical_event_tracker.service;

import com.ciaranmckenna.medical_event_tracker.dto.TimelineAnalysis;
import com.ciaranmckenna.medical_event_tracker.dto.TimelineDataPoint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for timeline analysis.
 * Focused on chronological data analysis and timeline generation.
 */
public interface TimelineService {

    /**
     * Generate timeline analysis for a patient within a specified date range.
     * 
     * @param patientId the patient's UUID
     * @param startDate the start date for analysis
     * @param endDate the end date for analysis
     * @return timeline analysis with chronological data points
     */
    TimelineAnalysis generateTimelineAnalysis(UUID patientId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Create timeline data points from medical events and dosages.
     * 
     * @param patientId the patient's UUID
     * @param startDate the start date for data collection
     * @param endDate the end date for data collection
     * @return list of timeline data points
     */
    List<TimelineDataPoint> createTimelineDataPoints(UUID patientId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Generate timeline for a specific medication.
     * 
     * @param patientId the patient's UUID
     * @param medicationId the medication's UUID
     * @param startDate the start date for analysis
     * @param endDate the end date for analysis
     * @return timeline analysis focused on the specific medication
     */
    TimelineAnalysis generateMedicationTimeline(
            UUID patientId, 
            UUID medicationId, 
            LocalDateTime startDate, 
            LocalDateTime endDate
    );

    /**
     * Calculate timeline statistics (total events, patterns, etc.).
     * 
     * @param dataPoints the timeline data points
     * @return statistical summary of the timeline
     */
    Map<String, Object> calculateTimelineStatistics(List<TimelineDataPoint> dataPoints);

    /**
     * Identify patterns in timeline data.
     * 
     * @param dataPoints the timeline data points
     * @return list of identified patterns
     */
    List<String> identifyTimelinePatterns(List<TimelineDataPoint> dataPoints);
}