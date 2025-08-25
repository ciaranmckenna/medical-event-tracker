package com.ciaranmckenna.medical_event_tracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to ensure input is sanitized and safe for medical data.
 * Validates that the input doesn't contain potentially dangerous HTML/JavaScript content.
 */
@Documented
@Constraint(validatedBy = SanitizedInputValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SanitizedInput {
    
    String message() default "Input contains potentially unsafe content";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * Whether to allow basic HTML formatting.
     * If false (default), all HTML is stripped.
     */
    boolean allowHtml() default false;
    
    /**
     * Maximum allowed length after sanitization.
     * -1 means no limit.
     */
    int maxLength() default -1;
}