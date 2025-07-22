package com.ciaranmckenna.medical_event_tracker.service;

import com.ciaranmckenna.medical_event_tracker.dto.PatientCreateRequest;
import com.ciaranmckenna.medical_event_tracker.dto.PatientResponse;
import com.ciaranmckenna.medical_event_tracker.dto.PatientUpdateRequest;
import com.ciaranmckenna.medical_event_tracker.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PatientService {

    /**
     * Create a new patient for the authenticated user
     */
    PatientResponse createPatient(PatientCreateRequest request, User user);

    /**
     * Get all active patients for the authenticated user
     */
    List<PatientResponse> getActivePatients(User user);

    /**
     * Get all patients (active and inactive) for the authenticated user
     */
    List<PatientResponse> getAllPatients(User user);

    /**
     * Get a specific patient by ID (must belong to authenticated user)
     */
    PatientResponse getPatientById(UUID patientId, User user);

    /**
     * Update an existing patient (must belong to authenticated user)
     */
    PatientResponse updatePatient(UUID patientId, PatientUpdateRequest request, User user);

    /**
     * Soft delete a patient (must belong to authenticated user)
     */
    void deletePatient(UUID patientId, User user);

    /**
     * Search patients by name for the authenticated user
     */
    List<PatientResponse> searchPatientsByName(String searchTerm, User user);

    /**
     * Find patients by age range for the authenticated user
     */
    List<PatientResponse> findPatientsByAgeRange(int minAge, int maxAge, User user);

    /**
     * Get patients with active medications for the authenticated user
     */
    List<PatientResponse> getPatientsWithActiveMedications(User user);

    /**
     * Get count of active patients for the authenticated user
     */
    long getActivePatientCount(User user);

    /**
     * Check if patient exists with the same details (to prevent duplicates)
     */
    boolean patientExists(String firstName, String lastName, LocalDate dateOfBirth, User user);
}