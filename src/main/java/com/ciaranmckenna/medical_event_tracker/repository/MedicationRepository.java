package com.ciaranmckenna.medical_event_tracker.repository;

import com.ciaranmckenna.medical_event_tracker.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, UUID> {

    /**
     * Find all active medications
     */
    List<Medication> findByActiveTrue();

    /**
     * Find active medication by ID
     */
    Optional<Medication> findByIdAndActiveTrue(UUID id);

    /**
     * Find medications by name pattern
     */
    @Query("SELECT m FROM Medication m WHERE m.active = true AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.genericName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Medication> findByNameContaining(@Param("searchTerm") String searchTerm);

    /**
     * Find medications by type
     */
    List<Medication> findByTypeAndActiveTrue(Medication.MedicationType type);

    /**
     * Find medications by manufacturer
     */
    List<Medication> findByManufacturerAndActiveTrue(String manufacturer);

    /**
     * Check if medication exists by name (case insensitive)
     */
    boolean existsByNameIgnoreCaseAndActiveTrue(String name);

    /**
     * Find most commonly used medications (based on patient assignments)
     */
    @Query("SELECT m FROM Medication m JOIN m.patientMedications pm " +
           "WHERE m.active = true AND pm.active = true " +
           "GROUP BY m ORDER BY COUNT(pm) DESC")
    List<Medication> findMostCommonlyUsed();
}