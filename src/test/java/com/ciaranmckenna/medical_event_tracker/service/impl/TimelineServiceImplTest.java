package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.dto.TimelineDataPoint;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEvent;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import com.ciaranmckenna.medical_event_tracker.entity.MedicationDosage;
import com.ciaranmckenna.medical_event_tracker.repository.MedicalEventRepository;
import com.ciaranmckenna.medical_event_tracker.repository.MedicationDosageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TimelineServiceImpl.
 * Focuses on BMI calculation and timeline data point creation.
 */
@ExtendWith(MockitoExtension.class)
class TimelineServiceImplTest {

    @Mock
    private MedicalEventRepository medicalEventRepository;

    @Mock
    private MedicationDosageRepository medicationDosageRepository;

    @InjectMocks
    private TimelineServiceImpl timelineService;

    private UUID patientId;
    private UUID medicationId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        medicationId = UUID.randomUUID();
        startDate = LocalDateTime.now().minusDays(7);
        endDate = LocalDateTime.now();
    }

    // ========== BMI Calculation Tests ==========

    @Test
    void createTimelineDataPoints_WithValidWeightAndHeight_CalculatesBMI() {
        // Given - Medical event with valid weight (70.5kg) and height (175cm)
        // Expected BMI: 70.5 / (1.75 * 1.75) = 23.0
        MedicalEvent event = createMedicalEvent(
                "Test Event",
                new BigDecimal("70.5"),  // weight in kg
                new BigDecimal("175.0")  // height in cm
        );

        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList(event));
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList());

        // When
        List<TimelineDataPoint> dataPoints = timelineService.createTimelineDataPoints(
                patientId, startDate, endDate);

        // Then
        assertThat(dataPoints).hasSize(1);
        TimelineDataPoint dataPoint = dataPoints.get(0);

        assertThat(dataPoint.eventType()).isEqualTo("EVENT");
        assertThat(dataPoint.bmi()).isNotNull();
        assertThat(dataPoint.bmi()).isEqualByComparingTo(new BigDecimal("23.0"));
        assertThat(dataPoint.hasBMI()).isTrue();
        assertThat(dataPoint.getFormattedBMI()).isEqualTo("23.0");
    }

    @Test
    void createTimelineDataPoints_WithDifferentWeightAndHeight_CalculatesCorrectBMI() {
        // Given - Medical event with weight (85kg) and height (180cm)
        // Expected BMI: 85 / (1.80 * 1.80) = 26.2
        MedicalEvent event = createMedicalEvent(
                "Overweight Event",
                new BigDecimal("85.0"),  // weight in kg
                new BigDecimal("180.0")  // height in cm
        );

        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList(event));
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList());

        // When
        List<TimelineDataPoint> dataPoints = timelineService.createTimelineDataPoints(
                patientId, startDate, endDate);

        // Then
        assertThat(dataPoints).hasSize(1);
        TimelineDataPoint dataPoint = dataPoints.get(0);

        assertThat(dataPoint.bmi()).isNotNull();
        assertThat(dataPoint.bmi()).isEqualByComparingTo(new BigDecimal("26.2"));
    }

    @Test
    void createTimelineDataPoints_WithNullWeight_ReturnsBMINull() {
        // Given - Medical event with null weight
        MedicalEvent event = createMedicalEvent(
                "Event Without Weight",
                null,  // null weight
                new BigDecimal("175.0")  // height in cm
        );

        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList(event));
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList());

        // When
        List<TimelineDataPoint> dataPoints = timelineService.createTimelineDataPoints(
                patientId, startDate, endDate);

        // Then
        assertThat(dataPoints).hasSize(1);
        TimelineDataPoint dataPoint = dataPoints.get(0);

        assertThat(dataPoint.bmi()).isNull();
        assertThat(dataPoint.hasBMI()).isFalse();
        assertThat(dataPoint.getFormattedBMI()).isEmpty();
    }

    @Test
    void createTimelineDataPoints_WithNullHeight_ReturnsBMINull() {
        // Given - Medical event with null height
        MedicalEvent event = createMedicalEvent(
                "Event Without Height",
                new BigDecimal("70.5"),  // weight in kg
                null  // null height
        );

        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList(event));
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList());

        // When
        List<TimelineDataPoint> dataPoints = timelineService.createTimelineDataPoints(
                patientId, startDate, endDate);

        // Then
        assertThat(dataPoints).hasSize(1);
        TimelineDataPoint dataPoint = dataPoints.get(0);

        assertThat(dataPoint.bmi()).isNull();
        assertThat(dataPoint.hasBMI()).isFalse();
    }

    @Test
    void createTimelineDataPoints_WithZeroWeight_ReturnsBMINull() {
        // Given - Medical event with zero weight (invalid)
        MedicalEvent event = createMedicalEvent(
                "Event With Zero Weight",
                BigDecimal.ZERO,  // invalid weight
                new BigDecimal("175.0")  // height in cm
        );

        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList(event));
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList());

        // When
        List<TimelineDataPoint> dataPoints = timelineService.createTimelineDataPoints(
                patientId, startDate, endDate);

        // Then
        assertThat(dataPoints).hasSize(1);
        TimelineDataPoint dataPoint = dataPoints.get(0);

        assertThat(dataPoint.bmi()).isNull();
    }

    @Test
    void createTimelineDataPoints_WithNegativeWeight_ReturnsBMINull() {
        // Given - Medical event with negative weight (invalid)
        MedicalEvent event = createMedicalEvent(
                "Event With Negative Weight",
                new BigDecimal("-70.5"),  // invalid negative weight
                new BigDecimal("175.0")  // height in cm
        );

        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList(event));
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList());

        // When
        List<TimelineDataPoint> dataPoints = timelineService.createTimelineDataPoints(
                patientId, startDate, endDate);

        // Then
        assertThat(dataPoints).hasSize(1);
        TimelineDataPoint dataPoint = dataPoints.get(0);

        assertThat(dataPoint.bmi()).isNull();
    }

    @Test
    void createTimelineDataPoints_WithHeightTooLow_ReturnsBMINull() {
        // Given - Medical event with height below minimum (< 30cm)
        MedicalEvent event = createMedicalEvent(
                "Event With Invalid Low Height",
                new BigDecimal("70.5"),  // weight in kg
                new BigDecimal("25.0")  // invalid height < 30cm
        );

        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList(event));
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList());

        // When
        List<TimelineDataPoint> dataPoints = timelineService.createTimelineDataPoints(
                patientId, startDate, endDate);

        // Then
        assertThat(dataPoints).hasSize(1);
        TimelineDataPoint dataPoint = dataPoints.get(0);

        assertThat(dataPoint.bmi()).isNull();
    }

    @Test
    void createTimelineDataPoints_WithHeightTooHigh_ReturnsBMINull() {
        // Given - Medical event with height above maximum (> 300cm)
        MedicalEvent event = createMedicalEvent(
                "Event With Invalid High Height",
                new BigDecimal("70.5"),  // weight in kg
                new BigDecimal("350.0")  // invalid height > 300cm
        );

        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList(event));
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList());

        // When
        List<TimelineDataPoint> dataPoints = timelineService.createTimelineDataPoints(
                patientId, startDate, endDate);

        // Then
        assertThat(dataPoints).hasSize(1);
        TimelineDataPoint dataPoint = dataPoints.get(0);

        assertThat(dataPoint.bmi()).isNull();
    }

    @Test
    void createTimelineDataPoints_WithMinimumValidHeight_CalculatesBMI() {
        // Given - Medical event with minimum valid height (30cm)
        MedicalEvent event = createMedicalEvent(
                "Event With Minimum Valid Height",
                new BigDecimal("5.0"),  // weight in kg
                new BigDecimal("30.0")  // minimum valid height
        );

        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList(event));
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList());

        // When
        List<TimelineDataPoint> dataPoints = timelineService.createTimelineDataPoints(
                patientId, startDate, endDate);

        // Then
        assertThat(dataPoints).hasSize(1);
        TimelineDataPoint dataPoint = dataPoints.get(0);

        // BMI should be calculated: 5 / (0.3 * 0.3) = 55.6
        assertThat(dataPoint.bmi()).isNotNull();
        assertThat(dataPoint.bmi()).isEqualByComparingTo(new BigDecimal("55.6"));
    }

    @Test
    void createTimelineDataPoints_WithMaximumValidHeight_CalculatesBMI() {
        // Given - Medical event with maximum valid height (300cm)
        MedicalEvent event = createMedicalEvent(
                "Event With Maximum Valid Height",
                new BigDecimal("100.0"),  // weight in kg
                new BigDecimal("300.0")  // maximum valid height
        );

        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList(event));
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList());

        // When
        List<TimelineDataPoint> dataPoints = timelineService.createTimelineDataPoints(
                patientId, startDate, endDate);

        // Then
        assertThat(dataPoints).hasSize(1);
        TimelineDataPoint dataPoint = dataPoints.get(0);

        // BMI should be calculated: 100 / (3.0 * 3.0) = 11.1
        assertThat(dataPoint.bmi()).isNotNull();
        assertThat(dataPoint.bmi()).isEqualByComparingTo(new BigDecimal("11.1"));
    }

    // ========== Timeline Data Point Type Tests ==========

    @Test
    void createTimelineDataPoints_EventDataPoint_HasEventTypeAndBMI() {
        // Given - Medical event with valid measurements
        MedicalEvent event = createMedicalEvent(
                "Medical Event",
                new BigDecimal("70.5"),
                new BigDecimal("175.0")
        );

        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList(event));
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList());

        // When
        List<TimelineDataPoint> dataPoints = timelineService.createTimelineDataPoints(
                patientId, startDate, endDate);

        // Then
        assertThat(dataPoints).hasSize(1);
        TimelineDataPoint dataPoint = dataPoints.get(0);

        assertThat(dataPoint.eventType()).isEqualTo("EVENT");
        assertThat(dataPoint.isMedicalEvent()).isTrue();
        assertThat(dataPoint.isDosage()).isFalse();
        assertThat(dataPoint.description()).isEqualTo("Medical Event");
        assertThat(dataPoint.severity()).isEqualTo(MedicalEventSeverity.MODERATE);
        assertThat(dataPoint.bmi()).isNotNull();
    }

    @Test
    void createTimelineDataPoints_DosageDataPoint_HasDosageTypeAndNullBMI() {
        // Given - Medication dosage (no weight/height measurements)
        MedicationDosage dosage = createMedicationDosage(new BigDecimal("500.0"));

        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList());
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList(dosage));

        // When
        List<TimelineDataPoint> dataPoints = timelineService.createTimelineDataPoints(
                patientId, startDate, endDate);

        // Then
        assertThat(dataPoints).hasSize(1);
        TimelineDataPoint dataPoint = dataPoints.get(0);

        assertThat(dataPoint.eventType()).isEqualTo("DOSAGE");
        assertThat(dataPoint.isDosage()).isTrue();
        assertThat(dataPoint.isMedicalEvent()).isFalse();
        assertThat(dataPoint.description()).isEqualTo("Medication Administration");
        assertThat(dataPoint.value()).isEqualByComparingTo(new BigDecimal("500.0"));
        assertThat(dataPoint.unit()).isEqualTo("mg");
        assertThat(dataPoint.bmi()).isNull();  // Dosages don't have BMI
        assertThat(dataPoint.hasBMI()).isFalse();
    }

    @Test
    void createTimelineDataPoints_MixedEventAndDosage_CorrectlyAssignsBMI() {
        // Given - Both medical event and medication dosage
        MedicalEvent event = createMedicalEvent(
                "Headache",
                new BigDecimal("70.5"),
                new BigDecimal("175.0")
        );
        MedicationDosage dosage = createMedicationDosage(new BigDecimal("500.0"));

        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList(event));
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList(dosage));

        // When
        List<TimelineDataPoint> dataPoints = timelineService.createTimelineDataPoints(
                patientId, startDate, endDate);

        // Then
        assertThat(dataPoints).hasSize(2);

        // Event should have BMI
        TimelineDataPoint eventPoint = dataPoints.stream()
                .filter(dp -> "EVENT".equals(dp.eventType()))
                .findFirst()
                .orElseThrow();
        assertThat(eventPoint.bmi()).isNotNull();
        assertThat(eventPoint.bmi()).isEqualByComparingTo(new BigDecimal("23.0"));

        // Dosage should not have BMI
        TimelineDataPoint dosagePoint = dataPoints.stream()
                .filter(dp -> "DOSAGE".equals(dp.eventType()))
                .findFirst()
                .orElseThrow();
        assertThat(dosagePoint.bmi()).isNull();
    }

    @Test
    void createTimelineDataPoints_MultipleEventsWithDifferentBMI_CalculatesEachCorrectly() {
        // Given - Multiple events with different weights (simulating weight change over time)
        MedicalEvent event1 = createMedicalEvent(
                "Event 1 - Before Weight Loss",
                new BigDecimal("85.0"),  // 85kg
                new BigDecimal("175.0")   // 175cm
        );
        event1.setEventTime(LocalDateTime.now().minusDays(5));

        MedicalEvent event2 = createMedicalEvent(
                "Event 2 - After Weight Loss",
                new BigDecimal("75.0"),  // 75kg (10kg loss)
                new BigDecimal("175.0")   // 175cm (same height)
        );
        event2.setEventTime(LocalDateTime.now().minusDays(1));

        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList(event1, event2));
        when(medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(
                eq(patientId), any(), any())).thenReturn(Arrays.asList());

        // When
        List<TimelineDataPoint> dataPoints = timelineService.createTimelineDataPoints(
                patientId, startDate, endDate);

        // Then
        assertThat(dataPoints).hasSize(2);

        // Event 1 BMI: 85 / (1.75 * 1.75) = 27.8
        TimelineDataPoint dataPoint1 = dataPoints.get(0);
        assertThat(dataPoint1.bmi()).isNotNull();
        assertThat(dataPoint1.bmi()).isEqualByComparingTo(new BigDecimal("27.8"));

        // Event 2 BMI: 75 / (1.75 * 1.75) = 24.5
        TimelineDataPoint dataPoint2 = dataPoints.get(1);
        assertThat(dataPoint2.bmi()).isNotNull();
        assertThat(dataPoint2.bmi()).isEqualByComparingTo(new BigDecimal("24.5"));

        // Verify BMI changed correctly (reduced by ~3.3)
        BigDecimal bmiDifference = dataPoint1.bmi().subtract(dataPoint2.bmi());
        assertThat(bmiDifference).isEqualByComparingTo(new BigDecimal("3.3"));
    }

    // ========== Helper Methods ==========

    /**
     * Helper method to create a test MedicalEvent with all required fields.
     */
    private MedicalEvent createMedicalEvent(String title, BigDecimal weightKg, BigDecimal heightCm) {
        MedicalEvent event = new MedicalEvent();
        event.setId(UUID.randomUUID());
        event.setPatientId(patientId);
        event.setMedicationId(medicationId);
        event.setEventTime(LocalDateTime.now().minusHours(2));
        event.setTitle(title);
        event.setDescription("Test description for " + title);
        event.setSeverity(MedicalEventSeverity.MODERATE);
        event.setCategory(MedicalEventCategory.SYMPTOM);
        event.setWeightKg(weightKg);
        event.setHeightCm(heightCm);
        event.setDosageGiven(new BigDecimal("5.00"));
        return event;
    }

    /**
     * Helper method to create a test MedicationDosage.
     */
    private MedicationDosage createMedicationDosage(BigDecimal dosageAmount) {
        MedicationDosage dosage = new MedicationDosage();
        dosage.setId(UUID.randomUUID());
        dosage.setPatientId(patientId);
        dosage.setMedicationId(medicationId);
        dosage.setAdministrationTime(LocalDateTime.now().minusHours(1));
        dosage.setDosageAmount(dosageAmount);
        dosage.setDosageUnit("mg");
        dosage.setAdministered(true);
        return dosage;
    }
}
