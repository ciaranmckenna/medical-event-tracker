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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        for (MedicalEvent event : events) {
            TimelineDataPoint dataPoint = new TimelineDataPoint(
                    event.getEventTime(),
                    "EVENT",
                    event.getTitle(),
                    null, // BigDecimal value
                    null, // String unit
                    event.getSeverity()
            );
            dataPoints.add(dataPoint);
        }

        // Get medication dosages in the date range
        List<MedicationDosage> dosages = medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(patientId, startDate, endDate);
        
        // Convert dosages to timeline data points
        for (MedicationDosage dosage : dosages) {
            TimelineDataPoint dataPoint = new TimelineDataPoint(
                    dosage.getAdministrationTime(),
                    "DOSAGE",
                    "Medication Administration",
                    dosage.getDosageAmount(),
                    "mg",
                    null // MedicalEventSeverity
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

    private TimelineDataPoint createEventDataPoint(MedicalEvent event) {
        return new TimelineDataPoint(
                event.getEventTime(),
                "EVENT",
                event.getTitle(),
                null, // BigDecimal value
                null, // String unit
                event.getSeverity()
        );
    }

    private TimelineDataPoint createDosageDataPoint(MedicationDosage dosage) {
        return new TimelineDataPoint(
                dosage.getAdministrationTime(),
                "DOSAGE",
                "Medication Administration",
                dosage.getDosageAmount(),
                "mg",
                null // MedicalEventSeverity
        );
    }
}