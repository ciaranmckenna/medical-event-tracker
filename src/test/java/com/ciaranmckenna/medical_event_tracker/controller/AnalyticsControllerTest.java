package com.ciaranmckenna.medical_event_tracker.controller;

import com.ciaranmckenna.medical_event_tracker.dto.DashboardSummary;
import com.ciaranmckenna.medical_event_tracker.dto.MedicationCorrelationAnalysis;
import com.ciaranmckenna.medical_event_tracker.dto.MedicationImpactAnalysis;
import com.ciaranmckenna.medical_event_tracker.dto.TimelineAnalysis;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import com.ciaranmckenna.medical_event_tracker.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test class for AnalyticsController.
 * Tests REST endpoints for analytics and data visualization functionality.
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController analyticsController;

    private final UUID testPatientId = UUID.randomUUID();
    private final UUID testMedicationId = UUID.randomUUID();
    private final LocalDateTime testTime = LocalDateTime.now();

    @Test
    void getDashboardSummary_Success() {
        // Given
        DashboardSummary summary = new DashboardSummary(
                testPatientId,
                25L,
                15L,
                Map.of(MedicalEventCategory.SYMPTOM, 10L, MedicalEventCategory.MEDICATION, 8L),
                Map.of(MedicalEventSeverity.MILD, 15L, MedicalEventSeverity.MODERATE, 7L),
                5L,
                testTime
        );

        when(analyticsService.generateDashboardSummary(testPatientId)).thenReturn(summary);

        // When
        ResponseEntity<DashboardSummary> response = analyticsController.getDashboardSummary(testPatientId);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(testPatientId, response.getBody().patientId());
        assertEquals(25L, response.getBody().totalEvents());
        assertEquals(15L, response.getBody().totalDosages());
        assertEquals(5L, response.getBody().recentEventsLast7Days());
    }

    @Test
    void getMedicationCorrelationAnalysis_Success() {
        // Given
        MedicationCorrelationAnalysis analysis = new MedicationCorrelationAnalysis(
                testMedicationId,
                testPatientId,
                "Test Medication",
                10L,
                5L,
                50.0,
                0.8,
                Map.of(MedicalEventCategory.SYMPTOM, 3L, MedicalEventCategory.ADVERSE_REACTION, 2L),
                Map.of(MedicalEventSeverity.MILD, 3L, MedicalEventSeverity.MODERATE, 2L),
                testTime
        );

        when(analyticsService.generateMedicationCorrelationAnalysis(testPatientId, testMedicationId))
                .thenReturn(analysis);

        // When
        ResponseEntity<MedicationCorrelationAnalysis> response = analyticsController.getMedicationCorrelationAnalysis(
                testPatientId, testMedicationId);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(testMedicationId, response.getBody().medicationId());
        assertEquals(testPatientId, response.getBody().patientId());
        assertEquals("Test Medication", response.getBody().medicationName());
        assertEquals(10L, response.getBody().totalDosages());
        assertEquals(5L, response.getBody().totalEventsAfterDosage());
        assertEquals(50.0, response.getBody().correlationPercentage());
        assertEquals(0.8, response.getBody().correlationStrength());
    }

    @Test
    void getAllMedicationCorrelations_Success() {
        // Given
        List<MedicationCorrelationAnalysis> analyses = List.of(
                new MedicationCorrelationAnalysis(
                        testMedicationId,
                        testPatientId,
                        "Medication A",
                        10L, 5L, 50.0, 0.8,
                        Map.of(MedicalEventCategory.SYMPTOM, 3L),
                        Map.of(MedicalEventSeverity.MILD, 3L),
                        testTime
                )
        );

        when(analyticsService.generateAllMedicationCorrelations(testPatientId)).thenReturn(analyses);

        // When
        ResponseEntity<List<MedicationCorrelationAnalysis>> response = analyticsController.getAllMedicationCorrelations(testPatientId);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Medication A", response.getBody().get(0).medicationName());
        assertEquals(testPatientId, response.getBody().get(0).patientId());
    }

    @Test
    void getTimelineAnalysis_Success() {
        // Given
        LocalDateTime startDate = testTime.minusDays(7);
        LocalDateTime endDate = testTime;
        
        TimelineAnalysis timeline = new TimelineAnalysis(
                testPatientId,
                startDate,
                endDate,
                List.of(),
                testTime
        );

        when(analyticsService.generateTimelineAnalysis(eq(testPatientId), any(), any()))
                .thenReturn(timeline);

        // When
        ResponseEntity<TimelineAnalysis> response = analyticsController.getTimelineAnalysis(
                testPatientId, startDate, endDate);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(testPatientId, response.getBody().patientId());
        assertEquals(startDate, response.getBody().periodStart());
        assertEquals(endDate, response.getBody().periodEnd());
    }

    @Test
    void getMedicationImpactAnalysis_Success() {
        // Given
        LocalDateTime startDate = testTime.minusDays(30);
        LocalDateTime endDate = testTime;
        
        MedicationImpactAnalysis impact = new MedicationImpactAnalysis(
                testMedicationId,
                testPatientId,
                "Test Medication",
                startDate,
                endDate,
                20L, 8L, 40.0, 5L, 2L, 60.0, 0.75,
                Map.of("before_medication", List.of(5L, 8L), "after_medication", List.of(2L, 3L)),
                testTime
        );

        when(analyticsService.generateMedicationImpactAnalysis(
                eq(testPatientId), eq(testMedicationId), any(), any()))
                .thenReturn(impact);

        // When
        ResponseEntity<MedicationImpactAnalysis> response = analyticsController.getMedicationImpactAnalysis(
                testPatientId, testMedicationId, startDate, endDate);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(testMedicationId, response.getBody().medicationId());
        assertEquals(testPatientId, response.getBody().patientId());
        assertEquals(20L, response.getBody().totalDosages());
        assertEquals(0.75, response.getBody().effectivenessScore());
    }

    @Test
    void getWeeklyTrends_Success() {
        // Given
        Map<String, DashboardSummary> weeklyTrends = Map.of(
                "week_1", new DashboardSummary(testPatientId, 5L, 3L, Map.of(), Map.of(), 2L, testTime),
                "week_2", new DashboardSummary(testPatientId, 7L, 4L, Map.of(), Map.of(), 3L, testTime)
        );

        when(analyticsService.generateWeeklySummaries(testPatientId)).thenReturn(weeklyTrends);

        // When
        ResponseEntity<Map<String, DashboardSummary>> response = analyticsController.getWeeklyTrends(testPatientId);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(5L, response.getBody().get("week_1").totalEvents());
        assertEquals(7L, response.getBody().get("week_2").totalEvents());
    }

    @Test
    void getAnalyticsOverview_Success() {
        // Given
        DashboardSummary dashboard = new DashboardSummary(
                testPatientId, 25L, 15L, Map.of(), Map.of(), 5L, testTime);
        List<MedicationCorrelationAnalysis> correlations = List.of();

        when(analyticsService.generateDashboardSummary(testPatientId)).thenReturn(dashboard);
        when(analyticsService.generateAllMedicationCorrelations(testPatientId)).thenReturn(correlations);

        // When
        ResponseEntity<AnalyticsController.AnalyticsOverviewResponse> response = analyticsController.getAnalyticsOverview(testPatientId);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(testPatientId, response.getBody().dashboardSummary().patientId());
        assertEquals(25L, response.getBody().dashboardSummary().totalEvents());
        assertNotNull(response.getBody().medicationCorrelations());
        assertNotNull(response.getBody().generatedAt());
    }
}