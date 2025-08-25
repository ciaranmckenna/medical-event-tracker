package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.dto.DashboardSummary;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import com.ciaranmckenna.medical_event_tracker.repository.MedicalEventRepository;
import com.ciaranmckenna.medical_event_tracker.repository.MedicationDosageRepository;
import com.ciaranmckenna.medical_event_tracker.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of DashboardService for dashboard analytics and summaries.
 * Focused on dashboard metrics and summary statistics.
 */
@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final MedicalEventRepository medicalEventRepository;
    private final MedicationDosageRepository medicationDosageRepository;

    public DashboardServiceImpl(MedicalEventRepository medicalEventRepository,
                               MedicationDosageRepository medicationDosageRepository) {
        this.medicalEventRepository = medicalEventRepository;
        this.medicationDosageRepository = medicationDosageRepository;
    }

    @Override
    public DashboardSummary generateDashboardSummary(UUID patientId) {
        validatePatientId(patientId);

        // Calculate key metrics
        long[] keyMetrics = calculateKeyMetrics(patientId);
        long totalEvents = keyMetrics[0];
        long totalDosages = keyMetrics[1];
        long recentEvents = keyMetrics[2];
        
        // Get event breakdowns
        Map<MedicalEventCategory, Long> eventsByCategory = getEventsByCategory(patientId);
        Map<MedicalEventSeverity, Long> eventsBySeverity = getEventsBySeverity(patientId);

        return new DashboardSummary(
                patientId,
                totalEvents,
                totalDosages,
                eventsByCategory,
                eventsBySeverity,
                recentEvents,
                LocalDateTime.now()
        );
    }

    @Override
    public Map<String, DashboardSummary> generateWeeklySummaries(UUID patientId) {
        validatePatientId(patientId);

        Map<String, DashboardSummary> weeklySummaries = new LinkedHashMap<>();
        LocalDateTime endDate = LocalDateTime.now();
        
        // Generate summaries for the last 8 weeks
        for (int week = 0; week < 8; week++) {
            LocalDateTime weekStart = endDate.minusWeeks(week + 1);
            LocalDateTime weekEnd = endDate.minusWeeks(week);
            
            DashboardSummary weeklySummary = generateWeeklySummary(patientId, weekStart, weekEnd);
            String weekKey = "Week " + (week + 1);
            weeklySummaries.put(weekKey, weeklySummary);
        }
        
        return weeklySummaries;
    }

    @Override
    public Map<MedicalEventCategory, Long> getEventsByCategory(UUID patientId) {
        List<Object[]> results = medicalEventRepository.countByPatientIdGroupByCategory(patientId);
        Map<MedicalEventCategory, Long> categoryMap = new HashMap<>();
        
        for (Object[] result : results) {
            MedicalEventCategory category = (MedicalEventCategory) result[0];
            Long count = (Long) result[1];
            categoryMap.put(category, count);
        }
        
        return categoryMap;
    }

    @Override
    public Map<MedicalEventSeverity, Long> getEventsBySeverity(UUID patientId) {
        List<Object[]> results = medicalEventRepository.countByPatientIdGroupBySeverity(patientId);
        Map<MedicalEventSeverity, Long> severityMap = new HashMap<>();
        
        for (Object[] result : results) {
            MedicalEventSeverity severity = (MedicalEventSeverity) result[0];
            Long count = (Long) result[1];
            severityMap.put(severity, count);
        }
        
        return severityMap;
    }

    @Override
    public long[] calculateKeyMetrics(UUID patientId) {
        long totalEvents = medicalEventRepository.countByPatientId(patientId);
        long totalDosages = medicationDosageRepository.countByPatientId(patientId);
        long recentEvents = getRecentEventsCount(patientId, 7);
        
        return new long[]{totalEvents, totalDosages, recentEvents};
    }

    @Override
    public long getRecentEventsCount(UUID patientId, int daysBack) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysBack);
        return medicalEventRepository.countByPatientIdAndEventTimeAfter(patientId, cutoffDate);
    }

    // Private helper methods

    private void validatePatientId(UUID patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }
    }

    private DashboardSummary generateWeeklySummary(UUID patientId, LocalDateTime startDate, LocalDateTime endDate) {
        long weeklyEvents = medicalEventRepository.countByPatientIdAndEventTimeBetween(patientId, startDate, endDate);
        long weeklyDosages = medicationDosageRepository.countByPatientIdAndAdministrationTimeBetween(patientId, startDate, endDate);
        
        // For weekly summaries, we'll use simplified category and severity breakdowns
        Map<MedicalEventCategory, Long> eventsByCategory = new HashMap<>();
        Map<MedicalEventSeverity, Long> eventsBySeverity = new HashMap<>();
        
        return new DashboardSummary(
                patientId,
                weeklyEvents,
                weeklyDosages,
                eventsByCategory,
                eventsBySeverity,
                weeklyEvents, // For weekly view, all events in period are "recent"
                LocalDateTime.now()
        );
    }
}