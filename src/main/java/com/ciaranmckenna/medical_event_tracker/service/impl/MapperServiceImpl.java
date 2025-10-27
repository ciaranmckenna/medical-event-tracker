package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.dto.*;
import com.ciaranmckenna.medical_event_tracker.entity.*;
import com.ciaranmckenna.medical_event_tracker.service.MapperService;
import com.ciaranmckenna.medical_event_tracker.util.InputSanitizer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of MapperService providing centralized mapping between DTOs and entities.
 * Includes input sanitization for security and follows DRY principle.
 */
@Service
public class MapperServiceImpl implements MapperService {

    private final InputSanitizer inputSanitizer;

    public MapperServiceImpl(InputSanitizer inputSanitizer) {
        this.inputSanitizer = inputSanitizer;
    }

    // Medical Event Mappings

    @Override
    public MedicalEvent mapToEntity(CreateMedicalEventRequest request) {
        MedicalEvent event = new MedicalEvent();
        event.setPatientId(request.patientId());
        event.setMedicationId(request.medicationId());
        event.setEventTime(request.eventTime());
        event.setTitle(inputSanitizer.sanitizeMedicalData(request.title()));
        event.setDescription(inputSanitizer.sanitizeMedicalData(request.description()));
        event.setSeverity(request.severity());
        event.setCategory(request.category());
        event.setWeightKg(request.weightKg());
        event.setHeightCm(request.heightCm());
        event.setDosageGiven(request.dosageGiven());
        return event;
    }

    @Override
    public MedicalEvent mapToEntity(UpdateMedicalEventRequest request) {
        MedicalEvent event = new MedicalEvent();
        event.setId(request.id());
        event.setPatientId(request.patientId());
        event.setMedicationId(request.medicationId());
        event.setEventTime(request.eventTime());
        event.setTitle(inputSanitizer.sanitizeMedicalData(request.title()));
        event.setDescription(inputSanitizer.sanitizeMedicalData(request.description()));
        event.setSeverity(request.severity());
        event.setCategory(request.category());
        event.setWeightKg(request.weightKg());
        event.setHeightCm(request.heightCm());
        event.setDosageGiven(request.dosageGiven());
        return event;
    }

    @Override
    public MedicalEventResponse mapToResponse(MedicalEvent entity) {
        return new MedicalEventResponse(
                entity.getId(),
                entity.getPatientId(),
                entity.getMedicationId(),
                entity.getEventTime(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getSeverity(),
                entity.getCategory(),
                entity.getWeightKg(),
                entity.getHeightCm(),
                entity.getDosageGiven(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    // Medication Dosage Mappings

    @Override
    public MedicationDosage mapToEntity(CreateMedicationDosageRequest request) {
        MedicationDosage dosage = new MedicationDosage();
        dosage.setPatientId(request.patientId());
        dosage.setMedicationId(request.medicationId());
        dosage.setDosageAmount(request.dosageAmount());
        dosage.setAdministrationTime(request.administrationTime());
        dosage.setSchedule(request.schedule());
        dosage.setNotes(inputSanitizer.sanitizePatientNotes(request.notes(), 1000));
        return dosage;
    }

    @Override
    public MedicationDosage mapToEntity(UpdateMedicationDosageRequest request) {
        MedicationDosage dosage = new MedicationDosage();
        dosage.setId(request.id());
        dosage.setPatientId(request.patientId());
        dosage.setMedicationId(request.medicationId());
        dosage.setDosageAmount(request.dosageAmount());
        dosage.setAdministrationTime(request.administrationTime());
        dosage.setSchedule(request.schedule());
        dosage.setNotes(inputSanitizer.sanitizePatientNotes(request.notes(), 1000));
        return dosage;
    }

    @Override
    public MedicationDosageResponse mapToResponse(MedicationDosage entity) {
        return new MedicationDosageResponse(
                entity.getId(),
                entity.getPatientId(),
                entity.getMedicationId(),
                entity.getAdministrationTime(),
                entity.getDosageAmount(),
                entity.getDosageUnit(),
                entity.getSchedule(),
                entity.isAdministered(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    // Patient Mappings

    @Override
    public Patient mapToEntity(PatientCreateRequest request, User user) {
        Patient patient = new Patient(
                inputSanitizer.sanitizeText(request.firstName()),
                inputSanitizer.sanitizeText(request.lastName()),
                request.dateOfBirth(),
                request.gender(),
                user
        );

        patient.setWeightKg(request.weightKg());
        patient.setHeightCm(request.heightCm());
        patient.setNotes(inputSanitizer.sanitizePatientNotes(request.notes(), 500));

        return patient;
    }

    @Override
    public PatientResponse mapToResponse(Patient entity) {
        // Use existing static factory method from PatientResponse
        return PatientResponse.fromEntity(entity);
    }

    @Override
    public void updateEntityFromRequest(Patient entity, PatientUpdateRequest request) {
        entity.setFirstName(inputSanitizer.sanitizeText(request.firstName()));
        entity.setLastName(inputSanitizer.sanitizeText(request.lastName()));
        entity.setDateOfBirth(request.dateOfBirth());
        entity.setGender(request.gender());
        entity.setWeightKg(request.weightKg());
        entity.setHeightCm(request.heightCm());
        entity.setNotes(inputSanitizer.sanitizePatientNotes(request.notes(), 500));
    }

    // User/Auth Mappings

    @Override
    public AuthResponse mapToAuthResponse(User user, String token, LocalDateTime expiresAt) {
        return AuthResponse.of(user, token, expiresAt);
    }

    @Override
    public UserProfileResponse mapToUserProfileResponse(User user) {
        return UserProfileResponse.of(user);
    }
}