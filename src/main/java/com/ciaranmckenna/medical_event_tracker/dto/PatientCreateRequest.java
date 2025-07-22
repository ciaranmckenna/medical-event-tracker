package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.Patient;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PatientCreateRequest(
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    String lastName,

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth,

    @NotNull(message = "Gender is required")
    Patient.Gender gender,

    @DecimalMin(value = "0.1", message = "Weight must be greater than 0.1 kg")
    @DecimalMax(value = "1000.0", message = "Weight must be less than 1000 kg")
    @Digits(integer = 4, fraction = 2, message = "Weight must have at most 4 digits before and 2 digits after decimal point")
    BigDecimal weightKg,

    @DecimalMin(value = "0.1", message = "Height must be greater than 0.1 cm")
    @DecimalMax(value = "300.0", message = "Height must be less than 300 cm")
    @Digits(integer = 3, fraction = 2, message = "Height must have at most 3 digits before and 2 digits after decimal point")
    BigDecimal heightCm,

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    String notes
) {
}