package com.ciaranmckenna.medical_event_tracker.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Authentication and Security Exception Handlers

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationFailure(Exception ex) {
        // Log the actual error for debugging but don't expose details
        logger.warn("Authentication failed: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "AUTHENTICATION_FAILED",
            "Invalid credentials or user not found",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(UserRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleUserRegistration(UserRegistrationException ex) {
        logger.warn("User registration failed: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "REGISTRATION_FAILED",
            "User registration failed due to invalid data",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "ACCESS_DENIED",
            "Insufficient permissions to access this resource",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // Domain-specific Exception Handlers

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePatientNotFound(PatientNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "PATIENT_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(DuplicatePatientException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePatient(DuplicatePatientException ex) {
        ErrorResponse error = new ErrorResponse(
            "DUPLICATE_PATIENT",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MedicationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMedicationNotFound(MedicationNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "MEDICATION_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
            "INVALID_REQUEST",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName;
            String errorMessage = error.getDefaultMessage();
            
            if (error instanceof FieldError fieldError) {
                // Field-level validation errors
                fieldName = fieldError.getField();
            } else {
                // Class-level validation errors (like @ValidDateRange)
                fieldName = error.getObjectName();
            }
            
            errors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
            "VALIDATION_FAILED",
            "Request validation failed",
            errors,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Log the full exception details for debugging but don't expose to client
        logger.error("Unexpected error occurred", ex);
        
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred. Please try again later.",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp
    ) {}

    public record ValidationErrorResponse(
        String code,
        String message,
        Map<String, String> fieldErrors,
        LocalDateTime timestamp
    ) {}
}