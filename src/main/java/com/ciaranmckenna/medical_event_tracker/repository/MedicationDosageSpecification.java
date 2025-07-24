package com.ciaranmckenna.medical_event_tracker.repository;

import com.ciaranmckenna.medical_event_tracker.dto.MedicationDosageSearchRequest;
import com.ciaranmckenna.medical_event_tracker.entity.DosageSchedule;
import com.ciaranmckenna.medical_event_tracker.entity.MedicationDosage;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Specifications for dynamic medication dosage queries.
 * Provides type-safe, reusable query criteria for advanced dosage searching and adherence analysis.
 */
public class MedicationDosageSpecification {

    /**
     * Create a specification for medication dosage search based on the provided criteria.
     * 
     * @param searchRequest the search criteria
     * @return specification for the search
     */
    public static Specification<MedicationDosage> createSpecification(MedicationDosageSearchRequest searchRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Patient ID is always required
            predicates.add(criteriaBuilder.equal(root.get("patientId"), searchRequest.patientId()));
            
            // Medication filter
            if (searchRequest.medicationIds() != null && !searchRequest.medicationIds().isEmpty()) {
                predicates.add(root.get("medicationId").in(searchRequest.medicationIds()));
            }
            
            // Schedule filter
            if (searchRequest.schedules() != null && !searchRequest.schedules().isEmpty()) {
                predicates.add(root.get("schedule").in(searchRequest.schedules()));
            }
            
            // Administered status filter
            if (searchRequest.administered() != null) {
                predicates.add(criteriaBuilder.equal(root.get("administered"), searchRequest.administered()));
            }
            
            // Date range filter
            if (searchRequest.startDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("administrationTime"), searchRequest.startDate()));
            }
            
            if (searchRequest.endDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("administrationTime"), searchRequest.endDate()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Create a specification for finding dosages by patient ID.
     * 
     * @param patientId the patient's UUID
     * @return specification for patient dosages
     */
    public static Specification<MedicationDosage> hasPatientId(UUID patientId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("patientId"), patientId);
    }

    /**
     * Create a specification for medication filtering.
     * 
     * @param medicationIds list of medication IDs to filter by
     * @return specification for medication filter
     */
    public static Specification<MedicationDosage> hasMedicationIds(List<UUID> medicationIds) {
        return (root, query, criteriaBuilder) -> {
            if (medicationIds == null || medicationIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return root.get("medicationId").in(medicationIds);
        };
    }

    /**
     * Create a specification for schedule filtering.
     * 
     * @param schedules list of dosage schedules to filter by
     * @return specification for schedule filter
     */
    public static Specification<MedicationDosage> hasSchedules(List<DosageSchedule> schedules) {
        return (root, query, criteriaBuilder) -> {
            if (schedules == null || schedules.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return root.get("schedule").in(schedules);
        };
    }

    /**
     * Create a specification for administered status filtering.
     * 
     * @param administered the administered status to filter by
     * @return specification for administered status filter
     */
    public static Specification<MedicationDosage> isAdministered(Boolean administered) {
        return (root, query, criteriaBuilder) -> {
            if (administered == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("administered"), administered);
        };
    }

    /**
     * Create a specification for date range filtering.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return specification for date range filter
     */
    public static Specification<MedicationDosage> hasDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("administrationTime"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("administrationTime"), endDate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Create a specification for finding missed doses (not administered and past due).
     * 
     * @param cutoffTime the time before which doses are considered missed
     * @return specification for missed doses
     */
    public static Specification<MedicationDosage> isMissed(LocalDateTime cutoffTime) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("administered"), false),
                criteriaBuilder.lessThan(root.get("administrationTime"), cutoffTime)
            );
    }

    /**
     * Create a specification for finding upcoming doses.
     * 
     * @param fromTime the start time for upcoming doses
     * @param toTime the end time for upcoming doses
     * @return specification for upcoming doses
     */
    public static Specification<MedicationDosage> isUpcoming(LocalDateTime fromTime, LocalDateTime toTime) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("administered"), false),
                criteriaBuilder.between(root.get("administrationTime"), fromTime, toTime)
            );
    }
}