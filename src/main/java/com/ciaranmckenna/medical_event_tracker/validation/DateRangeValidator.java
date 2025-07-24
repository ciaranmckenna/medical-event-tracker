package com.ciaranmckenna.medical_event_tracker.validation;

import com.ciaranmckenna.medical_event_tracker.dto.MedicalEventSearchRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for date range validation in search requests.
 * Ensures that start date is before or equal to end date when both are provided.
 */
public class DateRangeValidator implements ConstraintValidator<ValidDateRange, MedicalEventSearchRequest> {

    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(MedicalEventSearchRequest searchRequest, ConstraintValidatorContext context) {
        if (searchRequest == null) {
            return true; // Let other validations handle null
        }
        
        if (searchRequest.startDate() == null || searchRequest.endDate() == null) {
            return true; // Valid if either date is null
        }
        
        return searchRequest.startDate().isBefore(searchRequest.endDate()) 
            || searchRequest.startDate().isEqual(searchRequest.endDate());
    }
}