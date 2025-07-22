package com.ciaranmckenna.medical_event_tracker.repository;

import com.ciaranmckenna.medical_event_tracker.entity.Patient;
import com.ciaranmckenna.medical_event_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    /**
     * Find all active patients for a specific user
     */
    List<Patient> findByUserAndActiveTrue(User user);

    /**
     * Find all patients (active and inactive) for a specific user
     */
    List<Patient> findByUser(User user);

    /**
     * Find active patient by ID and user (for security)
     */
    Optional<Patient> findByIdAndUserAndActiveTrue(UUID id, User user);

    /**
     * Find patient by ID and user (including inactive)
     */
    Optional<Patient> findByIdAndUser(UUID id, User user);

    /**
     * Find patients by name pattern for a specific user
     */
    @Query("SELECT p FROM Patient p WHERE p.user = :user AND p.active = true AND " +
           "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Patient> findByUserAndNameContaining(@Param("user") User user, @Param("searchTerm") String searchTerm);

    /**
     * Find patients by age range for a specific user
     */
    @Query("SELECT p FROM Patient p WHERE p.user = :user AND p.active = true AND " +
           "p.dateOfBirth BETWEEN :startDate AND :endDate")
    List<Patient> findByUserAndDateOfBirthBetween(@Param("user") User user, 
                                                 @Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate);

    /**
     * Count active patients for a user
     */
    long countByUserAndActiveTrue(User user);

    /**
     * Find patients with medications
     */
    @Query("SELECT DISTINCT p FROM Patient p JOIN p.medications pm WHERE p.user = :user AND p.active = true AND pm.active = true")
    List<Patient> findByUserWithActiveMedications(@Param("user") User user);

    /**
     * Check if patient exists by name and date of birth for a user (to prevent duplicates)
     */
    boolean existsByUserAndFirstNameAndLastNameAndDateOfBirthAndActiveTrue(
        User user, String firstName, String lastName, LocalDate dateOfBirth);
}