import { apiClient } from './apiClient';
import type { Medication, MedicationDosage, PaginatedResponse } from '../../types/api';
import type { MedicationFormData, MedicationDosageFormData } from '../validation/medicationValidation';

export interface MedicationCreateRequest {
  patientId: string;
  name: string;
  dosage: number;
  unit: string;
  frequency: 'ONCE_DAILY' | 'TWICE_DAILY' | 'THREE_TIMES_DAILY' | 'AS_NEEDED';
  startDate: string;
  endDate?: string;
  active: boolean;
  notes?: string;
}

export interface MedicationUpdateRequest extends MedicationCreateRequest {
  id: string;
}

export interface MedicationDosageCreateRequest {
  patientId: string;
  medicationId: string;
  administrationTime: string;
  dosageAmount: number;
  dosageUnit: string;
  schedule: 'AM' | 'PM';
  administered: boolean;
  notes?: string;
}

export interface MedicationSearchParams {
  page?: number;
  size?: number;
  patientId?: string;
  medicationName?: string;
  active?: boolean;
  frequency?: string;
}

export interface MedicationDosageSearchParams {
  page?: number;
  size?: number;
  patientId?: string;
  medicationId?: string;
  startDate?: string;
  endDate?: string;
  administered?: boolean;
}

// Mock data for testing when backend is not available
const MOCK_MEDICATIONS: Medication[] = [
  {
    id: '1',
    patientId: '1', // John Doe
    name: 'Levetiracetam',
    dosage: 500,
    unit: 'mg',
    frequency: 'TWICE_DAILY',
    startDate: '2024-01-01',
    status: 'ACTIVE',
    notes: 'Take with food to reduce stomach upset',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  },
  {
    id: '2',
    patientId: '3', // Emma Johnson
    name: 'Lamotrigine',
    dosage: 100,
    unit: 'mg',
    frequency: 'TWICE_DAILY',
    startDate: '2024-01-15',
    status: 'ACTIVE',
    notes: 'Gradual dose increase as prescribed',
    createdAt: '2024-01-15T00:00:00Z',
    updatedAt: '2024-01-15T00:00:00Z'
  },
  {
    id: '3',
    patientId: '2', // Sarah Smith
    name: 'Carbamazepine',
    dosage: 200,
    unit: 'mg',
    frequency: 'TWICE_DAILY',
    startDate: '2024-02-01',
    status: 'ACTIVE',
    notes: 'Monitor blood levels regularly',
    createdAt: '2024-02-01T00:00:00Z',
    updatedAt: '2024-02-01T00:00:00Z'
  },
  {
    id: '4',
    patientId: '1', // John Doe
    name: 'Phenytoin',
    dosage: 100,
    unit: 'mg',
    frequency: 'ONCE_DAILY',
    startDate: '2024-01-01',
    endDate: '2024-06-01',
    status: 'INACTIVE',
    notes: 'Discontinued due to side effects',
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-06-01T00:00:00Z'
  },
  {
    id: '5',
    patientId: '4', // George Wilson
    name: 'Valproic Acid',
    dosage: 250,
    unit: 'mg',
    frequency: 'TWICE_DAILY',
    startDate: '2024-03-01',
    status: 'ACTIVE',
    notes: 'Senior patient - monitor liver function',
    createdAt: '2024-03-01T00:00:00Z',
    updatedAt: '2024-03-01T00:00:00Z'
  }
];

export class MedicationService {
  private readonly baseUrl = '/api/medications';
  private readonly dosageUrl = '/api/medication-dosages';
  private useMockData = true; // Force mock data for frontend development

  // Medication CRUD operations
  async getMedications(params: MedicationSearchParams = {}): Promise<PaginatedResponse<Medication>> {
    if (this.useMockData) {
      return this.getMockMedications(params);
    }
    
    try {
      const searchParams = new URLSearchParams();
      
      if (params.page !== undefined) searchParams.append('page', params.page.toString());
      if (params.size !== undefined) searchParams.append('size', params.size.toString());
      if (params.patientId) searchParams.append('patientId', params.patientId);
      if (params.medicationName) searchParams.append('name', params.medicationName);
      if (params.active !== undefined) searchParams.append('active', params.active.toString());
      if (params.frequency) searchParams.append('frequency', params.frequency);

      const url = `${this.baseUrl}${searchParams.toString() ? `?${searchParams.toString()}` : ''}`;
      return apiClient.get<PaginatedResponse<Medication>>(url);
    } catch (error) {
      console.warn('Medications API failed, switching to mock data:', error);
      this.useMockData = true;
      return this.getMockMedications(params);
    }
  }

  async getMedication(id: string): Promise<Medication> {
    if (this.useMockData) {
      const medication = MOCK_MEDICATIONS.find(med => med.id === id);
      if (!medication) throw new Error('Medication not found');
      return medication;
    }
    
    try {
      return apiClient.get<Medication>(`${this.baseUrl}/${id}`);
    } catch (error) {
      console.warn('Medication API failed, switching to mock data:', error);
      this.useMockData = true;
      const medication = MOCK_MEDICATIONS.find(med => med.id === id);
      if (!medication) throw new Error('Medication not found');
      return medication;
    }
  }

  async createMedication(medication: MedicationCreateRequest): Promise<Medication> {
    return apiClient.post<Medication>(this.baseUrl, medication);
  }

  async updateMedication(id: string, medication: MedicationUpdateRequest): Promise<Medication> {
    return apiClient.put<Medication>(`${this.baseUrl}/${id}`, medication);
  }

  async deleteMedication(id: string): Promise<void> {
    return apiClient.delete<void>(`${this.baseUrl}/${id}`);
  }

  // Mock data helper methods
  private getMockMedications(params: MedicationSearchParams = {}): PaginatedResponse<Medication> {
    let filteredMedications = [...MOCK_MEDICATIONS];
    
    // Apply patient filter
    if (params.patientId) {
      filteredMedications = filteredMedications.filter(med => med.patientId === params.patientId);
    }
    
    // Apply medication name filter
    if (params.medicationName) {
      const searchLower = params.medicationName.toLowerCase();
      filteredMedications = filteredMedications.filter(med =>
        med.name.toLowerCase().includes(searchLower)
      );
    }
    
    // Apply active status filter
    if (params.active !== undefined) {
      filteredMedications = filteredMedications.filter(med => 
        params.active ? med.status === 'ACTIVE' : med.status !== 'ACTIVE'
      );
    }
    
    // Apply frequency filter
    if (params.frequency) {
      filteredMedications = filteredMedications.filter(med => med.frequency === params.frequency);
    }
    
    // Sort by creation date (newest first)
    filteredMedications.sort((a, b) => 
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );
    
    // Simple pagination
    const page = params.page || 0;
    const size = params.size || 20;
    const startIndex = page * size;
    const endIndex = startIndex + size;
    const paginatedMedications = filteredMedications.slice(startIndex, endIndex);
    
    return {
      content: paginatedMedications,
      totalElements: filteredMedications.length,
      totalPages: Math.ceil(filteredMedications.length / size),
      page,
      size,
      first: page === 0,
      last: endIndex >= filteredMedications.length
    };
  }

  async getMedicationsByPatient(patientId: string): Promise<Medication[]> {
    const response = await this.getMedications({ patientId, active: true });
    return response.content;
  }

  // Medication dosage operations
  async getMedicationDosages(params: MedicationDosageSearchParams = {}): Promise<PaginatedResponse<MedicationDosage>> {
    const searchParams = new URLSearchParams();
    
    if (params.page !== undefined) searchParams.append('page', params.page.toString());
    if (params.size !== undefined) searchParams.append('size', params.size.toString());
    if (params.patientId) searchParams.append('patientId', params.patientId);
    if (params.medicationId) searchParams.append('medicationId', params.medicationId);
    if (params.startDate) searchParams.append('startDate', params.startDate);
    if (params.endDate) searchParams.append('endDate', params.endDate);
    if (params.administered !== undefined) searchParams.append('administered', params.administered.toString());

    const url = `${this.dosageUrl}${searchParams.toString() ? `?${searchParams.toString()}` : ''}`;
    return apiClient.get<PaginatedResponse<MedicationDosage>>(url);
  }

  async getMedicationDosage(id: string): Promise<MedicationDosage> {
    return apiClient.get<MedicationDosage>(`${this.dosageUrl}/${id}`);
  }

  async createMedicationDosage(dosage: MedicationDosageCreateRequest): Promise<MedicationDosage> {
    return apiClient.post<MedicationDosage>(this.dosageUrl, dosage);
  }

  async updateMedicationDosage(id: string, dosage: Partial<MedicationDosageCreateRequest>): Promise<MedicationDosage> {
    return apiClient.put<MedicationDosage>(`${this.dosageUrl}/${id}`, dosage);
  }

  async deleteMedicationDosage(id: string): Promise<void> {
    return apiClient.delete<void>(`${this.dosageUrl}/${id}`);
  }

  // Helper methods for form transformations
  transformMedicationFormToApiRequest(formData: MedicationFormData): MedicationCreateRequest {
    return {
      patientId: formData.patientId,
      name: formData.name,
      dosage: formData.dosage,
      unit: formData.unit,
      frequency: formData.frequency,
      startDate: formData.startDate,
      endDate: formData.endDate || undefined,
      active: true,
      notes: formData.notes || undefined
    };
  }

  transformMedicationApiToFormData(medication: Medication): MedicationFormData {
    return {
      patientId: medication.patientId,
      name: medication.name,
      dosage: medication.dosage,
      unit: medication.unit,
      frequency: medication.frequency,
      startDate: medication.startDate,
      endDate: medication.endDate || '',
      notes: medication.notes || ''
    };
  }

  transformDosageFormToApiRequest(formData: MedicationDosageFormData): MedicationDosageCreateRequest {
    return {
      patientId: formData.patientId,
      medicationId: formData.medicationId,
      administrationTime: formData.administrationTime,
      dosageAmount: formData.dosageAmount,
      dosageUnit: formData.dosageUnit,
      schedule: formData.schedule,
      administered: formData.administered,
      notes: formData.notes || undefined
    };
  }

  transformDosageApiToFormData(dosage: MedicationDosage): MedicationDosageFormData {
    return {
      patientId: dosage.patientId,
      medicationId: dosage.medicationId,
      administrationTime: dosage.administrationTime,
      dosageAmount: dosage.dosageAmount,
      dosageUnit: dosage.dosageUnit,
      schedule: dosage.schedule,
      administered: dosage.administered,
      notes: dosage.notes || ''
    };
  }

  // Utility methods
  formatMedicationDisplay(medication: Medication): string {
    return `${medication.name} ${medication.dosage}${medication.unit}`;
  }

  formatDosageDisplay(dosage: MedicationDosage): string {
    return `${dosage.dosageAmount}${dosage.dosageUnit} - ${dosage.schedule}`;
  }

  formatFrequencyDisplay(frequency: string): string {
    const displayNames: Record<string, string> = {
      'ONCE_DAILY': 'Once Daily',
      'TWICE_DAILY': 'Twice Daily',
      'THREE_TIMES_DAILY': 'Three Times Daily',
      'AS_NEEDED': 'As Needed'
    };
    return displayNames[frequency] || frequency;
  }

  isActiveMedication(medication: Medication): boolean {
    if (!medication.active) return false;
    
    const today = new Date();
    const startDate = new Date(medication.startDate);
    const endDate = medication.endDate ? new Date(medication.endDate) : null;
    
    return startDate <= today && (!endDate || endDate >= today);
  }

  getMedicationStatus(medication: Medication): 'Active' | 'Inactive' | 'Expired' | 'Future' {
    if (!medication.active) return 'Inactive';
    
    const today = new Date();
    const startDate = new Date(medication.startDate);
    const endDate = medication.endDate ? new Date(medication.endDate) : null;
    
    if (startDate > today) return 'Future';
    if (endDate && endDate < today) return 'Expired';
    return 'Active';
  }

  calculateDaysRemaining(medication: Medication): number | null {
    if (!medication.endDate) return null;
    
    const today = new Date();
    const endDate = new Date(medication.endDate);
    const diffTime = endDate.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    return diffDays;
  }
}

// Create and export singleton instance
export const medicationService = new MedicationService();
export default medicationService;