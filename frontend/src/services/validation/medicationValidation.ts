import { z } from 'zod';

// Medication form validation schema
export const medicationSchema = z.object({
  patientId: z
    .string()
    .min(1, 'Patient selection is required'),
  
  name: z
    .string()
    .min(1, 'Medication name is required')
    .max(100, 'Medication name too long')
    .regex(/^[a-zA-Z0-9\s\-\.()]+$/, 'Invalid characters in medication name'),
    
  dosage: z
    .number()
    .positive('Dosage must be positive')
    .max(10000, 'Dosage exceeds maximum safe limit')
    .refine((val) => val % 0.1 === 0, 'Dosage must be in increments of 0.1'),
    
  unit: z
    .string()
    .min(1, 'Dosage unit is required')
    .max(20, 'Unit name too long'),
    
  frequency: z
    .enum(['ONCE_DAILY', 'TWICE_DAILY', 'THREE_TIMES_DAILY', 'AS_NEEDED'], {
      errorMap: () => ({ message: 'Please select a valid frequency' })
    }),
    
  startDate: z
    .string()
    .min(1, 'Start date is required')
    .refine((date) => {
      const startDate = new Date(date);
      const today = new Date();
      const maxFutureDate = new Date();
      maxFutureDate.setFullYear(today.getFullYear() + 2);
      
      return startDate >= new Date('2020-01-01') && startDate <= maxFutureDate;
    }, 'Start date must be between 2020 and 2 years from now'),
    
  endDate: z
    .string()
    .optional()
    .refine((date) => {
      if (!date) return true;
      const endDate = new Date(date);
      const today = new Date();
      const maxFutureDate = new Date();
      maxFutureDate.setFullYear(today.getFullYear() + 5);
      
      return endDate >= today && endDate <= maxFutureDate;
    }, 'End date must be between today and 5 years from now'),
    
  notes: z
    .string()
    .max(500, 'Notes too long (maximum 500 characters)')
    .optional()
    .or(z.literal(''))
}).refine((data) => {
  if (data.endDate) {
    const startDate = new Date(data.startDate);
    const endDate = new Date(data.endDate);
    return endDate >= startDate;
  }
  return true;
}, {
  message: "End date must be after start date",
  path: ["endDate"]
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
  ...data,
  endDate: data.endDate || undefined,
  notes: data.notes || undefined,
  active: true // New medications are active by default
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