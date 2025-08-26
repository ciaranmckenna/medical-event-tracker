package com.ciaranmckenna.medical_event_tracker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for password strength requirements.
 * Implements business rules for secure password creation.
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final String UPPERCASE_PATTERN = ".*[A-Z].*";
    private static final String LOWERCASE_PATTERN = ".*[a-z].*";
    private static final String DIGIT_PATTERN = ".*\\d.*";
    private static final String SPECIAL_CHAR_PATTERN = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?].*";
    private static final int MIN_LENGTH = 8;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return false;
        }

        // Check minimum length
        if (password.length() < MIN_LENGTH) {
            addConstraintViolation(context, "Password must be at least " + MIN_LENGTH + " characters long");
            return false;
        }

        // Check for uppercase letter
        if (!password.matches(UPPERCASE_PATTERN)) {
            addConstraintViolation(context, "Password must contain at least one uppercase letter");
            return false;
        }

        // Check for lowercase letter
        if (!password.matches(LOWERCASE_PATTERN)) {
            addConstraintViolation(context, "Password must contain at least one lowercase letter");
            return false;
        }

        // Check for digit
        if (!password.matches(DIGIT_PATTERN)) {
            addConstraintViolation(context, "Password must contain at least one digit");
            return false;
        }

        // Check for special character
        if (!password.matches(SPECIAL_CHAR_PATTERN)) {
            addConstraintViolation(context, "Password must contain at least one special character");
            return false;
        }

        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}