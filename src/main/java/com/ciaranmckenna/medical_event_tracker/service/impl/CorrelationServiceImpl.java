package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.dto.MedicationCorrelationAnalysis;
import com.ciaranmckenna.medical_event_tracker.dto.MedicationImpactAnalysis;
import com.ciaranmckenna.medical_event_tracker.entity.*;
import com.ciaranmckenna.medical_event_tracker.repository.MedicalEventRepository;
import com.ciaranmckenna.medical_event_tracker.repository.MedicationDosageRepository;
import com.ciaranmckenna.medical_event_tracker.service.CorrelationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of CorrelationService for medication correlation analysis.
 * Focused solely on correlation calculations and medication impact analysis.
 */
@Service
@Transactional(readOnly = true)
public class CorrelationServiceImpl implements CorrelationService {

    private final MedicalEventRepository medicalEventRepository;
    private final MedicationDosageRepository medicationDosageRepository;

    public CorrelationServiceImpl(MedicalEventRepository medicalEventRepository,
                                 MedicationDosageRepository medicationDosageRepository) {
        this.medicalEventRepository = medicalEventRepository;
        this.medicationDosageRepository = medicationDosageRepository;
    }

    @Override
    public MedicationCorrelationAnalysis generateMedicationCorrelationAnalysis(UUID patientId, UUID medicationId) {
        validatePatientAndMedicationIds(patientId, medicationId);

        // Get all dosages for this medication
        List<MedicationDosage> dosages = medicationDosageRepository.findByPatientIdAndMedicationId(patientId, medicationId);
        
        if (dosages.isEmpty()) {
            return createEmptyCorrelationAnalysis(patientId, medicationId);
        }

        // Get events that occurred within 24 hours after dosages
        List<MedicalEvent> eventsAfterDosages = findEventsAfterDosages(patientId, medicationId, dosages);
        
        // Calculate correlation metrics
        double correlationPercentage = calculateCorrelationPercentage(dosages.size(), eventsAfterDosages.size());
        double correlationStrength = calculateCorrelationStrength(correlationPercentage);
        
        // Group events by category and severity
        Map<MedicalEventCategory, Long> eventsByCategory = groupEventsByCategory(eventsAfterDosages);
        Map<MedicalEventSeverity, Long> eventsBySeverity = groupEventsBySeverity(eventsAfterDosages);
        
        // Get medication name (simplified - in real implementation would query medication table)
        String medicationName = "Medication " + medicationId.toString().substring(0, 8);

        return new MedicationCorrelationAnalysis(
                medicationId,
                patientId,
                medicationName,
                (long) dosages.size(),
                (long) eventsAfterDosages.size(),
                correlationPercentage,
                correlationStrength,
                eventsByCategory,
                eventsBySeverity,
                LocalDateTime.now()
        );
    }

    @Override
    public List<MedicationCorrelationAnalysis> generateAllMedicationCorrelations(UUID patientId) {
        validatePatientId(patientId);

        // Get all unique medications for the patient
        List<UUID> medicationIds = medicationDosageRepository.findDistinctMedicationIdsByPatientId(patientId);
        
        return medicationIds.stream()
                .map(medicationId -> generateMedicationCorrelationAnalysis(patientId, medicationId))
                .collect(Collectors.toList());
    }

    @Override
    public MedicationImpactAnalysis generateMedicationImpactAnalysis(
            UUID patientId, UUID medicationId, LocalDateTime startDate, LocalDateTime endDate) {
        
        validatePatientAndMedicationIds(patientId, medicationId);
        validateDateRange(startDate, endDate);

        // Get dosages within the date range
        List<MedicationDosage> dosages = medicationDosageRepository
                .findByPatientIdAndMedicationIdAndAdministrationTimeBetween(
                        patientId, medicationId, startDate, endDate);

        // Get events within the date range
        List<MedicalEvent> events = medicalEventRepository
                .findByPatientIdAndEventTimeBetween(patientId, startDate, endDate);

        // Calculate impact metrics
        double averageEventsPerDay = calculateAverageEventsPerDay(events, startDate, endDate);
        double medicationEffectiveness = calculateMedicationEffectiveness(dosages, events);
        Map<MedicalEventSeverity, Long> severityImpact = analyzeSeverityImpact(events);

        // Calculate required metrics for MedicationImpactAnalysis
        long symptomEventsCount = events.stream()
                .mapToLong(event -> event.getCategory().toString().contains("SYMPTOM") ? 1 : 0)
                .sum();
        
        long adverseReactionEventsCount = events.stream()
                .mapToLong(event -> event.getCategory().toString().contains("ADVERSE") ? 1 : 0)
                .sum();
        
        double symptomReductionPercentage = calculateSymptomReduction(events, startDate, endDate);
        Map<String, java.util.List<Long>> weeklyTrends = calculateWeeklyTrends(events, startDate, endDate);
        
        return new MedicationImpactAnalysis(
                medicationId,
                patientId,
                "Medication Impact Analysis",
                startDate,
                endDate,
                (long) dosages.size(),
                (long) events.size(),
                averageEventsPerDay,
                symptomEventsCount,
                adverseReactionEventsCount,
                symptomReductionPercentage,
                medicationEffectiveness,
                weeklyTrends,
                LocalDateTime.now()
        );
    }

    @Override
    public double calculateCorrelationPercentage(int dosageCount, int eventCount) {
        if (dosageCount == 0) {
            return 0.0;
        }
        return Math.min(100.0, (eventCount * 100.0) / dosageCount);
    }

    @Override
    public double calculateCorrelationStrength(double correlationPercentage) {
        // Convert percentage to strength score (0.0 to 1.0)
        if (correlationPercentage >= 80) return 1.0;
        if (correlationPercentage >= 60) return 0.8;
        if (correlationPercentage >= 40) return 0.6;
        if (correlationPercentage >= 20) return 0.4;
        if (correlationPercentage > 0) return 0.2;
        return 0.0;
    }

    // Private helper methods

    private void validatePatientId(UUID patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
    }

    private void validatePatientAndMedicationIds(UUID patientId, UUID medicationId) {
        validatePatientId(patientId);
        if (medicationId == null) {
            throw new IllegalArgumentException("Medication ID cannot be null");
        }
    }

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
    }

    private MedicationCorrelationAnalysis createEmptyCorrelationAnalysis(UUID patientId, UUID medicationId) {
        return new MedicationCorrelationAnalysis(
                medicationId,
                patientId,
                "Unknown Medication",
                0L,
                0L,
                0.0,
                0.0,
                new HashMap<>(),
                new HashMap<>(),
                LocalDateTime.now()
        );
    }

    private List<MedicalEvent> findEventsAfterDosages(UUID patientId, UUID medicationId, List<MedicationDosage> dosages) {
        List<MedicalEvent> allEvents = new ArrayList<>();
        
        for (MedicationDosage dosage : dosages) {
            LocalDateTime dosageTime = dosage.getAdministrationTime();
            LocalDateTime windowEnd = dosageTime.plusHours(24);
            
            List<MedicalEvent> eventsInWindow = medicalEventRepository
                    .findByPatientIdAndEventTimeBetween(patientId, dosageTime, windowEnd);
            
            allEvents.addAll(eventsInWindow);
        }
        
        return allEvents;
    }

    private Map<MedicalEventCategory, Long> groupEventsByCategory(List<MedicalEvent> events) {
        return events.stream()
                .collect(Collectors.groupingBy(
                        MedicalEvent::getCategory,
                        Collectors.counting()
                ));
    }

    private Map<MedicalEventSeverity, Long> groupEventsBySeverity(List<MedicalEvent> events) {
        return events.stream()
                .collect(Collectors.groupingBy(
                        MedicalEvent::getSeverity,
                        Collectors.counting()
                ));
    }

    private double calculateAverageEventsPerDay(List<MedicalEvent> events, LocalDateTime startDate, LocalDateTime endDate) {
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate());
        if (daysBetween == 0) daysBetween = 1;
        return events.size() / (double) daysBetween;
    }

    private double calculateMedicationEffectiveness(List<MedicationDosage> dosages, List<MedicalEvent> events) {
        if (dosages.isEmpty()) return 0.0;
        
        // Simple effectiveness calculation: lower event count per dosage indicates higher effectiveness
        double eventsPerDosage = events.size() / (double) dosages.size();
        return Math.max(0.0, 1.0 - (eventsPerDosage / 10.0)); // Normalize to 0-1 scale
    }

    private Map<MedicalEventSeverity, Long> analyzeSeverityImpact(List<MedicalEvent> events) {
        return groupEventsBySeverity(events);
    }

    private double calculateSymptomReduction(List<MedicalEvent> events, LocalDateTime startDate, LocalDateTime endDate) {
        // Simple calculation - in real implementation would compare to baseline
        long totalEvents = events.size();
        if (totalEvents == 0) return 0.0;
        
        long symptomEvents = events.stream()
                .mapToLong(event -> event.getCategory().toString().contains("SYMPTOM") ? 1 : 0)
                .sum();
        
        return Math.max(0.0, (1.0 - ((double) symptomEvents / totalEvents)) * 100.0);
    }
    
    private Map<String, java.util.List<Long>> calculateWeeklyTrends(List<MedicalEvent> events, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, java.util.List<Long>> trends = new HashMap<>();
        trends.put("events", java.util.Arrays.asList((long) events.size()));
        trends.put("dosages", java.util.Arrays.asList(0L)); // Simplified
        return trends;
    }
}