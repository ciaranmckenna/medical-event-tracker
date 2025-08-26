package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.Medication;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateMedicationRequest(
    @NotBlank(message = "Medication name is required")
    @Size(min = 1, max = 100, message = "Medication name must be between 1 and 100 characters")
    String name,

    @Size(max = 100, message = "Generic name cannot exceed 100 characters")
    String genericName,

    @NotNull(message = "Medication type is required")
    Medication.MedicationType type,

    @DecimalMin(value = "0.001", message = "Strength must be greater than 0")
    @DecimalMax(value = "10000.0", message = "Strength must be less than 10000")
    @Digits(integer = 5, fraction = 3, message = "Strength must have at most 5 digits before and 3 digits after decimal point")
    BigDecimal strength,

    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    String unit,

    @Size(max = 100, message = "Manufacturer cannot exceed 100 characters")
    String manufacturer,

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description
) {}