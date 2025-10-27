package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing a single data point in a timeline analysis.
 * Can represent either a medication dosage or a medical event occurrence.
 * Includes BMI calculation for medical events with weight and height data.
 */
public record TimelineDataPoint(

        @NotNull(message = "Timestamp is required for timeline data point")
        LocalDateTime timestamp,

        @NotNull(message = "Event type is required for timeline data point")
        String eventType,

        @NotNull(message = "Description is required for timeline data point")
        String description,

        BigDecimal value,

        String unit,

        MedicalEventSeverity severity,

        /**
         * Body Mass Index calculated from weight and height data.
         * Only populated for EVENT types with valid weight/height measurements.
         * Null for DOSAGE types or when weight/height data is unavailable.
         */
        BigDecimal bmi
) {
    
    /**
     * Checks if this data point represents a medication dosage.
     * 
     * @return true if event type is "DOSAGE"
     */
    public boolean isDosage() {
        return "DOSAGE".equals(eventType);
    }
    
    /**
     * Checks if this data point represents a medical event.
     * 
     * @return true if event type is "EVENT"
     */
    public boolean isMedicalEvent() {
        return "EVENT".equals(eventType);
    }
    
    /**
     * Checks if this data point has a measurable value (e.g., dosage amount).
     * 
     * @return true if value is not null
     */
    public boolean hasQuantitativeValue() {
        return value != null;
    }
    
    /**
     * Checks if this is a high-severity event.
     * 
     * @return true if severity is SEVERE or CRITICAL
     */
    public boolean isHighSeverity() {
        return severity == MedicalEventSeverity.SEVERE || severity == MedicalEventSeverity.CRITICAL;
    }
    
    /**
     * Gets a formatted display string for the value and unit.
     *
     * @return formatted string like "500.0 mg" or empty string if no value
     */
    public String getFormattedValue() {
        if (value == null) {
            return "";
        }

        if (unit != null && !unit.trim().isEmpty()) {
            return value + " " + unit;
        }

        return value.toString();
    }

    /**
     * Checks if this data point has BMI data available.
     *
     * @return true if bmi field is not null
     */
    public boolean hasBMI() {
        return bmi != null;
    }

    /**
     * Gets formatted BMI string for display.
     *
     * @return formatted BMI like "23.5" or empty string if no BMI data
     */
    public String getFormattedBMI() {
        if (bmi == null) {
            return "";
        }
        return bmi.toString();
    }
}