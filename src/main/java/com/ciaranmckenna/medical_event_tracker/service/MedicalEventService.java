package com.ciaranmckenna.medical_event_tracker.service;

import com.ciaranmckenna.medical_event_tracker.entity.MedicalEvent;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing medical events.
 * Provides business logic for creating, retrieving, updating, and deleting medical events.
 */
public interface MedicalEventService {

    /**
     * Create a new medical event.
     *
     * @param medicalEvent the medical event to create
     * @return the created medical event with generated ID
     * @throws IllegalArgumentException if the medical event is null
     */
    MedicalEvent createMedicalEvent(MedicalEvent medicalEvent);

    /**
     * Retrieve a medical event by its ID.
     *
     * @param id the UUID of the medical event
     * @return an Optional containing the medical event if found, empty otherwise
     */
    Optional<MedicalEvent> getMedicalEventById(UUID id);

    /**
     * Retrieve all medical events for a specific patient, ordered by event time (most recent first).
     *
     * @param patientId the UUID of the patient
     * @return list of medical events for the patient
     */
    List<MedicalEvent> getMedicalEventsByPatientId(UUID patientId);

    /**
     * Update an existing medical event.
     *
     * @param medicalEvent the medical event with updated information
     * @return the updated medical event
     * @throws RuntimeException if the medical event is not found
     */
    MedicalEvent updateMedicalEvent(MedicalEvent medicalEvent);

    /**
     * Delete a medical event by its ID.
     *
     * @param id the UUID of the medical event to delete
     * @throws RuntimeException if the medical event is not found
     */
    void deleteMedicalEvent(UUID id);

    /**
     * Retrieve medical events for a patient within a specific date range.
     *
     * @param patientId the UUID of the patient
     * @param startTime the start of the date range
     * @param endTime   the end of the date range
     * @return list of medical events within the date range
     */
    List<MedicalEvent> getMedicalEventsByPatientIdAndDateRange(UUID patientId, 
                                                              LocalDateTime startTime, 
                                                              LocalDateTime endTime);

    /**
     * Retrieve medical events for a patient by category.
     *
     * @param patientId the UUID of the patient
     * @param category  the medical event category
     * @return list of medical events in the specified category
     */
    List<MedicalEvent> getMedicalEventsByPatientIdAndCategory(UUID patientId, MedicalEventCategory category);

    /**
     * Retrieve medical events for a patient by severity.
     *
     * @param patientId the UUID of the patient
     * @param severity  the medical event severity
     * @return list of medical events with the specified severity
     */
    List<MedicalEvent> getMedicalEventsByPatientIdAndSeverity(UUID patientId, MedicalEventSeverity severity);

    /**
     * Retrieve medical events for a patient linked to a specific medication.
     *
     * @param patientId    the UUID of the patient
     * @param medicationId the UUID of the medication
     * @return list of medical events linked to the medication
     */
    List<MedicalEvent> getMedicalEventsByPatientIdAndMedicationId(UUID patientId, UUID medicationId);

    /**
     * Search medical events for a patient by text in title or description.
     *
     * @param patientId  the UUID of the patient
     * @param searchText the text to search for
     * @return list of medical events containing the search text
     */
    List<MedicalEvent> searchMedicalEventsByPatientId(UUID patientId, String searchText);

    /**
     * Get recent medical events for a patient (last N days).
     *
     * @param patientId the UUID of the patient
     * @param daysBack  number of days to look back
     * @return list of recent medical events
     */
    List<MedicalEvent> getRecentMedicalEventsByPatientId(UUID patientId, int daysBack);

    /**
     * Count medical events for a patient by category.
     *
     * @param patientId the UUID of the patient
     * @param category  the medical event category
     * @return count of medical events in the specified category
     */
    long countMedicalEventsByPatientIdAndCategory(UUID patientId, MedicalEventCategory category);
}