package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.DosageSchedule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for medication dosage response.
 * Contains all medication dosage information for API responses.
 */
public record MedicationDosageResponse(
        UUID id,
        UUID patientId,
        UUID medicationId,
        LocalDateTime administrationTime,
        BigDecimal dosageAmount,
        String dosageUnit,
        DosageSchedule schedule,
        boolean administered,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}