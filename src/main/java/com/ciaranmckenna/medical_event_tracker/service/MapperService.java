package com.ciaranmckenna.medical_event_tracker.service;

import com.ciaranmckenna.medical_event_tracker.dto.*;
import com.ciaranmckenna.medical_event_tracker.entity.*;

/**
 * Service interface for mapping between DTOs and entities.
 * Centralizes mapping logic to follow DRY principle and Single Responsibility.
 */
public interface MapperService {

    // Medical Event Mappings
    MedicalEvent mapToEntity(CreateMedicalEventRequest request);
    MedicalEvent mapToEntity(UpdateMedicalEventRequest request);
    MedicalEventResponse mapToResponse(MedicalEvent entity);

    // Medication Dosage Mappings
    MedicationDosage mapToEntity(CreateMedicationDosageRequest request);
    MedicationDosage mapToEntity(UpdateMedicationDosageRequest request);
    MedicationDosageResponse mapToResponse(MedicationDosage entity);

    // Patient Mappings
    Patient mapToEntity(PatientCreateRequest request, User user);
    PatientResponse mapToResponse(Patient entity);
    void updateEntityFromRequest(Patient entity, PatientUpdateRequest request);

    // User/Auth Mappings
    AuthResponse mapToAuthResponse(User user, String token, java.time.LocalDateTime expiresAt);
    UserProfileResponse mapToUserProfileResponse(User user);
}