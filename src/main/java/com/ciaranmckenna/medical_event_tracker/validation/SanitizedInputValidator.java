package com.ciaranmckenna.medical_event_tracker.validation;

import com.ciaranmckenna.medical_event_tracker.util.InputSanitizer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Validator for the @SanitizedInput annotation.
 * Ensures medical data input is safe and sanitized.
 */
public class SanitizedInputValidator implements ConstraintValidator<SanitizedInput, String> {

    @Autowired
    private InputSanitizer inputSanitizer;

    private boolean allowHtml;
    private int maxLength;

    @Override
    public void initialize(SanitizedInput constraintAnnotation) {
        this.allowHtml = constraintAnnotation.allowHtml();
        this.maxLength = constraintAnnotation.maxLength();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null values are handled by @NotNull if required
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        // Check if input contains unsafe patterns
        if (!inputSanitizer.isSafeInput(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Input contains potentially dangerous content that could pose security risks"
            ).addConstraintViolation();
            return false;
        }

        // Check length after sanitization if specified
        if (maxLength > 0) {
            String sanitized = allowHtml ? 
                inputSanitizer.sanitizeText(value) : 
                inputSanitizer.sanitizeMedicalData(value);
            
            if (sanitized.length() > maxLength) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Input is too long after sanitization (max " + maxLength + " characters)"
                ).addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}