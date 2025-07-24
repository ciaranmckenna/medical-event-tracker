package com.ciaranmckenna.medical_event_tracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for date range validation.
 * Ensures that start date is before or equal to end date.
 */
@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    
    String message() default "Start date must be before or equal to end date";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}