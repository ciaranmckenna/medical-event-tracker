package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.DosageSchedule;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for creating a new medication dosage record.
 * Contains all required and optional fields for dosage creation.
 */
public record CreateMedicationDosageRequest(
        @NotNull(message = "Patient ID is required")
        UUID patientId,

        @NotNull(message = "Medication ID is required")
        UUID medicationId,

        @NotNull(message = "Administration time is required")
        @PastOrPresent(message = "Administration time cannot be in the future")
        LocalDateTime administrationTime,

        @NotNull(message = "Dosage amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Dosage amount must be greater than 0")
        @Digits(integer = 8, fraction = 3, message = "Dosage amount must have at most 8 integer digits and 3 decimal places")
        BigDecimal dosageAmount,

        @NotBlank(message = "Dosage unit is required")
        @Size(max = 20, message = "Dosage unit cannot exceed 20 characters")
        String dosageUnit,

        @NotNull(message = "Schedule is required")
        DosageSchedule schedule,

        boolean administered,

        @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        String notes
) {
}