import { z } from 'zod';

// Medication form validation schema
export const medicationSchema = z.object({
  name: z
    .string()
    .min(1, 'Medication name is required')
    .max(100, 'Medication name too long')
    .regex(/^[a-zA-Z0-9\s\-\.()]+$/, 'Invalid characters in medication name'),
  
  type: z
    .enum(['TABLET', 'CAPSULE', 'LIQUID', 'INJECTION', 'TOPICAL', 'INHALER', 'PATCH', 'SUPPOSITORY', 'OTHER'], {
      errorMap: () => ({ message: 'Please select a medication type' })
    }),
    
  genericName: z
    .string()
    .max(100, 'Generic name too long')
    .optional(),
    
  strength: z
    .number()
    .positive('Strength must be positive')
    .max(10000, 'Strength exceeds maximum safe limit')
    .optional(),
    
  unit: z
    .string()
    .max(20, 'Unit name too long')
    .optional(),
    
  manufacturer: z
    .string()
    .max(100, 'Manufacturer name too long')
    .optional(),
    
  description: z
    .string()
    .max(500, 'Description too long')
    .optional()
});

export type MedicationFormData = z.infer<typeof medicationSchema>;

// Medication dosage recording schema
export const medicationDosageSchema = z.object({
  patientId: z
    .string()
    .min(1, 'Patient selection is required'),
    
  medicationId: z
    .string()
    .min(1, 'Medication selection is required'),
    
  administrationTime: z
    .string()
    .min(1, 'Administration time is required')
    .refine((datetime) => {
      const adminTime = new Date(datetime);
      const now = new Date();
      const maxPast = new Date();
      maxPast.setDate(now.getDate() - 30); // Max 30 days in past
      
      return adminTime >= maxPast && adminTime <= now;
    }, 'Administration time must be within the last 30 days and not in the future'),
    
  dosageAmount: z
    .number()
    .positive('Dosage amount must be positive')
    .max(10000, 'Dosage amount exceeds maximum safe limit'),
    
  dosageUnit: z
    .string()
    .min(1, 'Dosage unit is required')
    .max(20, 'Unit name too long'),
    
  schedule: z
    .enum(['AM', 'PM'], {
      errorMap: () => ({ message: 'Please select AM or PM' })
    }),
    
  administered: z
    .boolean()
    .default(true),
    
  notes: z
    .string()
    .max(500, 'Notes too long (maximum 500 characters)')
    .optional()
    .or(z.literal(''))
});

export type MedicationDosageFormData = z.infer<typeof medicationDosageSchema>;

// Medication search/filter validation
export const medicationSearchSchema = z.object({
  patientId: z.string().optional(),
  medicationName: z.string().optional(),
  active: z.boolean().optional(),
  frequency: z.enum(['ONCE_DAILY', 'TWICE_DAILY', 'THREE_TIMES_DAILY', 'AS_NEEDED']).optional()
});

export type MedicationSearchData = z.infer<typeof medicationSearchSchema>;

// Transform form data for API submission
export const transformMedicationForSubmission = (data: MedicationFormData) => ({
  name: data.name,
  type: data.type,
  genericName: data.genericName || undefined,
  strength: data.strength || undefined,
  unit: data.unit || undefined,
  manufacturer: data.manufacturer || undefined,
  description: data.description || undefined
});

export const transformMedicationDosageForSubmission = (data: MedicationDosageFormData) => ({
  ...data,
  notes: data.notes || undefined
});

// Frequency display helpers
export const getFrequencyDisplayName = (frequency: string): string => {
  const displayNames: Record<string, string> = {
    'ONCE_DAILY': 'Once Daily',
    'TWICE_DAILY': 'Twice Daily', 
    'THREE_TIMES_DAILY': 'Three Times Daily',
    'AS_NEEDED': 'As Needed'
  };
  return displayNames[frequency] || frequency;
};

export const getFrequencyDescription = (frequency: string): string => {
  const descriptions: Record<string, string> = {
    'ONCE_DAILY': 'Take once per day',
    'TWICE_DAILY': 'Take twice per day (morning and evening)',
    'THREE_TIMES_DAILY': 'Take three times per day (morning, afternoon, evening)', 
    'AS_NEEDED': 'Take as needed for symptoms'
  };
  return descriptions[frequency] || frequency;
};