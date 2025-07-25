package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.dto.*;
import com.ciaranmckenna.medical_event_tracker.entity.*;
import com.ciaranmckenna.medical_event_tracker.repository.MedicalEventRepository;
import com.ciaranmckenna.medical_event_tracker.repository.MedicationDosageRepository;
import com.ciaranmckenna.medical_event_tracker.service.AnalyticsService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test class for AnalyticsServiceImpl.
 * Tests correlation analysis, dashboard summaries, timeline analysis, and medication impact analysis.
 */
@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private MedicalEventRepository medicalEventRepository;

    @Mock
    private MedicationDosageRepository medicationDosageRepository;

    private AnalyticsService analyticsService;

    private UUID testPatientId;
    private UUID testMedicationId;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsServiceImpl(medicalEventRepository, medicationDosageRepository);
        testPatientId = UUID.randomUUID();
        testMedicationId = UUID.randomUUID();
        testTime = LocalDateTime.now();
    }

    @Test
    void generateMedicationCorrelationAnalysis_Success() {
        // Given
        List<MedicationDosage> dosages = createTestDosages();
        List<MedicalEvent> events = createTestEvents();
        
        when(medicationDosageRepository.findByPatientIdAndMedicationId(testPatientId, testMedicationId))
                .thenReturn(dosages);
        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(eq(testPatientId), any(), any()))
                .thenReturn(events);

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
        when(medicalEventRepository.countByPatientId(testPatientId)).thenReturn(25L);
        when(medicationDosageRepository.countByPatientId(testPatientId)).thenReturn(15L);
        when(medicalEventRepository.countByPatientIdAndEventTimeAfter(eq(testPatientId), any()))
                .thenReturn(5L);
        when(medicalEventRepository.findByPatientId(testPatientId))
                .thenReturn(createTestEvents());

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
        
        List<MedicalEvent> events = createTestEvents();
        List<MedicationDosage> dosages = createTestDosages();
        
        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(testPatientId, startDate, endDate))
                .thenReturn(events);
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(testPatientId, startDate, endDate))
                .thenReturn(dosages);

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
        
        List<MedicationDosage> dosages = createTestDosages();
        List<MedicalEvent> eventsAfterDosages = createTestEvents();
        List<MedicalEvent> symptomEvents = List.of(createSymptomEvent());
        List<MedicalEvent> adverseEvents = List.of(createAdverseReactionEvent());
        
        when(medicationDosageRepository.findByPatientIdAndMedicationIdAndAdministrationTimeBetween(
                testPatientId, testMedicationId, startDate, endDate))
                .thenReturn(dosages);
        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(eq(testPatientId), any(), any()))
                .thenReturn(eventsAfterDosages);
        when(medicalEventRepository.findByPatientIdAndCategoryAndEventTimeBetween(
                testPatientId, MedicalEventCategory.SYMPTOM, startDate, endDate))
                .thenReturn(symptomEvents);
        when(medicalEventRepository.findByPatientIdAndCategoryAndEventTimeBetween(
                testPatientId, MedicalEventCategory.ADVERSE_REACTION, startDate, endDate))
                .thenReturn(adverseEvents);

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
        when(medicationDosageRepository.findByPatientIdAndMedicationId(testPatientId, testMedicationId))
                .thenReturn(List.of());

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
        
        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(testPatientId, startDate, endDate))
                .thenReturn(List.of());
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(testPatientId, startDate, endDate))
                .thenReturn(List.of());

        // When
        TimelineAnalysis result = analyticsService.generateTimelineAnalysis(testPatientId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(testPatientId, result.patientId());
        assertTrue(result.dataPoints().isEmpty());
        assertEquals(0, result.getTotalEvents());
        assertEquals(0, result.getTotalDosages());
    }

    private List<MedicationDosage> createTestDosages() {
        MedicationDosage dosage1 = new MedicationDosage();
        dosage1.setId(UUID.randomUUID());
        dosage1.setPatientId(testPatientId);
        dosage1.setMedicationId(testMedicationId);
        dosage1.setAdministrationTime(testTime.minusHours(6));
        dosage1.setDosageAmount(new BigDecimal("500.0"));
        dosage1.setDosageUnit("mg");
        dosage1.setSchedule(DosageSchedule.AM);

        MedicationDosage dosage2 = new MedicationDosage();
        dosage2.setId(UUID.randomUUID());
        dosage2.setPatientId(testPatientId);
        dosage2.setMedicationId(testMedicationId);
        dosage2.setAdministrationTime(testTime.minusHours(12));
        dosage2.setDosageAmount(new BigDecimal("500.0"));
        dosage2.setDosageUnit("mg");
        dosage2.setSchedule(DosageSchedule.PM);

        MedicationDosage dosage3 = new MedicationDosage();
        dosage3.setId(UUID.randomUUID());
        dosage3.setPatientId(testPatientId);
        dosage3.setMedicationId(testMedicationId);
        dosage3.setAdministrationTime(testTime.minusHours(18));
        dosage3.setDosageAmount(new BigDecimal("500.0"));
        dosage3.setDosageUnit("mg");
        dosage3.setSchedule(DosageSchedule.AM);

        return List.of(dosage1, dosage2, dosage3);
    }

    private List<MedicalEvent> createTestEvents() {
        MedicalEvent event1 = createSymptomEvent();
        MedicalEvent event2 = createAdverseReactionEvent();
        return List.of(event1, event2);
    }

    private MedicalEvent createSymptomEvent() {
        MedicalEvent event = new MedicalEvent();
        event.setId(UUID.randomUUID());
        event.setPatientId(testPatientId);
        event.setMedicationId(testMedicationId);
        event.setEventTime(testTime.minusHours(4));
        event.setTitle("Mild headache");
        event.setDescription("Patient reported mild headache 2 hours after medication");
        event.setSeverity(MedicalEventSeverity.MILD);
        event.setCategory(MedicalEventCategory.SYMPTOM);
        return event;
    }

    private MedicalEvent createAdverseReactionEvent() {
        MedicalEvent event = new MedicalEvent();
        event.setId(UUID.randomUUID());
        event.setPatientId(testPatientId);
        event.setMedicationId(testMedicationId);
        event.setEventTime(testTime.minusHours(8));
        event.setTitle("Nausea");
        event.setDescription("Patient experienced nausea after medication");
        event.setSeverity(MedicalEventSeverity.MODERATE);
        event.setCategory(MedicalEventCategory.ADVERSE_REACTION);
        return event;
    }
}