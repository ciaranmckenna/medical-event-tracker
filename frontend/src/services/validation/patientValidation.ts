import { z } from 'zod';

// Patient form validation schema
export const patientSchema = z.object({
  firstName: z
    .string()
    .min(1, 'First name is required')
    .max(50, 'First name too long')
    .regex(/^[a-zA-Z\s-']+$/, 'First name can only contain letters, spaces, hyphens, and apostrophes'),
  
  lastName: z
    .string()
    .min(1, 'Last name is required')
    .max(50, 'Last name too long')
    .regex(/^[a-zA-Z\s-']+$/, 'Last name can only contain letters, spaces, hyphens, and apostrophes'),
  
  dateOfBirth: z
    .string()
    .min(1, 'Date of birth is required')
    .refine((date) => {
      const birthDate = new Date(date);
      const today = new Date();
      const maxAge = new Date();
      maxAge.setFullYear(today.getFullYear() - 150);
      
      return birthDate <= today && birthDate >= maxAge;
    }, 'Please enter a valid date of birth'),
  
  height: z
    .number()
    .optional()
    .refine((val) => val === undefined || (val >= 30 && val <= 300), {
      message: 'Height must be between 30cm and 300cm'
    }),
  
  weight: z
    .number()
    .optional()
    .refine((val) => val === undefined || (val >= 0.5 && val <= 1000), {
      message: 'Weight must be between 0.5kg and 1000kg'
    }),
  
  emergencyContact: z
    .string()
    .max(100, 'Emergency contact name too long')
    .optional()
    .or(z.literal('')),
  
  emergencyPhone: z
    .string()
    .max(20, 'Phone number too long')
    .regex(/^[\d\s\-\+\(\)]*$/, 'Please enter a valid phone number')
    .optional()
    .or(z.literal('')),
  
  notes: z
    .string()
    .max(1000, 'Notes too long (maximum 1000 characters)')
    .optional()
    .or(z.literal(''))
});

export type PatientFormData = z.infer<typeof patientSchema>;

// Transform form data for API submission
export const transformPatientForSubmission = (data: PatientFormData) => ({
  ...data,
  height: data.height || undefined,
  weight: data.weight || undefined,
  emergencyContact: data.emergencyContact || undefined,
  emergencyPhone: data.emergencyPhone || undefined,
  notes: data.notes || undefined
});

// Patient search/filter validation
export const patientSearchSchema = z.object({
  searchTerm: z.string().optional(),
  ageMin: z.number().min(0).max(150).optional(),
  ageMax: z.number().min(0).max(150).optional()
}).refine((data) => {
  if (data.ageMin !== undefined && data.ageMax !== undefined) {
    return data.ageMin <= data.ageMax;
  }
  return true;
}, {
  message: "Minimum age cannot be greater than maximum age",
  path: ["ageMin"]
});

export type PatientSearchData = z.infer<typeof patientSearchSchema>;