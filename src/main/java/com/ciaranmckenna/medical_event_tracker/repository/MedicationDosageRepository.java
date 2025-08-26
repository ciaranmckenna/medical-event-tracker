package com.ciaranmckenna.medical_event_tracker.repository;

import com.ciaranmckenna.medical_event_tracker.entity.DosageSchedule;
import com.ciaranmckenna.medical_event_tracker.entity.MedicationDosage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for MedicationDosage entities.
 * Provides query capabilities for medication dosage tracking and analysis.
 */
@Repository
public interface MedicationDosageRepository extends JpaRepository<MedicationDosage, UUID>, JpaSpecificationExecutor<MedicationDosage> {

    /**
     * Find all medication dosages for a specific patient.
     *
     * @param patientId the patient's UUID
     * @return list of medication dosages for the patient
     */
    List<MedicationDosage> findByPatientId(UUID patientId);

    /**
     * Find medication dosages for a specific patient and medication.
     *
     * @param patientId    the patient's UUID
     * @param medicationId the medication's UUID
     * @return list of medication dosages for the patient and medication
     */
    List<MedicationDosage> findByPatientIdAndMedicationId(UUID patientId, UUID medicationId);

    /**
     * Find medication dosages for a patient by schedule.
     *
     * @param patientId the patient's UUID
     * @param schedule  the dosage schedule
     * @return list of medication dosages for the specified schedule
     */
    List<MedicationDosage> findByPatientIdAndSchedule(UUID patientId, DosageSchedule schedule);

    /**
     * Find medication dosages for a patient by administration status.
     *
     * @param patientId     the patient's UUID
     * @param administered  whether the dosage was administered
     * @return list of medication dosages with the specified administration status
     */
    List<MedicationDosage> findByPatientIdAndAdministered(UUID patientId, boolean administered);

    /**
     * Find medication dosages for a patient within a specific time range.
     *
     * @param patientId the patient's UUID
     * @param startTime the start of the time range
     * @param endTime   the end of the time range
     * @return list of medication dosages within the time range
     */
    List<MedicationDosage> findByPatientIdAndAdministrationTimeBetween(UUID patientId, 
                                                                      LocalDateTime startTime, 
                                                                      LocalDateTime endTime);

    /**
     * Find medication dosages for a patient ordered by administration time (most recent first).
     *
     * @param patientId the patient's UUID
     * @return list of medication dosages ordered by administration time descending
     */
    List<MedicationDosage> findByPatientIdOrderByAdministrationTimeDesc(UUID patientId);

    /**
     * Count medication dosages for a patient by schedule.
     *
     * @param patientId the patient's UUID
     * @param schedule  the dosage schedule
     * @return count of medication dosages for the specified schedule
     */
    long countByPatientIdAndSchedule(UUID patientId, DosageSchedule schedule);

    /**
     * Count administered vs pending dosages for a patient.
     *
     * @param patientId the patient's UUID
     * @return array containing [total_dosages, administered_dosages, pending_dosages]
     */
    @Query("SELECT " +
           "COUNT(md), " +
           "SUM(CASE WHEN md.administered = true THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN md.administered = false THEN 1 ELSE 0 END) " +
           "FROM MedicationDosage md WHERE md.patientId = :patientId")
    Object[] getDosageStatsByPatientId(@Param("patientId") UUID patientId);

    /**
     * Find missed dosages (not administered and past due).
     *
     * @param patientId the patient's UUID
     * @param cutoffTime the time cutoff for considering a dosage missed
     * @return list of missed medication dosages
     */
    @Query("SELECT md FROM MedicationDosage md WHERE md.patientId = :patientId " +
           "AND md.administered = false " +
           "AND md.administrationTime < :cutoffTime " +
           "ORDER BY md.administrationTime DESC")
    List<MedicationDosage> findMissedDosagesByPatientId(@Param("patientId") UUID patientId,
                                                       @Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find upcoming dosages for a patient.
     *
     * @param patientId   the patient's UUID
     * @param currentTime the current time
     * @param futureTime  the future time limit
     * @return list of upcoming medication dosages
     */
    @Query("SELECT md FROM MedicationDosage md WHERE md.patientId = :patientId " +
           "AND md.administered = false " +
           "AND md.administrationTime BETWEEN :currentTime AND :futureTime " +
           "ORDER BY md.administrationTime ASC")
    List<MedicationDosage> findUpcomingDosagesByPatientId(@Param("patientId") UUID patientId,
                                                         @Param("currentTime") LocalDateTime currentTime,
                                                         @Param("futureTime") LocalDateTime futureTime);

    /**
     * Calculate average dosage amount for a patient and medication.
     *
     * @param patientId    the patient's UUID
     * @param medicationId the medication's UUID
     * @return average dosage amount
     */
    @Query("SELECT AVG(md.dosageAmount) FROM MedicationDosage md " +
           "WHERE md.patientId = :patientId AND md.medicationId = :medicationId " +
           "AND md.administered = true")
    BigDecimal getAverageDosageAmount(@Param("patientId") UUID patientId,
                                     @Param("medicationId") UUID medicationId);

    /**
     * Find dosages with multiple filter criteria.
     *
     * @param patientId      the patient's UUID
     * @param medicationId   the medication UUID (optional)
     * @param schedule       the dosage schedule (optional)
     * @param administered   the administration status (optional)
     * @param startTime      the start time (optional)
     * @param endTime        the end time (optional)
     * @return list of medication dosages matching the criteria
     */
    @Query("SELECT md FROM MedicationDosage md WHERE md.patientId = :patientId " +
           "AND (:medicationId IS NULL OR md.medicationId = :medicationId) " +
           "AND (:schedule IS NULL OR md.schedule = :schedule) " +
           "AND (:administered IS NULL OR md.administered = :administered) " +
           "AND (:startTime IS NULL OR md.administrationTime >= :startTime) " +
           "AND (:endTime IS NULL OR md.administrationTime <= :endTime) " +
           "ORDER BY md.administrationTime DESC")
    List<MedicationDosage> findByPatientIdWithFilters(@Param("patientId") UUID patientId,
                                                      @Param("medicationId") UUID medicationId,
                                                      @Param("schedule") DosageSchedule schedule,
                                                      @Param("administered") Boolean administered,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    /**
     * Find recent dosages for adherence tracking.
     *
     * @param patientId   the patient's UUID
     * @param daysBack    number of days to look back
     * @return list of recent medication dosages
     */
    @Query("SELECT md FROM MedicationDosage md WHERE md.patientId = :patientId " +
           "AND md.administrationTime >= :cutoffDate " +
           "ORDER BY md.administrationTime DESC")
    List<MedicationDosage> findRecentDosagesByPatientId(@Param("patientId") UUID patientId,
                                                       @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count all medication dosages for a specific patient.
     *
     * @param patientId the patient's UUID
     * @return count of medication dosages for the patient
     */
    long countByPatientId(UUID patientId);

    /**
     * Find medication dosages for a patient, medication, and time range.
     *
     * @param patientId    the patient's UUID
     * @param medicationId the medication's UUID
     * @param startTime    the start of the time range
     * @param endTime      the end of the time range
     * @return list of medication dosages matching the criteria
     */
    List<MedicationDosage> findByPatientIdAndMedicationIdAndAdministrationTimeBetween(
            UUID patientId, UUID medicationId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find distinct medication IDs for a specific patient.
     *
     * @param patientId the patient's UUID
     * @return list of unique medication IDs for the patient
     */
    @Query("SELECT DISTINCT md.medicationId FROM MedicationDosage md WHERE md.patientId = :patientId")
    List<UUID> findDistinctMedicationIdsByPatientId(@Param("patientId") UUID patientId);

    /**
     * Count medication dosages for a patient within a time range.
     *
     * @param patientId the patient's UUID
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return count of medication dosages within the time range
     */
    long countByPatientIdAndAdministrationTimeBetween(UUID patientId, 
                                                     LocalDateTime startTime, 
                                                     LocalDateTime endTime);
}