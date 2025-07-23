package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for medical event response.
 * Contains all medical event information for API responses.
 */
public record MedicalEventResponse(
        UUID id,
        UUID patientId,
        UUID medicationId,
        LocalDateTime eventTime,
        String title,
        String description,
        MedicalEventSeverity severity,
        MedicalEventCategory category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}