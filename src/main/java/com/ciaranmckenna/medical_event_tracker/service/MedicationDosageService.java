package com.ciaranmckenna.medical_event_tracker.service;

import com.ciaranmckenna.medical_event_tracker.entity.DosageSchedule;
import com.ciaranmckenna.medical_event_tracker.entity.MedicationDosage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for managing medication dosages.
 * Provides business logic for creating, retrieving, updating, and tracking medication dosages.
 */
public interface MedicationDosageService {

    /**
     * Create a new medication dosage record.
     *
     * @param medicationDosage the medication dosage to create
     * @return the created medication dosage with generated ID
     * @throws IllegalArgumentException if the medication dosage is null
     */
    MedicationDosage createMedicationDosage(MedicationDosage medicationDosage);

    /**
     * Retrieve a medication dosage by its ID.
     *
     * @param id the UUID of the medication dosage
     * @return an Optional containing the medication dosage if found, empty otherwise
     */
    Optional<MedicationDosage> getMedicationDosageById(UUID id);

    /**
     * Retrieve all medication dosages for a specific patient, ordered by administration time (most recent first).
     *
     * @param patientId the UUID of the patient
     * @return list of medication dosages for the patient
     */
    List<MedicationDosage> getMedicationDosagesByPatientId(UUID patientId);

    /**
     * Update an existing medication dosage.
     *
     * @param medicationDosage the medication dosage with updated information
     * @return the updated medication dosage
     * @throws RuntimeException if the medication dosage is not found
     */
    MedicationDosage updateMedicationDosage(MedicationDosage medicationDosage);

    /**
     * Delete a medication dosage by its ID.
     *
     * @param id the UUID of the medication dosage to delete
     * @throws RuntimeException if the medication dosage is not found
     */
    void deleteMedicationDosage(UUID id);

    /**
     * Mark a medication dosage as administered.
     *
     * @param id the UUID of the medication dosage
     * @return the updated medication dosage
     * @throws RuntimeException if the medication dosage is not found
     */
    MedicationDosage markDosageAsAdministered(UUID id);

    /**
     * Retrieve medication dosages for a specific patient and medication.
     *
     * @param patientId    the UUID of the patient
     * @param medicationId the UUID of the medication
     * @return list of medication dosages for the patient and medication
     */
    List<MedicationDosage> getMedicationDosagesByPatientIdAndMedicationId(UUID patientId, UUID medicationId);

    /**
     * Retrieve medication dosages for a patient by schedule.
     *
     * @param patientId the UUID of the patient
     * @param schedule  the dosage schedule
     * @return list of medication dosages for the specified schedule
     */
    List<MedicationDosage> getMedicationDosagesByPatientIdAndSchedule(UUID patientId, DosageSchedule schedule);

    /**
     * Retrieve medication dosages for a patient by administration status.
     *
     * @param patientId     the UUID of the patient
     * @param administered  whether the dosage was administered
     * @return list of medication dosages with the specified administration status
     */
    List<MedicationDosage> getMedicationDosagesByPatientIdAndAdministered(UUID patientId, boolean administered);

    /**
     * Retrieve medication dosages for a patient within a specific time range.
     *
     * @param patientId the UUID of the patient
     * @param startTime the start of the time range
     * @param endTime   the end of the time range
     * @return list of medication dosages within the time range
     */
    List<MedicationDosage> getMedicationDosagesByPatientIdAndDateRange(UUID patientId, 
                                                                      LocalDateTime startTime, 
                                                                      LocalDateTime endTime);

    /**
     * Get missed dosages for a patient (not administered and past due).
     *
     * @param patientId   the UUID of the patient
     * @param cutoffTime  the time cutoff for considering a dosage missed
     * @return list of missed medication dosages
     */
    List<MedicationDosage> getMissedDosages(UUID patientId, LocalDateTime cutoffTime);

    /**
     * Get upcoming dosages for a patient within a time window.
     *
     * @param patientId     the UUID of the patient
     * @param currentTime   the current time
     * @param futureTime    the future time limit
     * @return list of upcoming medication dosages
     */
    List<MedicationDosage> getUpcomingDosages(UUID patientId, LocalDateTime currentTime, LocalDateTime futureTime);

    /**
     * Calculate average dosage amount for a patient and medication.
     *
     * @param patientId    the UUID of the patient
     * @param medicationId the UUID of the medication
     * @return average dosage amount, or null if no administered dosages found
     */
    BigDecimal getAverageDosageAmount(UUID patientId, UUID medicationId);

    /**
     * Get recent dosages for a patient (last N days).
     *
     * @param patientId the UUID of the patient
     * @param daysBack  number of days to look back
     * @return list of recent medication dosages
     */
    List<MedicationDosage> getRecentDosages(UUID patientId, int daysBack);

    /**
     * Count medication dosages for a patient by schedule.
     *
     * @param patientId the UUID of the patient
     * @param schedule  the dosage schedule
     * @return count of medication dosages for the specified schedule
     */
    long countDosagesByPatientIdAndSchedule(UUID patientId, DosageSchedule schedule);
}