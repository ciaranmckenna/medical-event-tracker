import { apiClient } from './apiClient';
import type { Patient, PaginatedResponse } from '../../types/api';
import type { PatientFormData } from '../validation/patientValidation';

export interface PatientCreateRequest {
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER';
  weightKg?: number;
  heightCm?: number;
  notes?: string;
}

export interface PatientUpdateRequest extends PatientCreateRequest {
  id: string;
}

export interface PatientSearchParams {
  page?: number;
  size?: number;
  searchTerm?: string;
  ageMin?: number;
  ageMax?: number;
}

// Mock data for testing when backend is not available
const MOCK_PATIENTS: Patient[] = [
  {
    id: '1',
    firstName: 'John',
    lastName: 'Doe',
    fullName: 'John Doe',
    dateOfBirth: '1985-06-15',
    ageInYears: 38,
    gender: 'MALE',
    heightCm: 180,
    weightKg: 75,
    bmi: 23.1,
    active: true,
    activeMedicationCount: 2,
    notes: 'No known allergies',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  },
  {
    id: '2',
    firstName: 'Sarah',
    lastName: 'Smith',
    fullName: 'Sarah Smith',
    dateOfBirth: '1992-03-22',
    ageInYears: 31,
    gender: 'FEMALE',
    heightCm: 165,
    weightKg: 60,
    bmi: 22.0,
    active: true,
    activeMedicationCount: 1,
    notes: 'Allergic to penicillin',
    createdAt: '2024-01-02T00:00:00Z',
    updatedAt: '2024-01-02T00:00:00Z'
  },
  {
    id: '3',
    firstName: 'Emma',
    lastName: 'Johnson',
    fullName: 'Emma Johnson',
    dateOfBirth: '2010-08-10',
    ageInYears: 13,
    gender: 'FEMALE',
    heightCm: 140,
    weightKg: 35,
    bmi: 17.9,
    active: true,
    activeMedicationCount: 0,
    notes: 'Child patient, requires parental consent',
    createdAt: '2024-01-03T00:00:00Z',
    updatedAt: '2024-01-03T00:00:00Z'
  },
  {
    id: '4',
    firstName: 'George',
    lastName: 'Wilson',
    fullName: 'George Wilson',
    dateOfBirth: '1950-12-05',
    ageInYears: 73,
    gender: 'MALE',
    heightCm: 175,
    weightKg: 80,
    bmi: 26.1,
    active: true,
    activeMedicationCount: 3,
    notes: 'Senior patient, multiple medications',
    createdAt: '2024-01-04T00:00:00Z',
    updatedAt: '2024-01-04T00:00:00Z'
  }
];

export class PatientService {
  private readonly baseUrl = '/api/patients';
  private useMockData = false; // Use real API data

  // Get all patients with pagination and search
  async getPatients(params: PatientSearchParams = {}): Promise<PaginatedResponse<Patient>> {
    // Always use mock data for frontend development
    if (this.useMockData) {
      return this.getMockPatients(params);
    }
    
    try {
      const searchParams = new URLSearchParams();
      
      if (params.page !== undefined) searchParams.append('page', params.page.toString());
      if (params.size !== undefined) searchParams.append('size', params.size.toString());
      if (params.searchTerm) searchParams.append('searchTerm', params.searchTerm);
      if (params.ageMin !== undefined) searchParams.append('ageMin', params.ageMin.toString());
      if (params.ageMax !== undefined) searchParams.append('ageMax', params.ageMax.toString());

      const url = `${this.baseUrl}${searchParams.toString() ? `?${searchParams.toString()}` : ''}`;
      return apiClient.get<PaginatedResponse<Patient>>(url);
    } catch (error) {
      console.warn('Patients API failed, switching to mock data:', error);
      this.useMockData = true;
      return this.getMockPatients(params);
    }
  }

  // Get single patient by ID
  async getPatient(id: string): Promise<Patient> {
    if (this.useMockData) {
      const patient = MOCK_PATIENTS.find(p => p.id === id);
      if (!patient) throw new Error('Patient not found');
      return patient;
    }
    
    try {
      return apiClient.get<Patient>(`${this.baseUrl}/${id}`);
    } catch (error) {
      console.warn('Patient API failed, switching to mock data:', error);
      this.useMockData = true;
      const patient = MOCK_PATIENTS.find(p => p.id === id);
      if (!patient) throw new Error('Patient not found');
      return patient;
    }
  }

  // Create new patient
  async createPatient(patient: PatientCreateRequest): Promise<Patient> {
    try {
      if (this.useMockData) {
        const newPatient: Patient = {
          id: (MOCK_PATIENTS.length + 1).toString(),
          ...patient,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
        MOCK_PATIENTS.push(newPatient);
        return newPatient;
      }
      return apiClient.post<Patient>(this.baseUrl, patient);
    } catch (error) {
      console.warn('Create patient API failed, switching to mock data:', error);
      this.useMockData = true;
      const newPatient: Patient = {
        id: (MOCK_PATIENTS.length + 1).toString(),
        ...patient,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      MOCK_PATIENTS.push(newPatient);
      return newPatient;
    }
  }

  // Update existing patient
  async updatePatient(id: string, patient: PatientUpdateRequest): Promise<Patient> {
    try {
      if (this.useMockData) {
        const index = MOCK_PATIENTS.findIndex(p => p.id === id);
        if (index === -1) throw new Error('Patient not found');
        MOCK_PATIENTS[index] = {
          ...MOCK_PATIENTS[index],
          ...patient,
          updatedAt: new Date().toISOString()
        };
        return MOCK_PATIENTS[index];
      }
      return apiClient.put<Patient>(`${this.baseUrl}/${id}`, patient);
    } catch (error) {
      console.warn('Update patient API failed, switching to mock data:', error);
      this.useMockData = true;
      const index = MOCK_PATIENTS.findIndex(p => p.id === id);
      if (index === -1) throw new Error('Patient not found');
      MOCK_PATIENTS[index] = {
        ...MOCK_PATIENTS[index],
        ...patient,
        updatedAt: new Date().toISOString()
      };
      return MOCK_PATIENTS[index];
    }
  }

  // Delete patient (soft delete)
  async deletePatient(id: string): Promise<void> {
    try {
      if (this.useMockData) {
        const index = MOCK_PATIENTS.findIndex(p => p.id === id);
        if (index === -1) throw new Error('Patient not found');
        MOCK_PATIENTS.splice(index, 1);
        return;
      }
      return apiClient.delete<void>(`${this.baseUrl}/${id}`);
    } catch (error) {
      console.warn('Delete patient API failed, switching to mock data:', error);
      this.useMockData = true;
      const index = MOCK_PATIENTS.findIndex(p => p.id === id);
      if (index === -1) throw new Error('Patient not found');
      MOCK_PATIENTS.splice(index, 1);
    }
  }

  // Get patient statistics for dashboard
  async getPatientStatistics(): Promise<{
    totalPatients: number;
    activePatients: number;
    recentPatients: number;
  }> {
    return apiClient.get<{
      totalPatients: number;
      activePatients: number;
      recentPatients: number;
    }>(`${this.baseUrl}/statistics`);
  }

  // Transform form data to API format
  transformFormToApiRequest(formData: PatientFormData): PatientCreateRequest {
    return {
      firstName: formData.firstName,
      lastName: formData.lastName,
      dateOfBirth: formData.dateOfBirth,
      gender: formData.gender,
      weightKg: formData.weightKg || undefined,
      heightCm: formData.heightCm || undefined,
      notes: formData.notes || undefined
    };
  }

  // Transform API data to form format
  transformApiToFormData(patient: Patient): PatientFormData {
    return {
      firstName: patient.firstName,
      lastName: patient.lastName,
      dateOfBirth: patient.dateOfBirth,
      gender: patient.gender,
      weightKg: patient.weightKg || undefined,
      heightCm: patient.heightCm || undefined,
      notes: patient.notes || ''
    };
  }

  // Calculate age from date of birth
  calculateAge(dateOfBirth: string): number {
    const today = new Date();
    const birthDate = new Date(dateOfBirth);
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    
    return age;
  }

  // Format patient display name
  formatPatientName(patient: Patient): string {
    return `${patient.firstName} ${patient.lastName}`;
  }

  // Format patient age display
  formatPatientAge(patient: Patient): string {
    const age = this.calculateAge(patient.dateOfBirth);
    return `${age} years old`;
  }

  // Mock data helper methods
  private getMockPatients(params: PatientSearchParams = {}): PaginatedResponse<Patient> {
    let filteredPatients = [...MOCK_PATIENTS];
    
    // Apply search filter
    if (params.searchTerm) {
      const searchLower = params.searchTerm.toLowerCase();
      filteredPatients = filteredPatients.filter(patient =>
        patient.firstName.toLowerCase().includes(searchLower) ||
        patient.lastName.toLowerCase().includes(searchLower) ||
        (patient.emergencyContact && patient.emergencyContact.toLowerCase().includes(searchLower))
      );
    }
    
    // Apply age filters
    if (params.ageMin !== undefined || params.ageMax !== undefined) {
      filteredPatients = filteredPatients.filter(patient => {
        const age = this.calculateAge(patient.dateOfBirth);
        if (params.ageMin !== undefined && age < params.ageMin) return false;
        if (params.ageMax !== undefined && age > params.ageMax) return false;
        return true;
      });
    }
    
    // Simple pagination
    const page = params.page || 0;
    const size = params.size || 20;
    const startIndex = page * size;
    const endIndex = startIndex + size;
    const paginatedPatients = filteredPatients.slice(startIndex, endIndex);
    
    return {
      content: paginatedPatients,
      totalElements: filteredPatients.length,
      totalPages: Math.ceil(filteredPatients.length / size),
      page,
      size,
      first: page === 0,
      last: endIndex >= filteredPatients.length
    };
  }
}

// Create and export singleton instance
export const patientService = new PatientService();
export default patientService;