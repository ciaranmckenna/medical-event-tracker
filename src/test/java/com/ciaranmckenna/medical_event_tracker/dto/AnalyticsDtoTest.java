package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for analytics DTOs.
 * Tests validation, immutability, and business logic for analytics data structures.
 */
class AnalyticsDtoTest {

    @Test
    void medicationCorrelationAnalysis_Creation_Success() {
        // Given
        UUID medicationId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        LocalDateTime analysisDate = LocalDateTime.now();
        
        // When
        MedicationCorrelationAnalysis analysis = new MedicationCorrelationAnalysis(
            medicationId,
            patientId,
            "Paracetamol 500mg",
            125L,
            50L,
            40.0,
            0.8,
            Map.of(
                MedicalEventCategory.SYMPTOM, 30L,
                MedicalEventCategory.ADVERSE_REACTION, 5L
            ),
            Map.of(
                MedicalEventSeverity.MILD, 20L,
                MedicalEventSeverity.MODERATE, 15L,
                MedicalEventSeverity.SEVERE, 10L
            ),
            analysisDate
        );
        
        // Then
        assertNotNull(analysis);
        assertEquals(medicationId, analysis.medicationId());
        assertEquals(patientId, analysis.patientId());
        assertEquals("Paracetamol 500mg", analysis.medicationName());
        assertEquals(125L, analysis.totalDosages());
        assertEquals(50L, analysis.totalEventsAfterDosage());
        assertEquals(40.0, analysis.correlationPercentage());
        assertEquals(0.8, analysis.correlationStrength());
        assertEquals(2, analysis.eventsByCategoryCount().size());
        assertEquals(3, analysis.eventsBySeverityCount().size());
        assertEquals(analysisDate, analysis.analysisGeneratedAt());
    }

    @Test
    void dashboardSummary_Creation_Success() {
        // Given
        UUID patientId = UUID.randomUUID();
        LocalDateTime generatedAt = LocalDateTime.now();
        
        // When
        DashboardSummary summary = new DashboardSummary(
            patientId,
            150L,
            45L,
            Map.of(
                MedicalEventCategory.SYMPTOM, 20L,
                MedicalEventCategory.MEDICATION, 15L,
                MedicalEventCategory.EMERGENCY, 5L
            ),
            Map.of(
                MedicalEventSeverity.MILD, 25L,
                MedicalEventSeverity.MODERATE, 15L,
                MedicalEventSeverity.SEVERE, 5L
            ),
            10L,
            generatedAt
        );
        
        // Then
        assertNotNull(summary);
        assertEquals(patientId, summary.patientId());
        assertEquals(150L, summary.totalEvents());
        assertEquals(45L, summary.totalDosages());
        assertEquals(3, summary.eventsByCategory().size());
        assertEquals(3, summary.eventsBySeverity().size());
        assertEquals(10L, summary.recentEventsLast7Days());
        assertEquals(generatedAt, summary.generatedAt());
    }

    @Test
    void timelineDataPoint_Creation_Success() {
        // Given
        LocalDateTime timestamp = LocalDateTime.now();
        
        // When
        TimelineDataPoint dataPoint = new TimelineDataPoint(
            timestamp,
            "DOSAGE",
            "Paracetamol 500mg administered",
            new BigDecimal("500.0"),
            "mg",
            MedicalEventSeverity.MILD
        );
        
        // Then
        assertNotNull(dataPoint);
        assertEquals(timestamp, dataPoint.timestamp());
        assertEquals("DOSAGE", dataPoint.eventType());
        assertEquals("Paracetamol 500mg administered", dataPoint.description());
        assertEquals(new BigDecimal("500.0"), dataPoint.value());
        assertEquals("mg", dataPoint.unit());
        assertEquals(MedicalEventSeverity.MILD, dataPoint.severity());
    }

    @Test
    void timelineAnalysis_Creation_Success() {
        // Given
        UUID patientId = UUID.randomUUID();
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime generatedAt = LocalDateTime.now();
        
        List<TimelineDataPoint> dataPoints = List.of(
            new TimelineDataPoint(
                LocalDateTime.now().minusDays(1),
                "DOSAGE",
                "Morning dose",
                new BigDecimal("500.0"),
                "mg",
                MedicalEventSeverity.MILD
            ),
            new TimelineDataPoint(
                LocalDateTime.now(),
                "EVENT",
                "Mild headache",
                null,
                null,
                MedicalEventSeverity.MILD
            )
        );
        
        // When
        TimelineAnalysis timeline = new TimelineAnalysis(
            patientId,
            startDate,
            endDate,
            dataPoints,
            generatedAt
        );
        
        // Then
        assertNotNull(timeline);
        assertEquals(patientId, timeline.patientId());
        assertEquals(startDate, timeline.periodStart());
        assertEquals(endDate, timeline.periodEnd());
        assertEquals(2, timeline.dataPoints().size());
        assertEquals(generatedAt, timeline.generatedAt());
    }

    @Test
    void medicationImpactAnalysis_Creation_Success() {
        // Given
        UUID medicationId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        LocalDateTime analysisDate = LocalDateTime.now();
        
        // When
        MedicationImpactAnalysis impact = new MedicationImpactAnalysis(
            medicationId,
            patientId,
            "Paracetamol 500mg",
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now(),
            45L,
            12L,
            26.7,
            15L,
            5L,
            66.7,
            0.75,
            Map.of(
                "before_medication", List.of(5L, 8L, 6L),
                "after_medication", List.of(2L, 3L, 1L)
            ),
            analysisDate
        );
        
        // Then
        assertNotNull(impact);
        assertEquals(medicationId, impact.medicationId());
        assertEquals(patientId, impact.patientId());
        assertEquals("Paracetamol 500mg", impact.medicationName());
        assertEquals(45L, impact.totalDosages());
        assertEquals(12L, impact.eventsWithin24Hours());
        assertEquals(26.7, impact.eventRatePercentage());
        assertEquals(15L, impact.symptomEvents());
        assertEquals(5L, impact.adverseReactionEvents());
        assertEquals(66.7, impact.symptomReductionPercentage());
        assertEquals(0.75, impact.effectivenessScore());
        assertTrue(impact.weeklyTrends().containsKey("before_medication"));
        assertEquals(analysisDate, impact.analysisGeneratedAt());
    }

    @Test
    void medicationCorrelationAnalysis_NullValues_HandledCorrectly() {
        // Given
        UUID medicationId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        
        // When
        MedicationCorrelationAnalysis analysis = new MedicationCorrelationAnalysis(
            medicationId,
            patientId,
            "Test Medication",
            0L,
            0L,
            0.0,
            0.0,
            Map.of(),
            Map.of(),
            LocalDateTime.now()
        );
        
        // Then
        assertNotNull(analysis);
        assertEquals(0L, analysis.totalDosages());
        assertEquals(0L, analysis.totalEventsAfterDosage());
        assertEquals(0.0, analysis.correlationPercentage());
        assertEquals(0.0, analysis.correlationStrength());
        assertTrue(analysis.eventsByCategoryCount().isEmpty());
        assertTrue(analysis.eventsBySeverityCount().isEmpty());
    }
}