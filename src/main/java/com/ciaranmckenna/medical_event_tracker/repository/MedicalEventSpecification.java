package com.ciaranmckenna.medical_event_tracker.repository;

import com.ciaranmckenna.medical_event_tracker.dto.MedicalEventSearchRequest;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEvent;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Specifications for dynamic medical event queries.
 * Provides type-safe, reusable query criteria for advanced medical event searching.
 */
public class MedicalEventSpecification {

    /**
     * Create a specification for medical event search based on the provided criteria.
     * 
     * @param searchRequest the search criteria
     * @return specification for the search
     */
    public static Specification<MedicalEvent> createSpecification(MedicalEventSearchRequest searchRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Patient ID filter (optional - if null, search all patients)
            if (searchRequest.patientId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("patientId"), searchRequest.patientId()));
            }

            // Text search in title and description
            if (searchRequest.searchText() != null && !searchRequest.searchText().trim().isEmpty()) {
                String searchPattern = "%" + searchRequest.searchText().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), searchPattern);
                Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
            }
            
            // Category filter
            if (searchRequest.categories() != null && !searchRequest.categories().isEmpty()) {
                predicates.add(root.get("category").in(searchRequest.categories()));
            }
            
            // Severity filter
            if (searchRequest.severities() != null && !searchRequest.severities().isEmpty()) {
                predicates.add(root.get("severity").in(searchRequest.severities()));
            }
            
            // Medication filter
            if (searchRequest.medicationIds() != null && !searchRequest.medicationIds().isEmpty()) {
                predicates.add(root.get("medicationId").in(searchRequest.medicationIds()));
            }
            
            // Date range filter
            if (searchRequest.startDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("eventTime"), searchRequest.startDate()));
            }
            
            if (searchRequest.endDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("eventTime"), searchRequest.endDate()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Create a specification for finding events by patient ID.
     * 
     * @param patientId the patient's UUID
     * @return specification for patient events
     */
    public static Specification<MedicalEvent> hasPatientId(UUID patientId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("patientId"), patientId);
    }

    /**
     * Create a specification for text search in title and description.
     * 
     * @param searchText the text to search for
     * @return specification for text search
     */
    public static Specification<MedicalEvent> containsText(String searchText) {
        return (root, query, criteriaBuilder) -> {
            if (searchText == null || searchText.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            String searchPattern = "%" + searchText.toLowerCase() + "%";
            Predicate titlePredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("title")), searchPattern);
            Predicate descriptionPredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("description")), searchPattern);
            
            return criteriaBuilder.or(titlePredicate, descriptionPredicate);
        };
    }

    /**
     * Create a specification for category filtering.
     * 
     * @param categories list of categories to filter by
     * @return specification for category filter
     */
    public static Specification<MedicalEvent> hasCategories(List<MedicalEventCategory> categories) {
        return (root, query, criteriaBuilder) -> {
            if (categories == null || categories.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return root.get("category").in(categories);
        };
    }

    /**
     * Create a specification for severity filtering.
     * 
     * @param severities list of severities to filter by
     * @return specification for severity filter
     */
    public static Specification<MedicalEvent> hasSeverities(List<MedicalEventSeverity> severities) {
        return (root, query, criteriaBuilder) -> {
            if (severities == null || severities.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return root.get("severity").in(severities);
        };
    }

    /**
     * Create a specification for medication filtering.
     * 
     * @param medicationIds list of medication IDs to filter by
     * @return specification for medication filter
     */
    public static Specification<MedicalEvent> hasMedicationIds(List<UUID> medicationIds) {
        return (root, query, criteriaBuilder) -> {
            if (medicationIds == null || medicationIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return root.get("medicationId").in(medicationIds);
        };
    }

    /**
     * Create a specification for date range filtering.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return specification for date range filter
     */
    public static Specification<MedicalEvent> hasDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("eventTime"), startDate));
            }
            
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("eventTime"), endDate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}