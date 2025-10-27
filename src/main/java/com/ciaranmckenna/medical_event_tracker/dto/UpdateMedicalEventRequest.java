package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for updating an existing medical event.
 * Contains all fields that can be updated for a medical event.
 *
 * Height is optional for patients older than 20 years as height growth typically stops by then.
 * Weight is always required as it impacts dosage calculations.
 * Dosage should be 0 if no medication was administered.
 */
public record UpdateMedicalEventRequest(
        @NotNull(message = "Event ID is required")
        UUID id,

        @NotNull(message = "Patient ID is required")
        UUID patientId,

        UUID medicationId,

        @NotNull(message = "Event time is required")
        @PastOrPresent(message = "Event time cannot be in the future")
        LocalDateTime eventTime,

        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title cannot exceed 200 characters")
        String title,

        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        @NotNull(message = "Severity is required")
        MedicalEventSeverity severity,

        @NotNull(message = "Category is required")
        MedicalEventCategory category,

        @NotNull(message = "Weight is required")
        @DecimalMin(value = "0.1", message = "Weight must be greater than 0.1 kg")
        @DecimalMax(value = "1000.0", message = "Weight must be less than 1000 kg")
        @Digits(integer = 4, fraction = 2, message = "Weight must have at most 4 digits before and 2 digits after decimal point")
        BigDecimal weightKg,

        @DecimalMin(value = "0.1", message = "Height must be greater than 0.1 cm")
        @DecimalMax(value = "300.0", message = "Height must be less than 300 cm")
        @Digits(integer = 3, fraction = 2, message = "Height must have at most 3 digits before and 2 digits after decimal point")
        BigDecimal heightCm,

        @NotNull(message = "Dosage is required (use 0 if no medication administered)")
        @DecimalMin(value = "0.0", message = "Dosage cannot be negative")
        @DecimalMax(value = "10000.0", message = "Dosage must be less than 10000")
        @Digits(integer = 5, fraction = 2, message = "Dosage must have at most 5 digits before and 2 digits after decimal point")
        BigDecimal dosageGiven
) {
}