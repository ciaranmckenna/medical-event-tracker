import { z } from 'zod';
import type { EventType, SeizureType, SeverityLevel, MedicalEventFormData, QuickEventLog } from '../../types/medical';

// Seizure Type validation with medical accuracy
export const seizureTypeSchema = z.enum([
  'TONIC_CLONIC',      // Grand mal seizures
  'FOCAL_AWARE',       // Simple partial seizures
  'FOCAL_IMPAIRED',    // Complex partial seizures
  'ABSENCE',           // Petit mal seizures
  'MYOCLONIC',         // Myoclonic seizures
  'ATONIC',            // Drop attacks
  'UNKNOWN'
], {
  errorMap: () => ({ message: 'Please select a valid seizure type' })
});

// Event Type validation
export const eventTypeSchema = z.enum([
  'SEIZURE',
  'MEDICATION_REACTION',
  'SYMPTOM',
  'EMERGENCY',
  'ROUTINE_CHECK',
  'OTHER'
], {
  errorMap: () => ({ message: 'Please select a valid event type' })
});

// Severity Level validation
export const severityLevelSchema = z.enum([
  'MILD',
  'MODERATE', 
  'SEVERE',
  'CRITICAL'
], {
  errorMap: () => ({ message: 'Please select severity level' })
});

// Duration validation for seizures (0.5 seconds to 24 hours)
const durationSchema = z.number()
  .min(0.5, 'Duration must be at least 0.5 seconds')
  .max(86400, 'Duration cannot exceed 24 hours (86400 seconds)')
  .optional();

// Medical dosage validation (for dosageGiven - required field, 0 allowed)
const dosageGivenSchema = z.number({
  required_error: 'Dosage is required (use 0 if no medication given)',
  invalid_type_error: 'Dosage must be a number'
})
  .min(0, 'Dosage cannot be negative')
  .max(10000, 'Dosage exceeds maximum safe limit');

// Weight validation (always required per MVP Stage 3)
const weightSchema = z.number({
  required_error: 'Patient weight is required',
  invalid_type_error: 'Weight must be a number'
})
  .min(0.1, 'Weight must be at least 0.1 kg')
  .max(1000, 'Weight cannot exceed 1000 kg');

// Height validation (optional for patients > 20 years per MVP Stage 3)
const heightSchema = z.number()
  .min(0.1, 'Height must be at least 0.1 cm')
  .max(300, 'Height cannot exceed 300 cm')
  .optional();


// Medical text validation with sanitization
const medicalTextSchema = (maxLength: number) => z.string()
  .max(maxLength, `Text cannot exceed ${maxLength} characters`)
  .refine(
    (val) => !/<script|javascript:|data:/i.test(val),
    'Invalid characters detected'
  );

// Triggers validation (comma-separated list)
const triggersSchema = z.string()
  .max(500, 'Triggers list too long')
  .optional()
  .transform((val) => val ? val.split(',').map(t => t.trim()).filter(Boolean) : undefined);

// Medical Event Form Validation Schema
export const medicalEventFormSchema = z.object({
  patientId: z.string()
    .min(1, 'Patient selection is required'),

  type: eventTypeSchema,

  title: medicalTextSchema(100)
    .min(1, 'Event title is required'),

  description: medicalTextSchema(1000)
    .min(1, 'Event description is required'),

  // Seizure-specific fields (conditional validation)
  seizureType: seizureTypeSchema.optional(),
  
  duration: durationSchema,

  severity: severityLevelSchema,

  // Context fields
  location: medicalTextSchema(200).optional(),
  
  triggers: triggersSchema,

  // Medical response fields
  medicationGiven: medicalTextSchema(100).optional(),

  dosageGiven: dosageGivenSchema,

  emergencyContactCalled: z.boolean().optional(),
  
  hospitalRequired: z.boolean().optional(),

  // Timing validation
  eventDate: z.string()
    .min(1, 'Event date is required')
    .refine(
      (val) => {
        const date = new Date(val);
        const now = new Date();
        const oneYearAgo = new Date(now.getFullYear() - 1, now.getMonth(), now.getDate());
        return date >= oneYearAgo && date <= now;
      },
      'Event date must be within the last year and not in the future'
    ),

  eventTime: z.string()
    .min(1, 'Event time is required')
    .regex(/^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$/, 'Invalid time format (HH:MM)'),

  // Patient measurements at time of event (MVP Stage 3 requirements)
  weightKg: weightSchema,

  heightCm: heightSchema,

  // Additional info
  witnessedBy: medicalTextSchema(200).optional(),
  
  notes: medicalTextSchema(1000).optional()

}).refine(
  (data) => {
    // If event type is SEIZURE, seizureType should be provided
    if (data.type === 'SEIZURE' && !data.seizureType) {
      return false;
    }
    return true;
  },
  {
    message: 'Seizure type is required for seizure events',
    path: ['seizureType']
  }
).refine(
  (data) => {
    // If medication was given, dosage should be provided
    if (data.medicationGiven && !data.dosageGiven) {
      return false;
    }
    return true;
  },
  {
    message: 'Dosage is required when medication is specified',
    path: ['dosageGiven']
  }
);

// Quick Event Log Schema for Emergency Situations
export const quickEventLogSchema = z.object({
  patientId: z.string()
    .min(1, 'Patient selection is required'),

  type: eventTypeSchema,

  severity: severityLevelSchema,

  startTime: z.string()
    .min(1, 'Start time is required'),

  duration: durationSchema,

  notes: medicalTextSchema(500).optional()
});

// Medical Event Search Schema
export const medicalEventSearchSchema = z.object({
  patientId: z.string().optional(),
  type: eventTypeSchema.optional(),
  severity: severityLevelSchema.optional(),
  status: z.enum(['ACTIVE', 'RESOLVED', 'ONGOING', 'REQUIRES_FOLLOW_UP']).optional(),
  dateFrom: z.string().optional(),
  dateTo: z.string().optional(),
  searchTerm: z.string().optional(),
  page: z.number().min(0).optional(),
  size: z.number().min(1).max(100).optional()
});

// Type exports
export type MedicalEventFormData = z.infer<typeof medicalEventFormSchema>;
export type QuickEventLogData = z.infer<typeof quickEventLogSchema>;
export type MedicalEventSearchParams = z.infer<typeof medicalEventSearchSchema>;

// Event Type Display Helpers
export const EVENT_TYPE_DISPLAY = {
  SEIZURE: { 
    label: 'Seizure', 
    icon: '🧠', 
    color: '#dc2626',
    emergencyLevel: 'high',
    description: 'Epileptic seizure or convulsive episode'
  },
  MEDICATION_REACTION: { 
    label: 'Medication Reaction', 
    icon: '💊', 
    color: '#f59e0b',
    emergencyLevel: 'medium',
    description: 'Adverse reaction to medication'
  },
  SYMPTOM: { 
    label: 'Symptom', 
    icon: '🩺', 
    color: '#06b6d4',
    emergencyLevel: 'low',
    description: 'General symptom or observation'
  },
  EMERGENCY: { 
    label: 'Emergency', 
    icon: '🚨', 
    color: '#dc2626',
    emergencyLevel: 'critical',
    description: 'Medical emergency requiring immediate attention'
  },
  ROUTINE_CHECK: { 
    label: 'Routine Check', 
    icon: '📋', 
    color: '#10b981',
    emergencyLevel: 'none',
    description: 'Scheduled medical check or routine observation'
  },
  OTHER: { 
    label: 'Other', 
    icon: '📝', 
    color: '#6b7280',
    emergencyLevel: 'low',
    description: 'Other medical event or observation'
  }
} as const;

export const SEIZURE_TYPE_DISPLAY = {
  TONIC_CLONIC: { 
    label: 'Tonic-Clonic (Grand Mal)', 
    description: 'Generalized seizure with muscle stiffening and jerking',
    commonDuration: '1-3 minutes',
    severity: 'high'
  },
  FOCAL_AWARE: { 
    label: 'Focal Aware (Simple Partial)', 
    description: 'Localized seizure with retained consciousness',
    commonDuration: '30 seconds - 2 minutes',
    severity: 'medium'
  },
  FOCAL_IMPAIRED: { 
    label: 'Focal Impaired (Complex Partial)', 
    description: 'Localized seizure with altered consciousness',
    commonDuration: '1-2 minutes',
    severity: 'medium'
  },
  ABSENCE: { 
    label: 'Absence (Petit Mal)', 
    description: 'Brief loss of consciousness with blank stare',
    commonDuration: '5-30 seconds',
    severity: 'low'
  },
  MYOCLONIC: { 
    label: 'Myoclonic', 
    description: 'Sudden muscle jerks or twitches',
    commonDuration: 'Few seconds',
    severity: 'low'
  },
  ATONIC: { 
    label: 'Atonic (Drop Attack)', 
    description: 'Sudden loss of muscle tone causing falls',
    commonDuration: '10-60 seconds',
    severity: 'medium'
  },
  UNKNOWN: { 
    label: 'Unknown Type', 
    description: 'Seizure type not yet determined',
    commonDuration: 'Variable',
    severity: 'medium'
  }
} as const;

export const SEVERITY_DISPLAY = {
  MILD: { 
    label: 'Mild', 
    color: '#10b981', 
    description: 'Minor symptoms, normal daily activities possible',
    priority: 1
  },
  MODERATE: { 
    label: 'Moderate', 
    color: '#f59e0b', 
    description: 'Noticeable symptoms, some impact on daily activities',
    priority: 2
  },
  SEVERE: { 
    label: 'Severe', 
    color: '#dc2626', 
    description: 'Significant symptoms, major impact on daily activities',
    priority: 3
  },
  CRITICAL: { 
    label: 'Critical', 
    color: '#7c2d12', 
    description: 'Life-threatening, requires immediate medical attention',
    priority: 4
  }
} as const;