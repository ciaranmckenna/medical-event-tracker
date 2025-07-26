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

export interface MedicalEventFormData {
  eventTime: Date;
  title: string;
  description?: string;
  severity: 'MILD' | 'MODERATE' | 'SEVERE' | 'CRITICAL';
  category: 'SYMPTOM' | 'SIDE_EFFECT' | 'ADVERSE_REACTION' | 'OBSERVATION' | 'EMERGENCY' | 'MEDICATION';
  duration?: number;
  medicationId?: string;
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

export const EVENT_CATEGORIES = {
  SYMPTOM: { label: 'Symptom', color: '#06b6d4', icon: '🩺' },
  SIDE_EFFECT: { label: 'Side Effect', color: '#f59e0b', icon: '⚠️' },
  ADVERSE_REACTION: { label: 'Adverse Reaction', color: '#dc2626', icon: '🚨' },
  OBSERVATION: { label: 'Observation', color: '#10b981', icon: '👁️' },
  EMERGENCY: { label: 'Emergency', color: '#dc2626', icon: '🚑' },
  MEDICATION: { label: 'Medication', color: '#8b5cf6', icon: '💊' }
} as const;

export const MEDICATION_FREQUENCIES = {
  ONCE_DAILY: { label: 'Once Daily', times: 1 },
  TWICE_DAILY: { label: 'Twice Daily', times: 2 },
  THREE_TIMES_DAILY: { label: 'Three Times Daily', times: 3 },
  AS_NEEDED: { label: 'As Needed', times: 0 }
} as const;

export const DOSAGE_SCHEDULES = {
  AM: { label: 'Morning', time: '08:00' },
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