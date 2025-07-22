package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.Patient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PatientResponse(
    UUID id,
    String firstName,
    String lastName,
    String fullName,
    LocalDate dateOfBirth,
    int ageInYears,
    Patient.Gender gender,
    BigDecimal weightKg,
    BigDecimal heightCm,
    BigDecimal bmi,
    String notes,
    boolean active,
    int activeMedicationCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    
    public static PatientResponse fromEntity(Patient patient, int activeMedicationCount) {
        return new PatientResponse(
            patient.getId(),
            patient.getFirstName(),
            patient.getLastName(),
            patient.getFullName(),
            patient.getDateOfBirth(),
            patient.getAgeInYears(),
            patient.getGender(),
            patient.getWeightKg(),
            patient.getHeightCm(),
            patient.getBmiIfAvailable(),
            patient.getNotes(),
            patient.isActive(),
            activeMedicationCount,
            patient.getCreatedAt(),
            patient.getUpdatedAt()
        );
    }
    
    public static PatientResponse fromEntity(Patient patient) {
        return fromEntity(patient, 0);
    }
}