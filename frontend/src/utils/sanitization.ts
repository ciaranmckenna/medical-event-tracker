import DOMPurify from 'dompurify';

/**
 * Medical data sanitization utilities
 * Provides frontend sanitization to complement backend security measures
 */

export interface SanitizationOptions {
  allowedTags?: string[];
  allowedAttributes?: string[];
  maxLength?: number;
  removeEmptyLines?: boolean;
}

/**
 * Sanitizes medical input text to prevent XSS and ensure data integrity
 */
export const sanitizeMedicalInput = (input: string, options: SanitizationOptions = {}): string => {
  if (!input || typeof input !== 'string') {
    return '';
  }

  const {
    allowedTags = [],
    allowedAttributes = [],
    maxLength = 2000,
    removeEmptyLines = true
  } = options;

  // Configure DOMPurify for medical data (very restrictive)
  const sanitized = DOMPurify.sanitize(input, {
    ALLOWED_TAGS: allowedTags,
    ALLOWED_ATTR: allowedAttributes,
    RETURN_DOM_FRAGMENT: false,
    RETURN_DOM: false,
    RETURN_TRUSTED_TYPE: false
  });

  let cleaned = sanitized.trim();

  // Remove excessive whitespace and newlines
  if (removeEmptyLines) {
    cleaned = cleaned.replace(/\n\s*\n/g, '\n').trim();
  }

  // Enforce length limits
  if (cleaned.length > maxLength) {
    cleaned = cleaned.substring(0, maxLength).trim();
  }

  return cleaned;
};

/**
 * Sanitizes patient notes with specific medical context
 */
export const sanitizePatientNotes = (notes: string): string => {
  return sanitizeMedicalInput(notes, {
    maxLength: 1000,
    removeEmptyLines: true
  });
};

/**
 * Sanitizes medication names and descriptions
 */
export const sanitizeMedicationData = (text: string): string => {
  return sanitizeMedicalInput(text, {
    maxLength: 500,
    removeEmptyLines: true
  });
};

/**
 * Sanitizes medical event descriptions
 */
export const sanitizeEventDescription = (description: string): string => {
  return sanitizeMedicalInput(description, {
    maxLength: 2000,
    removeEmptyLines: true
  });
};

/**
 * Sanitizes patient names (very strict)
 */
export const sanitizePatientName = (name: string): string => {
  if (!name || typeof name !== 'string') {
    return '';
  }

  // Very strict: only letters, spaces, hyphens, apostrophes
  const namePattern = /^[a-zA-Z\s\-']+$/;
  const cleaned = name.trim().replace(/\s+/g, ' ');

  if (!namePattern.test(cleaned)) {
    throw new Error('Invalid characters in name');
  }

  if (cleaned.length > 50) {
    throw new Error('Name too long');
  }

  return cleaned;
};

/**
 * Sanitizes email addresses
 */
export const sanitizeEmail = (email: string): string => {
  if (!email || typeof email !== 'string') {
    return '';
  }

  const cleaned = email.trim().toLowerCase();
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (!emailPattern.test(cleaned)) {
    throw new Error('Invalid email format');
  }

  return cleaned;
};

/**
 * Sanitizes phone numbers
 */
export const sanitizePhoneNumber = (phone: string): string => {
  if (!phone || typeof phone !== 'string') {
    return '';
  }

  // Remove all non-digit characters except +
  const cleaned = phone.replace(/[^\d+]/g, '');
  
  // Basic phone validation
  if (cleaned.length < 10 || cleaned.length > 16) {
    throw new Error('Invalid phone number length');
  }

  return cleaned;
};

/**
 * Validates and sanitizes dosage amounts
 */
export const sanitizeDosageAmount = (amount: number): number => {
  if (typeof amount !== 'number' || isNaN(amount)) {
    throw new Error('Invalid dosage amount');
  }

  if (amount < 0.1 || amount > 10000) {
    throw new Error('Dosage amount out of valid range');
  }

  // Round to 3 decimal places for medical precision
  return Math.round(amount * 1000) / 1000;
};

/**
 * Validates and sanitizes height (in cm)
 */
export const sanitizeHeight = (height: number): number => {
  if (typeof height !== 'number' || isNaN(height)) {
    throw new Error('Invalid height value');
  }

  if (height < 10 || height > 300) {
    throw new Error('Height out of valid range');
  }

  return Math.round(height * 100) / 100; // 2 decimal places
};

/**
 * Validates and sanitizes weight (in kg)
 */
export const sanitizeWeight = (weight: number): number => {
  if (typeof weight !== 'number' || isNaN(weight)) {
    throw new Error('Invalid weight value');
  }

  if (weight < 0.5 || weight > 1000) {
    throw new Error('Weight out of valid range');
  }

  return Math.round(weight * 100) / 100; // 2 decimal places
};

/**
 * Comprehensive form data sanitization for medical forms
 */
export const sanitizeFormData = <T extends Record<string, any>>(
  data: T, 
  fieldMappings: Record<keyof T, (value: any) => any>
): T => {
  const sanitized = { ...data };

  for (const [field, sanitizer] of Object.entries(fieldMappings)) {
    if (sanitized[field] !== undefined) {
      try {
        sanitized[field] = sanitizer(sanitized[field]);
      } catch (error) {
        throw new Error(`Validation failed for ${field}: ${error.message}`);
      }
    }
  }

  return sanitized;
};

/**
 * Escape special characters for safe display
 */
export const escapeForDisplay = (text: string): string => {
  if (!text || typeof text !== 'string') {
    return '';
  }

  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;')
    .replace(/\//g, '&#x2F;');
};

/**
 * Medical-specific input validation patterns
 */
export const MEDICAL_PATTERNS = {
  PATIENT_NAME: /^[a-zA-Z\s\-']{1,50}$/,
  MEDICATION_NAME: /^[a-zA-Z0-9\s\-\.()]{1,100}$/,
  DOSAGE_UNIT: /^[a-zA-Z]{1,20}$/,
  USERNAME: /^[a-zA-Z0-9_]{3,50}$/,
  EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PHONE: /^[+]?[1-9][\d]{9,15}$/
} as const;

export default {
  sanitizeMedicalInput,
  sanitizePatientNotes,
  sanitizeMedicationData,
  sanitizeEventDescription,
  sanitizePatientName,
  sanitizeEmail,
  sanitizePhoneNumber,
  sanitizeDosageAmount,
  sanitizeHeight,
  sanitizeWeight,
  sanitizeFormData,
  escapeForDisplay,
  MEDICAL_PATTERNS
};