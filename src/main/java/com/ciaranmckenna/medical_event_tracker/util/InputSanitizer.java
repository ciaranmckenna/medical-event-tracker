package com.ciaranmckenna.medical_event_tracker.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing user input to prevent XSS attacks.
 * Particularly important for medical data that may be displayed in UI.
 */
@Component
public class InputSanitizer {

    // Patterns for potentially dangerous content
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern VBSCRIPT_PATTERN = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONLOAD_PATTERN = Pattern.compile("onload[^=]*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONERROR_PATTERN = Pattern.compile("onerror[^=]*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONCLICK_PATTERN = Pattern.compile("onclick[^=]*=", Pattern.CASE_INSENSITIVE);

    /**
     * Sanitizes text input by removing potentially dangerous HTML/JavaScript content.
     * Preserves basic formatting while removing security risks.
     * 
     * @param input the raw input string
     * @return sanitized string safe for storage and display
     */
    public String sanitizeText(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        String sanitized = input;
        
        // Remove script tags and their content
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove javascript: and vbscript: protocols
        sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = VBSCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove event handlers
        sanitized = ONLOAD_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = ONERROR_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = ONCLICK_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove all HTML tags (strict approach for medical data)
        sanitized = HTML_PATTERN.matcher(sanitized).replaceAll("");
        
        // Clean up multiple whitespaces
        sanitized = sanitized.replaceAll("\\s+", " ").trim();
        
        return sanitized;
    }

    /**
     * Sanitizes medical data with additional validation.
     * Ensures medical descriptions are clean and safe.
     * 
     * @param medicalText the medical text input
     * @return sanitized medical text
     */
    public String sanitizeMedicalData(String medicalText) {
        if (medicalText == null) {
            return null;
        }

        String sanitized = sanitizeText(medicalText);
        
        // Additional medical data specific sanitization
        // Remove any remaining suspicious patterns
        sanitized = sanitized.replaceAll("[<>\"']", ""); // Remove quotes and brackets
        sanitized = sanitized.replaceAll("&[a-zA-Z0-9#]+;", ""); // Remove HTML entities
        
        return sanitized;
    }

    /**
     * Sanitizes and validates patient notes with length constraints.
     * 
     * @param notes patient notes input
     * @param maxLength maximum allowed length
     * @return sanitized and truncated notes
     */
    public String sanitizePatientNotes(String notes, int maxLength) {
        if (notes == null) {
            return null;
        }

        String sanitized = sanitizeMedicalData(notes);
        
        // Truncate if too long
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength - 3) + "...";
        }
        
        return sanitized;
    }

    /**
     * Validates that input doesn't contain suspicious patterns.
     * Used for additional validation in critical medical forms.
     * 
     * @param input the input to validate
     * @return true if input appears safe, false otherwise
     */
    public boolean isSafeInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return true;
        }

        // Check for dangerous patterns
        if (SCRIPT_PATTERN.matcher(input).find()) return false;
        if (JAVASCRIPT_PATTERN.matcher(input).find()) return false;
        if (VBSCRIPT_PATTERN.matcher(input).find()) return false;
        if (ONLOAD_PATTERN.matcher(input).find()) return false;
        if (ONERROR_PATTERN.matcher(input).find()) return false;
        if (ONCLICK_PATTERN.matcher(input).find()) return false;

        return true;
    }
}