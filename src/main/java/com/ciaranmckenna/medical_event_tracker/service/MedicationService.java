package com.ciaranmckenna.medical_event_tracker.service;

import com.ciaranmckenna.medical_event_tracker.dto.CreateMedicationRequest;
import com.ciaranmckenna.medical_event_tracker.dto.MedicationResponse;
import com.ciaranmckenna.medical_event_tracker.dto.UpdateMedicationRequest;
import com.ciaranmckenna.medical_event_tracker.entity.Medication;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing medications
 */
public interface MedicationService {

    /**
     * Create a new medication
     */
    MedicationResponse createMedication(CreateMedicationRequest request);

    /**
     * Get all active medications
     */
    List<MedicationResponse> getAllActiveMedications();

    /**
     * Get medication by ID
     */
    MedicationResponse getMedicationById(UUID id);

    /**
     * Update medication
     */
    MedicationResponse updateMedication(UUID id, UpdateMedicationRequest request);

    /**
     * Soft delete medication
     */
    void deleteMedication(UUID id);

    /**
     * Search medications by name or generic name
     */
    List<MedicationResponse> searchMedications(String searchTerm);

    /**
     * Get medications by type
     */
    List<MedicationResponse> getMedicationsByType(Medication.MedicationType type);

    /**
     * Get most commonly used medications
     */
    List<MedicationResponse> getMostCommonlyUsedMedications();

    /**
     * Check if medication exists by name
     */
    boolean medicationExistsByName(String name);
}