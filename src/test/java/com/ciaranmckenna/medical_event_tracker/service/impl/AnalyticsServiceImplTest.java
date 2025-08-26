package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.dto.*;
import com.ciaranmckenna.medical_event_tracker.entity.*;
import com.ciaranmckenna.medical_event_tracker.service.AnalyticsService;
import com.ciaranmckenna.medical_event_tracker.service.CorrelationService;
import com.ciaranmckenna.medical_event_tracker.service.DashboardService;
import com.ciaranmckenna.medical_event_tracker.service.TimelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Test class for AnalyticsServiceImpl.
 * Tests correlation analysis, dashboard summaries, timeline analysis, and medication impact analysis.
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private CorrelationService correlationService;

    @Mock
    private DashboardService dashboardService;

    @Mock
    private TimelineService timelineService;

    private AnalyticsService analyticsService;

    private UUID testPatientId;
    private UUID testMedicationId;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsServiceImpl(correlationService, dashboardService, timelineService);
        testPatientId = UUID.randomUUID();
        testMedicationId = UUID.randomUUID();
        testTime = LocalDateTime.now();
    }

    @Test
    void generateMedicationCorrelationAnalysis_Success() {
        // Given
        MedicationCorrelationAnalysis expectedAnalysis = createTestCorrelationAnalysis();
        when(correlationService.generateMedicationCorrelationAnalysis(testPatientId, testMedicationId))
                .thenReturn(expectedAnalysis);

        // When
        MedicationCorrelationAnalysis result = analyticsService.generateMedicationCorrelationAnalysis(
                testPatientId, testMedicationId);

        // Then
        assertNotNull(result);
        assertEquals(testPatientId, result.patientId());
        assertEquals(testMedicationId, result.medicationId());
        assertEquals(3L, result.totalDosages());
        assertEquals(2L, result.totalEventsAfterDosage());
        assertTrue(result.correlationPercentage() > 0);
        assertNotNull(result.eventsByCategoryCount());
        assertNotNull(result.eventsBySeverityCount());
        assertNotNull(result.analysisGeneratedAt());
    }

    @Test
    void generateDashboardSummary_Success() {
        // Given
        DashboardSummary expectedSummary = createTestDashboardSummary();
        when(dashboardService.generateDashboardSummary(testPatientId))
                .thenReturn(expectedSummary);

        // When
        DashboardSummary result = analyticsService.generateDashboardSummary(testPatientId);

        // Then
        assertNotNull(result);
        assertEquals(testPatientId, result.patientId());
        assertEquals(25L, result.totalEvents());
        assertEquals(15L, result.totalDosages());
        assertEquals(5L, result.recentEventsLast7Days());
        assertTrue(result.eventsByCategory().size() >= 1);
        assertTrue(result.eventsBySeverity().size() >= 1);
        assertNotNull(result.generatedAt());
    }

    @Test
    void generateTimelineAnalysis_Success() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        TimelineAnalysis expectedTimeline = createTestTimelineAnalysis(startDate, endDate);
        
        when(timelineService.generateTimelineAnalysis(testPatientId, startDate, endDate))
                .thenReturn(expectedTimeline);

        // When
        TimelineAnalysis result = analyticsService.generateTimelineAnalysis(testPatientId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(testPatientId, result.patientId());
        assertEquals(startDate, result.periodStart());
        assertEquals(endDate, result.periodEnd());
        assertFalse(result.dataPoints().isEmpty());
        assertEquals(5, result.dataPoints().size()); // 2 events + 3 dosages
        assertNotNull(result.generatedAt());
    }

    @Test
    void generateMedicationImpactAnalysis_Success() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        MedicationImpactAnalysis expectedImpact = createTestMedicationImpactAnalysis(startDate, endDate);
        
        when(correlationService.generateMedicationImpactAnalysis(testPatientId, testMedicationId, startDate, endDate))
                .thenReturn(expectedImpact);

        // When
        MedicationImpactAnalysis result = analyticsService.generateMedicationImpactAnalysis(
                testPatientId, testMedicationId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(testPatientId, result.patientId());
        assertEquals(testMedicationId, result.medicationId());
        assertEquals(startDate, result.analysisPeriodStart());
        assertEquals(endDate, result.analysisPeriodEnd());
        assertEquals(3L, result.totalDosages());
        assertEquals(2L, result.eventsWithin24Hours());
        assertTrue(result.eventRatePercentage() > 0);
        assertEquals(1L, result.symptomEvents());
        assertEquals(1L, result.adverseReactionEvents());
        assertNotNull(result.weeklyTrends());
        assertNotNull(result.analysisGeneratedAt());
    }

    @Test
    void generateMedicationCorrelationAnalysis_NoData_ReturnsEmptyAnalysis() {
        // Given
        MedicationCorrelationAnalysis emptyAnalysis = createEmptyCorrelationAnalysis();
        when(correlationService.generateMedicationCorrelationAnalysis(testPatientId, testMedicationId))
                .thenReturn(emptyAnalysis);

        // When
        MedicationCorrelationAnalysis result = analyticsService.generateMedicationCorrelationAnalysis(
                testPatientId, testMedicationId);

        // Then
        assertNotNull(result);
        assertEquals(0L, result.totalDosages());
        assertEquals(0L, result.totalEventsAfterDosage());
        assertEquals(0.0, result.correlationPercentage());
        assertEquals(0.0, result.correlationStrength());
        assertTrue(result.eventsByCategoryCount().isEmpty());
        assertTrue(result.eventsBySeverityCount().isEmpty());
    }

    @Test
    void generateTimelineAnalysis_EmptyPeriod_ReturnsEmptyTimeline() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        TimelineAnalysis emptyTimeline = createEmptyTimelineAnalysis(startDate, endDate);
        
        when(timelineService.generateTimelineAnalysis(testPatientId, startDate, endDate))
                .thenReturn(emptyTimeline);

        // When
        TimelineAnalysis result = analyticsService.generateTimelineAnalysis(testPatientId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(testPatientId, result.patientId());
        assertTrue(result.dataPoints().isEmpty());
        assertEquals(0, result.getTotalEvents());
        assertEquals(0, result.getTotalDosages());
    }

    private MedicationCorrelationAnalysis createTestCorrelationAnalysis() {
        return new MedicationCorrelationAnalysis(
                testMedicationId,
                testPatientId,
                "Test Medication",
                3L,
                2L,
                66.67,
                0.75,
                Map.of(MedicalEventCategory.SYMPTOM, 1L, MedicalEventCategory.ADVERSE_REACTION, 1L),
                Map.of(MedicalEventSeverity.MILD, 1L, MedicalEventSeverity.MODERATE, 1L),
                testTime
        );
    }

    private MedicationCorrelationAnalysis createEmptyCorrelationAnalysis() {
        return new MedicationCorrelationAnalysis(
                testMedicationId,
                testPatientId,
                "Test Medication",
                0L,
                0L,
                0.0,
                0.0,
                Map.of(),
                Map.of(),
                testTime
        );
    }

    private DashboardSummary createTestDashboardSummary() {
        return new DashboardSummary(
                testPatientId,
                25L,
                15L,
                Map.of(MedicalEventCategory.SYMPTOM, 15L, MedicalEventCategory.ADVERSE_REACTION, 10L),
                Map.of(MedicalEventSeverity.MILD, 10L, MedicalEventSeverity.MODERATE, 10L, MedicalEventSeverity.SEVERE, 5L),
                5L,
                testTime
        );
    }

    private TimelineAnalysis createTestTimelineAnalysis(LocalDateTime startDate, LocalDateTime endDate) {
        List<TimelineDataPoint> dataPoints = List.of(
                new TimelineDataPoint(testTime.minusHours(4), "EVENT", "Mild headache", new BigDecimal("0"), null, MedicalEventSeverity.MILD),
                new TimelineDataPoint(testTime.minusHours(8), "EVENT", "Nausea", new BigDecimal("0"), null, MedicalEventSeverity.MODERATE),
                new TimelineDataPoint(testTime.minusHours(6), "DOSAGE", "500mg medication", new BigDecimal("500"), "mg", null),
                new TimelineDataPoint(testTime.minusHours(12), "DOSAGE", "500mg medication", new BigDecimal("500"), "mg", null),
                new TimelineDataPoint(testTime.minusHours(18), "DOSAGE", "500mg medication", new BigDecimal("500"), "mg", null)
        );
        
        return new TimelineAnalysis(
                testPatientId,
                startDate,
                endDate,
                dataPoints,
                testTime
        );
    }

    private TimelineAnalysis createEmptyTimelineAnalysis(LocalDateTime startDate, LocalDateTime endDate) {
        return new TimelineAnalysis(
                testPatientId,
                startDate,
                endDate,
                List.of(),
                testTime
        );
    }

    private MedicationImpactAnalysis createTestMedicationImpactAnalysis(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, List<Long>> weeklyTrends = Map.of(
                "week1", List.of(2L, 1L),
                "week2", List.of(1L, 0L),
                "week3", List.of(0L, 1L),
                "week4", List.of(1L, 0L)
        );
        
        return new MedicationImpactAnalysis(
                testMedicationId,
                testPatientId,
                "Test Medication",
                startDate,
                endDate,
                3L,
                2L,
                66.67,
                1L,
                1L,
                75.0,
                0.8,
                weeklyTrends,
                testTime
        );
    }
}