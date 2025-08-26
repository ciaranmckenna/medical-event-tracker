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
  
  gender: z.enum(['MALE', 'FEMALE', 'OTHER'], {
    required_error: 'Gender is required'
  }),
  
  heightCm: z
    .number()
    .optional()
    .refine((val) => val === undefined || (val >= 30 && val <= 300), {
      message: 'Height must be between 30cm and 300cm'
    }),
  
  weightKg: z
    .number()
    .optional()
    .refine((val) => val === undefined || (val >= 0.5 && val <= 1000), {
      message: 'Weight must be between 0.5kg and 1000kg'
    }),
  
  notes: z
    .string()
    .max(500, 'Notes too long (maximum 500 characters)')
    .optional()
    .or(z.literal(''))
});

export type PatientFormData = z.infer<typeof patientSchema>;

// Transform form data for API submission
export const transformPatientForSubmission = (data: PatientFormData) => ({
  firstName: data.firstName,
  lastName: data.lastName,
  dateOfBirth: data.dateOfBirth,
  gender: data.gender,
  weightKg: data.weightKg || undefined,
  heightCm: data.heightCm || undefined,
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