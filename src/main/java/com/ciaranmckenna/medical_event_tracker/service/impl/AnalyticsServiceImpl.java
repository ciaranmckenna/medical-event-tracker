package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.dto.*;
import com.ciaranmckenna.medical_event_tracker.service.AnalyticsService;
import com.ciaranmckenna.medical_event_tracker.service.CorrelationService;
import com.ciaranmckenna.medical_event_tracker.service.DashboardService;
import com.ciaranmckenna.medical_event_tracker.service.TimelineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of AnalyticsService that delegates to focused services.
 * Acts as a facade for correlation, dashboard, and timeline services.
 * Updated to follow Single Responsibility Principle.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final CorrelationService correlationService;
    private final DashboardService dashboardService;
    private final TimelineService timelineService;

    public AnalyticsServiceImpl(CorrelationService correlationService,
                               DashboardService dashboardService,
                               TimelineService timelineService) {
        this.correlationService = correlationService;
        this.dashboardService = dashboardService;
        this.timelineService = timelineService;
    }

    @Override
    public MedicationCorrelationAnalysis generateMedicationCorrelationAnalysis(UUID patientId, UUID medicationId) {
        return correlationService.generateMedicationCorrelationAnalysis(patientId, medicationId);
    }

    @Override
    public DashboardSummary generateDashboardSummary(UUID patientId) {
        return dashboardService.generateDashboardSummary(patientId);
    }

    @Override
    public TimelineAnalysis generateTimelineAnalysis(UUID patientId, LocalDateTime startDate, LocalDateTime endDate) {
        return timelineService.generateTimelineAnalysis(patientId, startDate, endDate);
    }

    @Override
    public MedicationImpactAnalysis generateMedicationImpactAnalysis(UUID patientId, UUID medicationId, 
                                                                   LocalDateTime startDate, LocalDateTime endDate) {
        return correlationService.generateMedicationImpactAnalysis(patientId, medicationId, startDate, endDate);
    }

    @Override
    public List<MedicationCorrelationAnalysis> generateAllMedicationCorrelations(UUID patientId) {
        return correlationService.generateAllMedicationCorrelations(patientId);
    }

    @Override
    public Map<String, DashboardSummary> generateWeeklySummaries(UUID patientId) {
        return dashboardService.generateWeeklySummaries(patientId);
    }
}