package com.ciaranmckenna.medical_event_tracker.service;

import com.ciaranmckenna.medical_event_tracker.dto.DashboardSummary;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;

import java.util.Map;
import java.util.UUID;

/**
 * Service interface for dashboard-related analytics.
 * Focused on summary statistics and dashboard metrics.
 */
public interface DashboardService {

    /**
     * Generate comprehensive dashboard summary for a patient.
     * 
     * @param patientId the patient's UUID
     * @return dashboard summary with key metrics
     */
    DashboardSummary generateDashboardSummary(UUID patientId);

    /**
     * Generate weekly summaries for trend analysis.
     * 
     * @param patientId the patient's UUID
     * @return map of weekly summaries with week identifiers
     */
    Map<String, DashboardSummary> generateWeeklySummaries(UUID patientId);

    /**
     * Get events grouped by category for a patient.
     * 
     * @param patientId the patient's UUID
     * @return map of event categories to counts
     */
    Map<MedicalEventCategory, Long> getEventsByCategory(UUID patientId);

    /**
     * Get events grouped by severity for a patient.
     * 
     * @param patientId the patient's UUID
     * @return map of event severities to counts
     */
    Map<MedicalEventSeverity, Long> getEventsBySeverity(UUID patientId);

    /**
     * Calculate key metrics for a patient.
     * 
     * @param patientId the patient's UUID
     * @return array of key metrics [totalEvents, totalDosages, recentEvents]
     */
    long[] calculateKeyMetrics(UUID patientId);

    /**
     * Get recent events count for a patient (last N days).
     * 
     * @param patientId the patient's UUID
     * @param daysBack number of days to look back
     * @return count of recent events
     */
    long getRecentEventsCount(UUID patientId, int daysBack);
}