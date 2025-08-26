// API Response Types
export interface ApiResponse<T> {
  data: T;
  message: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  first: boolean;
  last: boolean;
}

// Authentication Types
export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'PRIMARY_USER' | 'SECONDARY_USER' | 'ADMIN';
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

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
  token: string;
  user: User;
  expiresIn: number;
}

// Patient Types
export interface Patient {
  id: string;
  firstName: string;
  lastName: string;
  fullName: string;
  dateOfBirth: string;
  ageInYears: number;
  gender: 'MALE' | 'FEMALE' | 'OTHER';
  weightKg?: number;
  heightCm?: number;
  bmi?: number;
  notes?: string;
  active: boolean;
  activeMedicationCount: number;
  createdAt: string;
  updatedAt: string;
}

// Medication Types
// Medication Catalog - Master list of available medications
export interface MedicationCatalog {
  id: string;
  name: string;
  genericName?: string;
  type: 'TABLET' | 'CAPSULE' | 'LIQUID' | 'INJECTION' | 'TOPICAL' | 'INHALER' | 'PATCH' | 'SUPPOSITORY' | 'OTHER';
  strength?: number;
  unit?: string;
  manufacturer?: string;
  description?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

// Patient Medication - Specific medication assigned to a patient
export interface Medication {
  id: string;
  patientId: string;
  name: string;
  dosage: number;
  unit: string;
  frequency: 'ONCE_DAILY' | 'TWICE_DAILY' | 'THREE_TIMES_DAILY' | 'AS_NEEDED';
  startDate: string;
  endDate?: string;
  active: boolean;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface MedicationDosage {
  id: string;
  patientId: string;
  medicationId: string;
  administrationTime: string;
  dosageAmount: number;
  dosageUnit: string;
  schedule: 'AM' | 'PM';
  administered: boolean;
  notes?: string;
  createdAt: string;
}

// Medical Event Types
export interface MedicalEvent {
  id: string;
  patientId: string;
  medicationId?: string;
  eventTime: string;
  title: string;
  description?: string;
  severity: 'MILD' | 'MODERATE' | 'SEVERE' | 'CRITICAL';
  category: 'SYMPTOM' | 'SIDE_EFFECT' | 'ADVERSE_REACTION' | 'OBSERVATION' | 'EMERGENCY' | 'MEDICATION';
  duration?: number;
  resolved: boolean;
  createdAt: string;
  updatedAt: string;
}

// Analytics Types
export interface DashboardSummary {
  patientId: string;
  totalEvents: number;
  totalDosages: number;
  eventsByCategory: Record<string, number>;
  eventsBySeverity: Record<string, number>;
  recentEventsLast7Days: number;
  generatedAt: string;
}

export interface MedicationCorrelationAnalysis {
  medicationId: string;
  patientId: string;
  medicationName: string;
  totalDosages: number;
  totalEventsAfterDosage: number;
  correlationPercentage: number;
  correlationStrength: number;
  eventsByCategoryCount: Record<string, number>;
  eventsBySeverityCount: Record<string, number>;
  analysisGeneratedAt: string;
}

export interface TimelineAnalysis {
  patientId: string;
  periodStart: string;
  periodEnd: string;
  dataPoints: TimelineDataPoint[];
  generatedAt: string;
}

export interface TimelineDataPoint {
  timestamp: string;
  type: 'MEDICATION' | 'EVENT';
  medicationId?: string;
  medicationName?: string;
  dosageAmount?: number;
  eventId?: string;
  eventTitle?: string;
  eventSeverity?: string;
  eventCategory?: string;
}

export interface MedicationImpactAnalysis {
  medicationId: string;
  patientId: string;
  medicationName: string;
  analysisPeriodStart: string;
  analysisPeriodEnd: string;
  totalDosages: number;
  totalEventsAfterDosage: number;
  correlationPercentage: number;
  beforePeriodEvents: number;
  afterPeriodEvents: number;
  improvementPercentage: number;
  effectivenessScore: number;
  trendData: Record<string, number[]>;
  analysisGeneratedAt: string;
}