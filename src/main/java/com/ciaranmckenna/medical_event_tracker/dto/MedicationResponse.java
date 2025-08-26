package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.Medication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MedicationResponse(
    UUID id,
    String name,
    String genericName,
    Medication.MedicationType type,
    BigDecimal strength,
    String unit,
    String manufacturer,
    String description,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static MedicationResponse of(Medication medication) {
        return new MedicationResponse(
            medication.getId(),
            medication.getName(),
            medication.getGenericName(),
            medication.getType(),
            medication.getStrength(),
            medication.getUnit(),
            medication.getManufacturer(),
            medication.getDescription(),
            medication.isActive(),
            medication.getCreatedAt(),
            medication.getUpdatedAt()
        );
    }
}