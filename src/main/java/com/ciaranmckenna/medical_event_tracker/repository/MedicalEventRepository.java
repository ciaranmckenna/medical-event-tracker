package com.ciaranmckenna.medical_event_tracker.repository;

import com.ciaranmckenna.medical_event_tracker.entity.MedicalEvent;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository interface for MedicalEvent entities.
 * Provides advanced query capabilities for medical event data retrieval and analysis.
 */
@Repository
public interface MedicalEventRepository extends JpaRepository<MedicalEvent, UUID>, JpaSpecificationExecutor<MedicalEvent> {

    /**
     * Find all medical events for a specific patient.
     *
     * @param patientId the patient's UUID
     * @return list of medical events for the patient
     */
    List<MedicalEvent> findByPatientId(UUID patientId);

    /**
     * Count all medical events for a specific patient.
     *
     * @param patientId the patient's UUID
     * @return count of medical events for the patient
     */
    long countByPatientId(UUID patientId);

    /**
     * Find medical events for a patient within a specific time range.
     *
     * @param patientId the patient's UUID
     * @param startTime the start of the time range
     * @param endTime   the end of the time range
     * @return list of medical events within the time range
     */
    List<MedicalEvent> findByPatientIdAndEventTimeBetween(UUID patientId, 
                                                         LocalDateTime startTime, 
                                                         LocalDateTime endTime);

    /**
     * Find medical events for a patient by category.
     *
     * @param patientId the patient's UUID
     * @param category  the event category
     * @return list of medical events in the specified category
     */
    List<MedicalEvent> findByPatientIdAndCategory(UUID patientId, MedicalEventCategory category);

    /**
     * Find medical events for a patient by severity.
     *
     * @param patientId the patient's UUID
     * @param severity  the event severity
     * @return list of medical events with the specified severity
     */
    List<MedicalEvent> findByPatientIdAndSeverity(UUID patientId, MedicalEventSeverity severity);

    /**
     * Find medical events for a patient linked to a specific medication.
     *
     * @param patientId    the patient's UUID
     * @param medicationId the medication's UUID
     * @return list of medical events linked to the medication
     */
    List<MedicalEvent> findByPatientIdAndMedicationId(UUID patientId, UUID medicationId);

    /**
     * Find medical events for a patient ordered by event time (most recent first).
     *
     * @param patientId the patient's UUID
     * @return list of medical events ordered by event time descending
     */
    List<MedicalEvent> findByPatientIdOrderByEventTimeDesc(UUID patientId);

    /**
     * Count medical events for a patient by category.
     *
     * @param patientId the patient's UUID
     * @param category  the event category
     * @return count of medical events in the specified category
     */
    long countByPatientIdAndCategory(UUID patientId, MedicalEventCategory category);

    /**
     * Find medical events for a patient that contain specific text in title or description.
     *
     * @param patientId   the patient's UUID
     * @param searchText  the text to search for
     * @return list of medical events containing the search text
     */
    @Query("SELECT me FROM MedicalEvent me WHERE me.patientId = :patientId " +
           "AND (LOWER(me.title) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "OR LOWER(me.description) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    List<MedicalEvent> findByPatientIdAndTitleOrDescriptionContaining(@Param("patientId") UUID patientId,
                                                                     @Param("searchText") String searchText);

    /**
     * Find recent medical events for a patient (last N days).
     *
     * @param patientId the patient's UUID
     * @param daysBack  number of days to look back
     * @return list of recent medical events
     */
    @Query("SELECT me FROM MedicalEvent me WHERE me.patientId = :patientId " +
           "AND me.eventTime >= :cutoffDate ORDER BY me.eventTime DESC")
    List<MedicalEvent> findRecentEventsByPatientId(@Param("patientId") UUID patientId,
                                                   @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find medical events for a patient with multiple filter criteria.
     *
     * @param patientId    the patient's UUID
     * @param category     the event category (optional)
     * @param severity     the event severity (optional)
     * @param medicationId the medication UUID (optional)
     * @param startTime    the start time (optional)
     * @param endTime      the end time (optional)
     * @return list of medical events matching the criteria
     */
    @Query("SELECT me FROM MedicalEvent me WHERE me.patientId = :patientId " +
           "AND (:category IS NULL OR me.category = :category) " +
           "AND (:severity IS NULL OR me.severity = :severity) " +
           "AND (:medicationId IS NULL OR me.medicationId = :medicationId) " +
           "AND (:startTime IS NULL OR me.eventTime >= :startTime) " +
           "AND (:endTime IS NULL OR me.eventTime <= :endTime) " +
           "ORDER BY me.eventTime DESC")
    List<MedicalEvent> findByPatientIdWithFilters(@Param("patientId") UUID patientId,
                                                  @Param("category") MedicalEventCategory category,
                                                  @Param("severity") MedicalEventSeverity severity,
                                                  @Param("medicationId") UUID medicationId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * Get statistics about medical events for a patient.
     *
     * @param patientId the patient's UUID
     * @return array containing [total_events, symptom_events, medication_events, emergency_events]
     */
    @Query("SELECT " +
           "COUNT(me), " +
           "SUM(CASE WHEN me.category = 'SYMPTOM' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN me.category = 'MEDICATION' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN me.category = 'EMERGENCY' THEN 1 ELSE 0 END) " +
           "FROM MedicalEvent me WHERE me.patientId = :patientId")
    Object[] getEventStatsByPatientId(@Param("patientId") UUID patientId);

    /**
     * Count medical events for a patient that occurred after a specific time.
     *
     * @param patientId the patient's UUID
     * @param afterTime the cutoff time
     * @return count of events after the specified time
     */
    long countByPatientIdAndEventTimeAfter(UUID patientId, LocalDateTime afterTime);

    /**
     * Find medical events for a patient by category within a time range.
     *
     * @param patientId the patient's UUID
     * @param category  the event category
     * @param startTime the start of the time range
     * @param endTime   the end of the time range
     * @return list of medical events matching the criteria
     */
    List<MedicalEvent> findByPatientIdAndCategoryAndEventTimeBetween(UUID patientId, 
                                                                   MedicalEventCategory category,
                                                                   LocalDateTime startTime, 
                                                                   LocalDateTime endTime);

    /**
     * Find medical events for a patient linked to a specific medication within a time range.
     *
     * @param patientId the patient's UUID
     * @param medicationId the medication's UUID
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of medical events linked to the medication within the time range
     */
    List<MedicalEvent> findByPatientIdAndMedicationIdAndEventTimeBetween(UUID patientId, 
                                                                        UUID medicationId,
                                                                        LocalDateTime startTime, 
                                                                        LocalDateTime endTime);

    /**
     * Count medical events for a patient within a time range.
     *
     * @param patientId the patient's UUID
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return count of medical events within the time range
     */
    long countByPatientIdAndEventTimeBetween(UUID patientId, 
                                           LocalDateTime startTime, 
                                           LocalDateTime endTime);

    /**
     * Get medical events grouped by category for a patient.
     *
     * @param patientId the patient's UUID
     * @return map of category to count
     */
    @Query("SELECT me.category, COUNT(me) FROM MedicalEvent me WHERE me.patientId = :patientId GROUP BY me.category")
    List<Object[]> countByPatientIdGroupByCategory(@Param("patientId") UUID patientId);

    /**
     * Get medical events grouped by severity for a patient.
     *
     * @param patientId the patient's UUID
     * @return map of severity to count
     */
    @Query("SELECT me.severity, COUNT(me) FROM MedicalEvent me WHERE me.patientId = :patientId GROUP BY me.severity")
    List<Object[]> countByPatientIdGroupBySeverity(@Param("patientId") UUID patientId);
}