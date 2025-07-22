package com.ciaranmckenna.medical_event_tracker.repository;

import com.ciaranmckenna.medical_event_tracker.entity.Patient;
import com.ciaranmckenna.medical_event_tracker.entity.PatientMedication;
import com.ciaranmckenna.medical_event_tracker.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientMedicationRepository extends JpaRepository<PatientMedication, UUID> {

    /**
     * Find all active medications for a patient
     */
    List<PatientMedication> findByPatientAndActiveTrue(Patient patient);

    /**
     * Find all medications (active and inactive) for a patient
     */
    List<PatientMedication> findByPatient(Patient patient);

    /**
     * Find active patient-medication relationship by ID
     */
    Optional<PatientMedication> findByIdAndActiveTrue(UUID id);

    /**
     * Find active patient-medication by patient and medication
     */
    Optional<PatientMedication> findByPatientAndMedicationAndActiveTrue(Patient patient, Medication medication);

    /**
     * Find currently active medications for a patient (considering start/end dates)
     */
    @Query("SELECT pm FROM PatientMedication pm WHERE pm.patient = :patient AND pm.active = true AND " +
           "pm.startDate <= :now AND (pm.endDate IS NULL OR pm.endDate > :now)")
    List<PatientMedication> findCurrentlyActiveMedications(@Param("patient") Patient patient, 
                                                          @Param("now") LocalDateTime now);

    /**
     * Find medications scheduled for a specific time range
     */
    @Query("SELECT pm FROM PatientMedication pm WHERE pm.patient = :patient AND pm.active = true AND " +
           "pm.startDate <= :endTime AND (pm.endDate IS NULL OR pm.endDate > :startTime)")
    List<PatientMedication> findMedicationsInTimeRange(@Param("patient") Patient patient,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * Find medications with morning schedule
     */
    @Query("SELECT pm FROM PatientMedication pm WHERE pm.patient = :patient AND pm.active = true AND " +
           "pm.morningTime IS NOT NULL")
    List<PatientMedication> findMedicationsWithMorningSchedule(@Param("patient") Patient patient);

    /**
     * Find medications with evening schedule
     */
    @Query("SELECT pm FROM PatientMedication pm WHERE pm.patient = :patient AND pm.active = true AND " +
           "pm.eveningTime IS NOT NULL")
    List<PatientMedication> findMedicationsWithEveningSchedule(@Param("patient") Patient patient);

    /**
     * Count active medications for a patient
     */
    long countByPatientAndActiveTrue(Patient patient);

    /**
     * Find all patients taking a specific medication
     */
    List<PatientMedication> findByMedicationAndActiveTrue(Medication medication);

    /**
     * Check if patient is already taking a specific medication
     */
    boolean existsByPatientAndMedicationAndActiveTrue(Patient patient, Medication medication);

    /**
     * Find medications starting in a date range
     */
    List<PatientMedication> findByPatientAndStartDateBetweenAndActiveTrue(
        Patient patient, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find medications ending in a date range
     */
    List<PatientMedication> findByPatientAndEndDateBetweenAndActiveTrue(
        Patient patient, LocalDateTime startDate, LocalDateTime endDate);
}