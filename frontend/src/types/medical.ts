// Backend-aligned Medical Event Types
export type MedicalEventCategory = 
  | 'ADVERSE_REACTION'
  | 'APPOINTMENT'
  | 'EMERGENCY'
  | 'MEDICATION'
  | 'OBSERVATION'
  | 'SYMPTOM'
  | 'TEST';

export type MedicalEventSeverity = 'MILD' | 'MODERATE' | 'SEVERE' | 'CRITICAL';

export type DosageSchedule = 
  | 'AM'
  | 'AS_NEEDED'
  | 'BEDTIME'
  | 'CUSTOM'
  | 'EVERY_12_HOURS'
  | 'EVERY_4_HOURS'
  | 'EVERY_6_HOURS'
  | 'EVERY_8_HOURS'
  | 'MIDDAY'
  | 'PM';

export type Gender = 'MALE' | 'FEMALE' | 'OTHER' | 'PREFER_NOT_TO_SAY';
export type UserRole = 'ADMIN' | 'PRIMARY_USER' | 'SECONDARY_USER';

// Backend-aligned Response Interfaces
export interface MedicalEventResponse {
  id: string;
  patientId: string;
  title: string;
  description: string;
  category: MedicalEventCategory;
  severity: MedicalEventSeverity;
  eventTime: string;
  medicationId?: string;
  // Patient measurements at time of event (MVP Stage 3)
  weightKg: number;
  heightCm?: number;
  dosage: number;
  createdAt: string;
  updatedAt: string;
}

export interface MedicationDosageResponse {
  id: string;
  patientId: string;
  medicationId: string;
  administrationTime: string;
  dosageAmount: number;
  dosageUnit: string;
  schedule: DosageSchedule;
  administered: boolean;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PatientResponse {
  id: string;
  userId: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: Gender;
  heightCm?: number;
  weightKg?: number;
  notes?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

// Analytics DTOs matching backend
export interface MedicationCorrelationAnalysis {
  medicationId: string;
  patientId: string;
  medicationName: string;
  totalDosages: number;
  eventsWithin24Hours: number;
  correlationPercentage: number;
  correlationStrength: number;
  eventsByCategory: Record<MedicalEventCategory, number>;
  eventsBySeverity: Record<MedicalEventSeverity, number>;
  analysisGeneratedAt: string;
}

export interface MedicationImpactAnalysis {
  medicationId: string;
  patientId: string;
  medicationName: string;
  analysisPeriodStart: string;
  analysisPeriodEnd: string;
  totalDosages: number;
  eventsWithin24Hours: number;
  eventRatePercentage: number;
  symptomEvents: number;
  adverseReactionEvents: number;
  symptomReductionPercentage: number;
  effectivenessScore: number;
  weeklyTrends: Record<string, number[]>;
  analysisGeneratedAt: string;
}

export interface TimelineDataPoint {
  timestamp: string;
  eventType: 'EVENT' | 'DOSAGE';
  description: string;
  value?: number;
  unit?: string;
  severity?: MedicalEventSeverity;
}

export interface TimelineAnalysis {
  patientId: string;
  periodStart: string;
  periodEnd: string;
  dataPoints: TimelineDataPoint[];
  generatedAt: string;
}

export interface DashboardSummary {
  patientId: string;
  totalEvents: number;
  totalDosages: number;
  recentEvents: number;
  recentDosages: number;
  eventsByCategory: Record<MedicalEventCategory, number>;
  eventsBySeverity: Record<MedicalEventSeverity, number>;
  weeklyStatistics: Record<string, number>;
  generatedAt: string;
}

// Request DTOs
export interface CreateMedicalEventRequest {
  patientId: string;
  title: string;
  description: string;
  category: MedicalEventCategory;
  severity: MedicalEventSeverity;
  eventTime: string;
  medicationId?: string;
  // Medical measurements required as per MVP Stage 3 requirements
  weightKg: number;        // Patient weight in kg (always required)
  heightCm?: number;       // Patient height in cm (optional for patients > 20 years)
  dosage: number;          // Medication dosage (0 if no medication given)
}

export interface CreateMedicationDosageRequest {
  patientId: string;
  medicationId: string;
  administrationTime: string;
  dosageAmount: number;
  dosageUnit: string;
  schedule: DosageSchedule;
  notes?: string;
}

export interface PatientCreateRequest {
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: Gender;
  heightCm?: number;
  weightKg?: number;
  notes?: string;
}

// Auth DTOs
export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface AuthResponse {
  id: string | UUID;  // Backend sends UUID, frontend converts to string
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  token: string;
  expiresAt: string | LocalDateTime;  // Backend sends LocalDateTime, frontend converts to string
  tokenType: string;
}

// Helper type for UUID handling
type UUID = string;
type LocalDateTime = string;

export interface UserProfileResponse {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  enabled: boolean;
  createdAt: string;
}

// Enhanced Medical Event Interface for Seizure Tracking
export interface MedicalEvent {
  id: string;
  patientId: string;
  type: EventType;
  title: string;
  description: string;

  // Seizure-specific fields
  seizureType?: SeizureType;
  duration?: number; // in seconds
  severity: SeverityLevel;
  status: EventStatus;

  // Location and context
  location?: string;
  triggers?: string[];

  // Medical response
  medicationGiven?: string;
  dosageGiven?: number;
  emergencyContactCalled?: boolean;
  hospitalRequired?: boolean;

  // Patient measurements at time of event (MVP Stage 3)
  weightKg?: number;        // Patient weight in kg
  heightCm?: number;        // Patient height in cm
  dosage?: number;          // Medication dosage for this event

  // Timestamps
  eventTimestamp: string; // When the event occurred
  reportedTimestamp: string; // When it was logged
  resolvedTimestamp?: string; // When it was resolved

  // Metadata
  reportedBy: string; // User who reported
  witnessedBy?: string[];
  notes?: string;
  attachments?: string[]; // URLs to images/videos

  // System fields
  createdAt: string;
  updatedAt: string;
}

// Medical Form Types
export interface MedicationFormData {
  name: string;
  dosage: number;
  unit: string;
  frequency: 'ONCE_DAILY' | 'TWICE_DAILY' | 'THREE_TIMES_DAILY' | 'AS_NEEDED';
  startDate: Date;
  endDate?: Date;
  notes?: string;
}

export interface PatientFormData {
  firstName: string;
  lastName: string;
  dateOfBirth: Date;
  height?: number;
  weight?: number;
  emergencyContact?: string;
  emergencyPhone?: string;
  notes?: string;
}

// Enhanced Medical Event Form for Seizure Logging
export interface MedicalEventFormData {
  patientId: string;
  type: EventType;
  title: string;
  description: string;

  // Seizure-specific
  seizureType?: SeizureType;
  duration?: number;
  severity: SeverityLevel;

  // Context
  location?: string;
  triggers?: string;

  // Medical response
  medicationGiven?: string;
  dosageGiven?: number;
  emergencyContactCalled?: boolean;
  hospitalRequired?: boolean;

  // Timing
  eventDate: string;
  eventTime: string;

  // Patient measurements (MVP Stage 3 requirements)
  weightKg: number;        // Patient weight at time of event (always required)
  heightCm?: number;       // Patient height at time of event (optional for patients > 20 years)

  // Additional info
  witnessedBy?: string;
  notes?: string;
}

// Quick emergency logging interface
export interface QuickEventLog {
  patientId: string;
  type: EventType;
  severity: SeverityLevel;
  startTime: string;
  duration?: number;
  notes?: string;
}

export interface DosageFormData {
  medicationId: string;
  administrationTime: Date;
  dosageAmount: number;
  schedule: 'AM' | 'PM';
  administered: boolean;
  notes?: string;
}

// Medical Constants
export const SEVERITY_LEVELS = {
  MILD: { label: 'Mild', color: '#84cc16', priority: 1 },
  MODERATE: { label: 'Moderate', color: '#f97316', priority: 2 },
  SEVERE: { label: 'Severe', color: '#dc2626', priority: 3 },
  CRITICAL: { label: 'Critical', color: '#dc2626', priority: 4 }
} as const;

export const MEDICAL_EVENT_CATEGORIES = {
  ADVERSE_REACTION: { label: 'Adverse Reaction', color: '#dc2626', icon: '🚨' },
  APPOINTMENT: { label: 'Appointment', color: '#06b6d4', icon: '📅' },
  EMERGENCY: { label: 'Emergency', color: '#dc2626', icon: '🚑' },
  MEDICATION: { label: 'Medication', color: '#8b5cf6', icon: '💊' },
  OBSERVATION: { label: 'Observation', color: '#10b981', icon: '👁️' },
  SYMPTOM: { label: 'Symptom', color: '#06b6d4', icon: '🩺' },
  TEST: { label: 'Test', color: '#f59e0b', icon: '🧪' }
} as const;

export const MEDICATION_FREQUENCIES = {
  ONCE_DAILY: { label: 'Once Daily', times: 1 },
  TWICE_DAILY: { label: 'Twice Daily', times: 2 },
  THREE_TIMES_DAILY: { label: 'Three Times Daily', times: 3 },
  AS_NEEDED: { label: 'As Needed', times: 0 }
} as const;

export const DOSAGE_SCHEDULES = {
  AM: { label: 'Morning', time: '08:00' },
  AS_NEEDED: { label: 'As Needed', time: null },
  BEDTIME: { label: 'Bedtime', time: '22:00' },
  CUSTOM: { label: 'Custom', time: null },
  EVERY_12_HOURS: { label: 'Every 12 Hours', time: null },
  EVERY_4_HOURS: { label: 'Every 4 Hours', time: null },
  EVERY_6_HOURS: { label: 'Every 6 Hours', time: null },
  EVERY_8_HOURS: { label: 'Every 8 Hours', time: null },
  MIDDAY: { label: 'Midday', time: '12:00' },
  PM: { label: 'Evening', time: '20:00' }
} as const;

// Medical Validation Rules
export const MEDICAL_VALIDATION = {
  DOSAGE: {
    MIN: 0.1,
    MAX: 10000,
    PRECISION: 0.1
  },
  WEIGHT: {
    MIN: 0.5,
    MAX: 1000
  },
  HEIGHT: {
    MIN: 10,
    MAX: 300
  },
  NAME_LENGTH: {
    MIN: 1,
    MAX: 100
  },
  DESCRIPTION_LENGTH: {
    MAX: 500
  },
  PHONE_PATTERN: /^[+]?[1-9][\d]{0,15}$/
} as const;

// Chart Configuration Types
export interface ChartDataPoint {
  timestamp: string;
  value: number;
  type: 'medication' | 'event';
  severity?: string;
  category?: string;
  medicationName?: string;
  eventTitle?: string;
}

export interface CorrelationChartProps {
  data: ChartDataPoint[];
  medications: Medication[];
  events: MedicalEvent[];
  timeRange: {
    start: Date;
    end: Date;
  };
}

// Medical Alert Types
export interface MedicalAlert {
  id: string;
  type: 'warning' | 'error' | 'info' | 'success';
  severity: 'low' | 'medium' | 'high' | 'critical';
  title: string;
  message: string;
  timestamp: string;
  dismissed: boolean;
  autoHide?: boolean;
  duration?: number;
}

// Offline Sync Types
export interface OfflineData {
  id: string;
  type: 'medication' | 'event' | 'dosage' | 'patient';
  data: unknown;
  timestamp: string;
  synced: boolean;
}

// Search Filter Types
export interface MedicalSearchFilters {
  dateRange?: {
    start: Date;
    end: Date;
  };
  severity?: string[];
  category?: string[];
  medicationId?: string;
  patientId?: string;
  searchText?: string;
  sortBy?: 'date' | 'severity' | 'category';
  sortOrder?: 'asc' | 'desc';
}