package com.ciaranmckenna.medical_event_tracker.controller;

import com.ciaranmckenna.medical_event_tracker.dto.DashboardSummary;
import com.ciaranmckenna.medical_event_tracker.dto.MedicationCorrelationAnalysis;
import com.ciaranmckenna.medical_event_tracker.dto.MedicationImpactAnalysis;
import com.ciaranmckenna.medical_event_tracker.dto.TimelineAnalysis;
import com.ciaranmckenna.medical_event_tracker.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for analytics and data visualization endpoints.
 * Provides comprehensive analytics capabilities for medical event tracking.
 */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Generate dashboard summary for a patient.
     *
     * @param patientId the patient's UUID
     * @return dashboard summary with key metrics
     */
    @GetMapping("/dashboard/{patientId}")
    public ResponseEntity<DashboardSummary> getDashboardSummary(@PathVariable UUID patientId) {
        DashboardSummary summary = analyticsService.generateDashboardSummary(patientId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Generate correlation analysis between a specific medication and medical events.
     *
     * @param patientId    the patient's UUID
     * @param medicationId the medication's UUID
     * @return medication correlation analysis
     */
    @GetMapping("/correlation/{patientId}/medication/{medicationId}")
    public ResponseEntity<MedicationCorrelationAnalysis> getMedicationCorrelationAnalysis(
            @PathVariable UUID patientId,
            @PathVariable UUID medicationId) {
        
        MedicationCorrelationAnalysis analysis = analyticsService.generateMedicationCorrelationAnalysis(
                patientId, medicationId);
        return ResponseEntity.ok(analysis);
    }

    /**
     * Generate correlation analysis for all medications for a patient.
     *
     * @param patientId the patient's UUID
     * @return list of correlation analyses for all medications
     */
    @GetMapping("/correlation/{patientId}/all-medications")
    public ResponseEntity<List<MedicationCorrelationAnalysis>> getAllMedicationCorrelations(
            @PathVariable UUID patientId) {
        
        List<MedicationCorrelationAnalysis> analyses = analyticsService.generateAllMedicationCorrelations(patientId);
        return ResponseEntity.ok(analyses);
    }

    /**
     * Generate timeline analysis for a patient within a specified date range.
     *
     * @param patientId the patient's UUID
     * @param startDate the start date (ISO format: 2023-01-01T00:00:00)
     * @param endDate   the end date (ISO format: 2023-12-31T23:59:59)
     * @return timeline analysis with chronological data points
     */
    @GetMapping("/timeline/{patientId}")
    public ResponseEntity<TimelineAnalysis> getTimelineAnalysis(
            @PathVariable UUID patientId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        
        TimelineAnalysis timeline = analyticsService.generateTimelineAnalysis(patientId, startDate, endDate);
        return ResponseEntity.ok(timeline);
    }

    /**
     * Generate medication impact analysis for a specific medication and time period.
     *
     * @param patientId    the patient's UUID
     * @param medicationId the medication's UUID
     * @param startDate    the start date (ISO format: 2023-01-01T00:00:00)
     * @param endDate      the end date (ISO format: 2023-12-31T23:59:59)
     * @return medication impact analysis with effectiveness metrics
     */
    @GetMapping("/impact/{patientId}/medication/{medicationId}")
    public ResponseEntity<MedicationImpactAnalysis> getMedicationImpactAnalysis(
            @PathVariable UUID patientId,
            @PathVariable UUID medicationId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        
        MedicationImpactAnalysis impact = analyticsService.generateMedicationImpactAnalysis(
                patientId, medicationId, startDate, endDate);
        return ResponseEntity.ok(impact);
    }

    /**
     * Generate weekly summaries for the last 8 weeks for trend analysis.
     *
     * @param patientId the patient's UUID
     * @return map of weekly summaries with week identifiers
     */
    @GetMapping("/weekly-trends/{patientId}")
    public ResponseEntity<Map<String, DashboardSummary>> getWeeklyTrends(@PathVariable UUID patientId) {
        Map<String, DashboardSummary> weeklyTrends = analyticsService.generateWeeklySummaries(patientId);
        return ResponseEntity.ok(weeklyTrends);
    }

    /**
     * Generate comprehensive analytics overview for a patient.
     * Combines dashboard summary with correlation analysis for all medications.
     *
     * @param patientId the patient's UUID
     * @return comprehensive analytics overview
     */
    @GetMapping("/overview/{patientId}")
    public ResponseEntity<AnalyticsOverviewResponse> getAnalyticsOverview(@PathVariable UUID patientId) {
        DashboardSummary dashboard = analyticsService.generateDashboardSummary(patientId);
        List<MedicationCorrelationAnalysis> correlations = analyticsService.generateAllMedicationCorrelations(patientId);
        
        AnalyticsOverviewResponse overview = new AnalyticsOverviewResponse(
                dashboard,
                correlations,
                LocalDateTime.now()
        );
        
        return ResponseEntity.ok(overview);
    }

    /**
     * DTO for comprehensive analytics overview response.
     */
    public record AnalyticsOverviewResponse(
            DashboardSummary dashboardSummary,
            List<MedicationCorrelationAnalysis> medicationCorrelations,
            LocalDateTime generatedAt
    ) {}
}