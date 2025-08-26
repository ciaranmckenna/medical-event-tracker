package com.ciaranmckenna.medical_event_tracker.service;

import com.ciaranmckenna.medical_event_tracker.dto.MedicationCorrelationAnalysis;
import com.ciaranmckenna.medical_event_tracker.dto.MedicationImpactAnalysis;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for medication correlation analysis.
 * Focused on correlation calculations between medications and medical events.
 */
public interface CorrelationService {

    /**
     * Generate correlation analysis between a specific medication and medical events.
     * 
     * @param patientId the patient's UUID
     * @param medicationId the medication's UUID
     * @return correlation analysis for the specific medication
     */
    MedicationCorrelationAnalysis generateMedicationCorrelationAnalysis(UUID patientId, UUID medicationId);

    /**
     * Generate correlation analysis for all medications for a patient.
     * 
     * @param patientId the patient's UUID
     * @return list of correlation analyses for all medications
     */
    List<MedicationCorrelationAnalysis> generateAllMedicationCorrelations(UUID patientId);

    /**
     * Generate medication impact analysis for a specific medication and time period.
     * 
     * @param patientId the patient's UUID
     * @param medicationId the medication's UUID
     * @param startDate start date for analysis
     * @param endDate end date for analysis
     * @return medication impact analysis
     */
    MedicationImpactAnalysis generateMedicationImpactAnalysis(
            UUID patientId, 
            UUID medicationId, 
            LocalDateTime startDate, 
            LocalDateTime endDate
    );

    /**
     * Calculate correlation percentage between dosages and events.
     * 
     * @param dosageCount number of dosages
     * @param eventCount number of related events
     * @return correlation percentage
     */
    double calculateCorrelationPercentage(int dosageCount, int eventCount);

    /**
     * Calculate correlation strength based on percentage.
     * 
     * @param correlationPercentage the correlation percentage
     * @return correlation strength (0.0 to 1.0)
     */
    double calculateCorrelationStrength(double correlationPercentage);
}