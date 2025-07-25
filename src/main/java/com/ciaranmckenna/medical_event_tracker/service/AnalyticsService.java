package com.ciaranmckenna.medical_event_tracker.service;

import com.ciaranmckenna.medical_event_tracker.dto.DashboardSummary;
import com.ciaranmckenna.medical_event_tracker.dto.MedicationCorrelationAnalysis;
import com.ciaranmckenna.medical_event_tracker.dto.MedicationImpactAnalysis;
import com.ciaranmckenna.medical_event_tracker.dto.TimelineAnalysis;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service interface for analytics and data visualization operations.
 * Provides correlation analysis, dashboard summaries, timeline analysis, and medication impact analysis.
 */
public interface AnalyticsService {

    /**
     * Generates correlation analysis between medication dosages and medical events for a specific patient and medication.
     * Analyzes the relationship between medication administration and subsequent medical events.
     *
     * @param patientId    the UUID of the patient
     * @param medicationId the UUID of the medication
     * @return correlation analysis with statistics and event categorization
     * @throws IllegalArgumentException if patientId or medicationId is null
     */
    MedicationCorrelationAnalysis generateMedicationCorrelationAnalysis(UUID patientId, UUID medicationId);

    /**
     * Generates a comprehensive dashboard summary for a patient.
     * Includes total counts, category breakdowns, severity distributions, and recent activity.
     *
     * @param patientId the UUID of the patient
     * @return dashboard summary with key metrics and statistics
     * @throws IllegalArgumentException if patientId is null
     */
    DashboardSummary generateDashboardSummary(UUID patientId);

    /**
     * Generates timeline analysis of medical events and medication dosages for a specified period.
     * Creates chronological data points suitable for visualization and correlation analysis.
     *
     * @param patientId the UUID of the patient
     * @param startDate the start of the analysis period
     * @param endDate   the end of the analysis period
     * @return timeline analysis with chronological data points
     * @throws IllegalArgumentException if any parameter is null or if startDate is after endDate
     */
    TimelineAnalysis generateTimelineAnalysis(UUID patientId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Generates detailed impact analysis for a specific medication over a specified period.
     * Analyzes effectiveness, side effects, symptom reduction, and weekly trends.
     *
     * @param patientId    the UUID of the patient
     * @param medicationId the UUID of the medication
     * @param startDate    the start of the analysis period
     * @param endDate      the end of the analysis period
     * @return medication impact analysis with effectiveness metrics and trends
     * @throws IllegalArgumentException if any parameter is null or if startDate is after endDate
     */
    MedicationImpactAnalysis generateMedicationImpactAnalysis(UUID patientId, UUID medicationId, 
                                                            LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Generates correlation analysis for all medications for a specific patient.
     * Provides a comprehensive view of how different medications correlate with medical events.
     *
     * @param patientId the UUID of the patient
     * @return list of correlation analyses for all medications taken by the patient
     * @throws IllegalArgumentException if patientId is null
     */
    java.util.List<MedicationCorrelationAnalysis> generateAllMedicationCorrelations(UUID patientId);

    /**
     * Generates weekly summary statistics for a patient over the last 8 weeks.
     * Useful for trend analysis and identifying patterns in medical events and medication effectiveness.
     *
     * @param patientId the UUID of the patient
     * @return map of weekly statistics with week identifiers as keys
     * @throws IllegalArgumentException if patientId is null
     */
    java.util.Map<String, DashboardSummary> generateWeeklySummaries(UUID patientId);
}