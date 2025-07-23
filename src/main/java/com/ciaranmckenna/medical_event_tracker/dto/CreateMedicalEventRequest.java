package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for creating a new medical event.
 * Contains all required and optional fields for medical event creation.
 */
public record CreateMedicalEventRequest(
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
        MedicalEventCategory category
) {
}