package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.dto.TimelineAnalysis;
import com.ciaranmckenna.medical_event_tracker.dto.TimelineDataPoint;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEvent;
import com.ciaranmckenna.medical_event_tracker.entity.MedicationDosage;
import com.ciaranmckenna.medical_event_tracker.repository.MedicalEventRepository;
import com.ciaranmckenna.medical_event_tracker.repository.MedicationDosageRepository;
import com.ciaranmckenna.medical_event_tracker.service.TimelineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementation of TimelineService for timeline analysis.
 * Focused on chronological data analysis and timeline generation.
 */
@Service
@Transactional(readOnly = true)
public class TimelineServiceImpl implements TimelineService {

    private final MedicalEventRepository medicalEventRepository;
    private final MedicationDosageRepository medicationDosageRepository;

    public TimelineServiceImpl(MedicalEventRepository medicalEventRepository,
                              MedicationDosageRepository medicationDosageRepository) {
        this.medicalEventRepository = medicalEventRepository;
        this.medicationDosageRepository = medicationDosageRepository;
    }

    @Override
    public TimelineAnalysis generateTimelineAnalysis(UUID patientId, LocalDateTime startDate, LocalDateTime endDate) {
        validateInputs(patientId, startDate, endDate);

        List<TimelineDataPoint> dataPoints = createTimelineDataPoints(patientId, startDate, endDate);
        Map<String, Object> statistics = calculateTimelineStatistics(dataPoints);
        List<String> patterns = identifyTimelinePatterns(dataPoints);

        return new TimelineAnalysis(
                patientId,
                startDate,
                endDate,
                dataPoints,
                LocalDateTime.now()
        );
    }

    @Override
    public List<TimelineDataPoint> createTimelineDataPoints(UUID patientId, LocalDateTime startDate, LocalDateTime endDate) {
        List<TimelineDataPoint> dataPoints = new ArrayList<>();

        // Get medical events in the date range
        List<MedicalEvent> events = medicalEventRepository.findByPatientIdAndEventTimeBetween(patientId, startDate, endDate);
        
        // Convert events to timeline data points
        // Each event includes BMI calculation based on weight and height at time of event
        for (MedicalEvent event : events) {
            // Calculate BMI from event's weight and height data
            BigDecimal bmi = calculateBMI(event.getWeightKg(), event.getHeightCm());

            TimelineDataPoint dataPoint = new TimelineDataPoint(
                    event.getEventTime(),
                    "EVENT",
                    event.getTitle(),
                    null, // BigDecimal value - not used for events
                    null, // String unit - not used for events
                    event.getSeverity(),
                    bmi   // BMI calculated from event's weight and height
            );
            dataPoints.add(dataPoint);
        }

        // Get medication dosages in the date range
        List<MedicationDosage> dosages = medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(patientId, startDate, endDate);
        
        // Convert dosages to timeline data points
        // Dosages don't include BMI as they don't have weight/height measurements
        for (MedicationDosage dosage : dosages) {
            TimelineDataPoint dataPoint = new TimelineDataPoint(
                    dosage.getAdministrationTime(),
                    "DOSAGE",
                    "Medication Administration",
                    dosage.getDosageAmount(),
                    "mg",
                    null, // MedicalEventSeverity - not applicable for dosages
                    null  // BMI - not applicable for dosages
            );
            dataPoints.add(dataPoint);
        }

        // Sort by timestamp
        dataPoints.sort(Comparator.comparing(TimelineDataPoint::timestamp));

        return dataPoints;
    }

    @Override
    public TimelineAnalysis generateMedicationTimeline(UUID patientId, UUID medicationId, LocalDateTime startDate, LocalDateTime endDate) {
        validateInputs(patientId, startDate, endDate);
        
        if (medicationId == null) {
            throw new IllegalArgumentException("Medication ID cannot be null");
        }

        // Get only events and dosages related to this medication
        List<MedicalEvent> events = medicalEventRepository.findByPatientIdAndMedicationIdAndEventTimeBetween(
                patientId, medicationId, startDate, endDate);
        
        List<MedicationDosage> dosages = medicationDosageRepository.findByPatientIdAndMedicationIdAndAdministrationTimeBetween(
                patientId, medicationId, startDate, endDate);

        List<TimelineDataPoint> dataPoints = new ArrayList<>();
        
        // Convert to timeline data points (simplified version)
        events.forEach(event -> dataPoints.add(createEventDataPoint(event)));
        dosages.forEach(dosage -> dataPoints.add(createDosageDataPoint(dosage)));

        // Sort by timestamp
        dataPoints.sort(Comparator.comparing(TimelineDataPoint::timestamp));

        Map<String, Object> statistics = calculateTimelineStatistics(dataPoints);
        List<String> patterns = identifyTimelinePatterns(dataPoints);

        return new TimelineAnalysis(
                patientId,
                startDate,
                endDate,
                dataPoints,
                LocalDateTime.now()
        );
    }

    @Override
    public Map<String, Object> calculateTimelineStatistics(List<TimelineDataPoint> dataPoints) {
        Map<String, Object> statistics = new HashMap<>();
        
        statistics.put("totalDataPoints", dataPoints.size());
        statistics.put("medicalEvents", dataPoints.stream()
                .mapToLong(dp -> "EVENT".equals(dp.eventType()) ? 1 : 0)
                .sum());
        statistics.put("medicationDosages", dataPoints.stream()
                .mapToLong(dp -> "DOSAGE".equals(dp.eventType()) ? 1 : 0)
                .sum());
        
        // Calculate time span
        if (!dataPoints.isEmpty()) {
            LocalDateTime firstEvent = dataPoints.get(0).timestamp();
            LocalDateTime lastEvent = dataPoints.get(dataPoints.size() - 1).timestamp();
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(firstEvent, lastEvent);
            statistics.put("timeSpanDays", daysBetween);
        }

        return statistics;
    }

    @Override
    public List<String> identifyTimelinePatterns(List<TimelineDataPoint> dataPoints) {
        List<String> patterns = new ArrayList<>();
        
        if (dataPoints.isEmpty()) {
            return patterns;
        }

        // Simple pattern identification
        long eventCount = dataPoints.stream().mapToLong(dp -> "EVENT".equals(dp.eventType()) ? 1 : 0).sum();
        long dosageCount = dataPoints.stream().mapToLong(dp -> "DOSAGE".equals(dp.eventType()) ? 1 : 0).sum();

        if (eventCount > dosageCount * 1.5) {
            patterns.add("High event frequency relative to medication dosages");
        } else if (dosageCount > eventCount * 2) {
            patterns.add("Consistent medication administration with low event frequency");
        }

        // Check for clustering
        if (dataPoints.size() > 10) {
            patterns.add("Dense activity period with multiple data points");
        }

        return patterns;
    }

    // Private helper methods

    private void validateInputs(UUID patientId, LocalDateTime startDate, LocalDateTime endDate) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    private Map<String, Object> createEventProperties(MedicalEvent event) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("eventId", event.getId());
        properties.put("description", event.getDescription());
        properties.put("category", event.getCategory());
        properties.put("severity", event.getSeverity());
        return properties;
    }

    private Map<String, Object> createDosageProperties(MedicationDosage dosage) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("dosageId", dosage.getId());
        properties.put("medicationId", dosage.getMedicationId());
        properties.put("amount", dosage.getDosageAmount());
        properties.put("schedule", dosage.getSchedule());
        return properties;
    }

    /**
     * Creates a TimelineDataPoint from a MedicalEvent.
     * Calculates BMI from the event's weight and height measurements.
     *
     * @param event The medical event to convert
     * @return TimelineDataPoint with event data and calculated BMI
     */
    private TimelineDataPoint createEventDataPoint(MedicalEvent event) {
        // Calculate BMI from event's weight and height at time of occurrence
        BigDecimal bmi = calculateBMI(event.getWeightKg(), event.getHeightCm());

        return new TimelineDataPoint(
                event.getEventTime(),
                "EVENT",
                event.getTitle(),
                null, // BigDecimal value - not used for events
                null, // String unit - not used for events
                event.getSeverity(),
                bmi   // BMI calculated from event's measurements
        );
    }

    /**
     * Creates a TimelineDataPoint from a MedicationDosage.
     * Dosages don't include BMI as they don't have weight/height measurements.
     *
     * @param dosage The medication dosage to convert
     * @return TimelineDataPoint with dosage data
     */
    private TimelineDataPoint createDosageDataPoint(MedicationDosage dosage) {
        return new TimelineDataPoint(
                dosage.getAdministrationTime(),
                "DOSAGE",
                "Medication Administration",
                dosage.getDosageAmount(),
                "mg",
                null, // MedicalEventSeverity - not applicable for dosages
                null  // BMI - not applicable for dosages
        );
    }

    /**
     * Calculates Body Mass Index (BMI) from weight and height measurements.
     *
     * BMI Formula: BMI = weight(kg) / (height(m))^2
     *
     * This calculation uses the standard medical BMI formula where:
     * - Weight must be in kilograms (kg)
     * - Height is converted from centimeters (cm) to meters (m)
     * - Result is rounded to 1 decimal place for medical accuracy
     *
     * Validation:
     * - Returns null if either weight or height is null (missing data)
     * - Returns null if weight is <= 0 (invalid measurement)
     * - Returns null if height is <= 0 (invalid measurement)
     * - Height must be in a reasonable range (30-300 cm) to avoid calculation errors
     *
     * Medical Context:
     * BMI is calculated at the time of each medical event to track changes over time.
     * This allows monitoring of weight changes in response to medications or medical conditions.
     *
     * @param weightKg The patient's weight in kilograms at time of event
     * @param heightCm The patient's height in centimeters at time of event
     * @return The calculated BMI rounded to 1 decimal place, or null if inputs are invalid
     */
    private BigDecimal calculateBMI(BigDecimal weightKg, BigDecimal heightCm) {
        // Return null for missing data - this is expected when weight/height not recorded
        if (weightKg == null || heightCm == null) {
            return null;
        }

        // Validate weight is positive and reasonable
        if (weightKg.compareTo(BigDecimal.ZERO) <= 0) {
            return null; // Invalid weight measurement
        }

        // Validate height is positive and within reasonable medical range (30cm to 300cm)
        // This prevents calculation errors and ensures data quality
        if (heightCm.compareTo(BigDecimal.ZERO) <= 0 ||
            heightCm.compareTo(new BigDecimal("30")) < 0 ||
            heightCm.compareTo(new BigDecimal("300")) > 0) {
            return null; // Invalid height measurement
        }

        // Convert height from centimeters to meters for BMI formula
        // Divide by 100: e.g., 175cm = 1.75m
        BigDecimal heightInMeters = heightCm.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        // Calculate height squared: height(m) * height(m)
        BigDecimal heightSquared = heightInMeters.multiply(heightInMeters);

        // Calculate BMI: weight(kg) / height²(m)
        // Round to 1 decimal place for standard medical reporting
        BigDecimal bmi = weightKg.divide(heightSquared, 1, RoundingMode.HALF_UP);

        return bmi;
    }
}