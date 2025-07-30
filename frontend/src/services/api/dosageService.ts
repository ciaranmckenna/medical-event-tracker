import { apiClient } from './apiClient';
import type { PaginatedResponse } from '../../types/api';

export interface DosageRecord {
  id: string;
  medicationId: string;
  patientId: string;
  scheduledTime: string;
  administeredTime?: string;
  dosageAmount: number;
  schedule: 'AM' | 'PM';
  administered: boolean;
  notes?: string;
  administeredBy?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DosageCreateRequest {
  medicationId: string;
  patientId: string;
  schedule: 'AM' | 'PM';
  dosageAmount: number;
  notes?: string;
}

export interface DosageUpdateRequest {
  administered: boolean;
  administeredTime?: string;
  notes?: string;
  administeredBy?: string;
}

export interface DosageSearchParams {
  patientId?: string;
  medicationId?: string;
  dateFrom?: string;
  dateTo?: string;
  administered?: boolean;
  schedule?: 'AM' | 'PM';
  page?: number;
  size?: number;
}

// Mock data for testing when backend is not available
const MOCK_DOSAGE_RECORDS: DosageRecord[] = [
  // John Doe - Comprehensive 30-day medication history
  // Recent days (good adherence period)
  {
    id: '1',
    medicationId: '1', // Levetiracetam
    patientId: '1', // John Doe
    scheduledTime: '2025-07-26T08:00:00Z',
    administeredTime: '2025-07-26T08:15:00Z',
    dosageAmount: 500,
    schedule: 'AM',
    administered: true,
    notes: 'Taken with breakfast',
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-26T08:00:00Z',
    updatedAt: '2025-07-26T08:15:00Z'
  },
  {
    id: '2',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-26T20:00:00Z',
    administeredTime: '2025-07-26T20:10:00Z',
    dosageAmount: 500,
    schedule: 'PM',
    administered: true,
    notes: 'Taken with dinner',
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-26T20:00:00Z',
    updatedAt: '2025-07-26T20:10:00Z'
  },
  // January 25 - Morning dose missed (correlated with seizure)
  {
    id: '3',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-25T08:00:00Z',
    dosageAmount: 500,
    schedule: 'AM',
    administered: false, // MISSED - correlated with morning seizure
    notes: 'Missed due to oversleeping',
    createdAt: '2025-07-25T08:00:00Z',
    updatedAt: '2025-07-25T08:30:00Z'
  },
  {
    id: '4',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-25T20:00:00Z',
    administeredTime: '2025-07-25T20:00:00Z',
    dosageAmount: 500,
    schedule: 'PM',
    administered: true,
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-25T20:00:00Z',
    updatedAt: '2025-07-25T20:00:00Z'
  },
  // January 24
  {
    id: '5',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-24T08:00:00Z',
    administeredTime: '2025-07-24T08:05:00Z',
    dosageAmount: 500,
    schedule: 'AM',
    administered: true,
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-24T08:00:00Z',
    updatedAt: '2025-07-24T08:05:00Z'
  },
  {
    id: '6',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-24T20:00:00Z',
    administeredTime: '2025-07-24T20:15:00Z',
    dosageAmount: 500,
    schedule: 'PM',
    administered: true,
    notes: 'Slightly delayed',
    administeredBy: 'John Doe',
    createdAt: '2025-07-24T20:00:00Z',
    updatedAt: '2025-07-24T20:15:00Z'
  },
  // July 23 - Evening dose late (correlated with evening focal seizure)
  {
    id: '7',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-23T08:00:00Z',
    administeredTime: '2025-07-23T08:00:00Z',
    dosageAmount: 500,
    schedule: 'AM',
    administered: true,
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-23T08:00:00Z',
    updatedAt: '2025-07-23T08:00:00Z'
  },
  {
    id: '8',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-23T20:00:00Z',
    administeredTime: '2025-07-23T21:30:00Z', // LATE - 1.5 hours
    dosageAmount: 500,
    schedule: 'PM',
    administered: true,
    notes: 'Very late - forgot until after seizure',
    administeredBy: 'John Doe',
    createdAt: '2025-07-23T20:00:00Z',
    updatedAt: '2025-07-23T21:30:00Z'
  },
  // July 22
  {
    id: '9',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-22T08:00:00Z',
    administeredTime: '2025-07-22T08:00:00Z',
    dosageAmount: 500,
    schedule: 'AM',
    administered: true,
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-22T08:00:00Z',
    updatedAt: '2025-07-22T08:00:00Z'
  },
  {
    id: '10',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-22T20:00:00Z',
    administeredTime: '2025-07-22T20:05:00Z',
    dosageAmount: 500,
    schedule: 'PM',
    administered: true,
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-22T20:00:00Z',
    updatedAt: '2025-07-22T20:05:00Z'
  },
  // July 21
  {
    id: '11',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-21T08:00:00Z',
    administeredTime: '2025-07-21T08:10:00Z',
    dosageAmount: 500,
    schedule: 'AM',
    administered: true,
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-21T08:00:00Z',
    updatedAt: '2025-07-21T08:10:00Z'
  },
  {
    id: '12',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-21T20:00:00Z',
    administeredTime: '2025-07-21T19:55:00Z',
    dosageAmount: 500,
    schedule: 'PM',
    administered: true,
    administeredBy: 'John Doe',
    createdAt: '2025-07-21T20:00:00Z',
    updatedAt: '2025-07-21T19:55:00Z'
  },
  // July 20 - Good adherence but breakthrough seizure
  {
    id: '13',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-20T08:00:00Z',
    administeredTime: '2025-07-20T08:00:00Z',
    dosageAmount: 500,
    schedule: 'AM',
    administered: true,
    notes: 'On time, good adherence',
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-20T08:00:00Z',
    updatedAt: '2025-07-20T08:00:00Z'
  },
  {
    id: '14',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-20T20:00:00Z',
    administeredTime: '2025-07-20T20:00:00Z',
    dosageAmount: 500,
    schedule: 'PM',
    administered: true,
    notes: 'On time despite afternoon seizure',
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-20T20:00:00Z',
    updatedAt: '2025-07-20T20:00:00Z'
  },
  // July 19
  {
    id: '15',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-19T08:00:00Z',
    administeredTime: '2025-07-19T08:05:00Z',
    dosageAmount: 500,
    schedule: 'AM',
    administered: true,
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-19T08:00:00Z',
    updatedAt: '2025-07-19T08:05:00Z'
  },
  {
    id: '16',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-19T20:00:00Z',
    administeredTime: '2025-07-19T20:20:00Z',
    dosageAmount: 500,
    schedule: 'PM',
    administered: true,
    notes: 'Slightly delayed',
    administeredBy: 'John Doe',
    createdAt: '2025-07-19T20:00:00Z',
    updatedAt: '2025-07-19T20:20:00Z'
  },
  // July 18
  {
    id: '17',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-18T08:00:00Z',
    administeredTime: '2025-07-18T08:00:00Z',
    dosageAmount: 500,
    schedule: 'AM',
    administered: true,
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-18T08:00:00Z',
    updatedAt: '2025-07-18T08:00:00Z'
  },
  {
    id: '18',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-18T20:00:00Z',
    administeredTime: '2025-07-18T20:00:00Z',
    dosageAmount: 500,
    schedule: 'PM',
    administered: true,
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-18T20:00:00Z',
    updatedAt: '2025-07-18T20:00:00Z'
  },
  // July 17
  {
    id: '19',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-17T08:00:00Z',
    administeredTime: '2025-07-17T08:15:00Z',
    dosageAmount: 500,
    schedule: 'AM',
    administered: true,
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-17T08:00:00Z',
    updatedAt: '2025-07-17T08:15:00Z'
  },
  {
    id: '20',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-17T20:00:00Z',
    administeredTime: '2025-07-17T20:10:00Z',
    dosageAmount: 500,
    schedule: 'PM',
    administered: true,
    administeredBy: 'John Doe',
    createdAt: '2025-07-17T20:00:00Z',
    updatedAt: '2025-07-17T20:10:00Z'
  },
  // July 16
  {
    id: '21',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-16T08:00:00Z',
    administeredTime: '2025-07-16T08:30:00Z',
    dosageAmount: 500,
    schedule: 'AM',
    administered: true,
    notes: 'Delayed - weekend sleep-in',
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-16T08:00:00Z',
    updatedAt: '2025-07-16T08:30:00Z'
  },
  {
    id: '22',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-16T20:00:00Z',
    administeredTime: '2025-07-16T19:50:00Z',
    dosageAmount: 500,
    schedule: 'PM',
    administered: true,
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-16T20:00:00Z',
    updatedAt: '2025-07-16T19:50:00Z'
  },
  // July 15 - Evening dose missed (correlated with night seizure)
  {
    id: '23',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-15T08:00:00Z',
    administeredTime: '2025-07-15T08:05:00Z',
    dosageAmount: 500,
    schedule: 'AM',
    administered: true,
    administeredBy: 'Jane Doe',
    createdAt: '2025-07-15T08:00:00Z',
    updatedAt: '2025-07-15T08:05:00Z'
  },
  {
    id: '24',
    medicationId: '1',
    patientId: '1',
    scheduledTime: '2025-07-15T20:00:00Z',
    dosageAmount: 500,
    schedule: 'PM',
    administered: false, // MISSED - correlated with night seizure
    notes: 'Completely forgotten',
    createdAt: '2025-07-15T20:00:00Z',
    updatedAt: '2025-07-15T20:00:00Z'
  },
  {
    id: '5',
    medicationId: '2', // Lamotrigine
    patientId: '3', // Emma Johnson
    scheduledTime: '2025-07-26T08:00:00Z',
    administeredTime: '2025-07-26T08:05:00Z',
    dosageAmount: 100,
    schedule: 'AM',
    administered: true,
    notes: 'School day - taken before breakfast',
    administeredBy: 'Parents',
    createdAt: '2025-07-26T08:00:00Z',
    updatedAt: '2025-07-26T08:05:00Z'
  },
  {
    id: '6',
    medicationId: '2',
    patientId: '3',
    scheduledTime: '2025-07-26T20:00:00Z',
    dosageAmount: 100,
    schedule: 'PM',
    administered: false, // Missed dose
    createdAt: '2025-07-26T20:00:00Z',
    updatedAt: '2025-07-26T20:00:00Z'
  },
  {
    id: '7',
    medicationId: '3', // Carbamazepine  
    patientId: '2', // Sarah Smith
    scheduledTime: '2025-07-26T08:00:00Z',
    administeredTime: '2025-07-26T08:00:00Z',
    dosageAmount: 200,
    schedule: 'AM',
    administered: true,
    administeredBy: 'Sarah Smith',
    createdAt: '2025-07-26T08:00:00Z',
    updatedAt: '2025-07-26T08:00:00Z'
  }
];

export class DosageService {
  private readonly baseUrl = '/api/dosages';
  private useMockData = true; // Force mock data for frontend development

  // Get dosage records with pagination and search
  async getDosageRecords(params: DosageSearchParams = {}): Promise<PaginatedResponse<DosageRecord>> {
    try {
      if (this.useMockData) {
        return this.getMockDosageRecords(params);
      }
      
      const searchParams = new URLSearchParams();
      
      if (params.patientId) searchParams.append('patientId', params.patientId);
      if (params.medicationId) searchParams.append('medicationId', params.medicationId);
      if (params.dateFrom) searchParams.append('dateFrom', params.dateFrom);
      if (params.dateTo) searchParams.append('dateTo', params.dateTo);
      if (params.administered !== undefined) searchParams.append('administered', params.administered.toString());
      if (params.schedule) searchParams.append('schedule', params.schedule);
      if (params.page !== undefined) searchParams.append('page', params.page.toString());
      if (params.size !== undefined) searchParams.append('size', params.size.toString());

      const url = `${this.baseUrl}${searchParams.toString() ? `?${searchParams.toString()}` : ''}`;
      return apiClient.get<PaginatedResponse<DosageRecord>>(url);
    } catch (error) {
      console.warn('Dosage records API failed, switching to mock data:', error);
      this.useMockData = true;
      return this.getMockDosageRecords(params);
    }
  }

  // Get single dosage record by ID
  async getDosageRecord(id: string): Promise<DosageRecord> {
    try {
      if (this.useMockData) {
        const record = MOCK_DOSAGE_RECORDS.find(r => r.id === id);
        if (!record) throw new Error('Dosage record not found');
        return record;
      }
      return apiClient.get<DosageRecord>(`${this.baseUrl}/${id}`);
    } catch (error) {
      console.warn('Dosage record API failed, switching to mock data:', error);
      this.useMockData = true;
      const record = MOCK_DOSAGE_RECORDS.find(r => r.id === id);
      if (!record) throw new Error('Dosage record not found');
      return record;
    }
  }

  // Create new dosage record
  async createDosageRecord(dosage: DosageCreateRequest): Promise<DosageRecord> {
    try {
      if (this.useMockData) {
        const newRecord: DosageRecord = {
          id: (MOCK_DOSAGE_RECORDS.length + 1).toString(),
          ...dosage,
          scheduledTime: this.calculateScheduledTime(dosage.schedule),
          administered: false,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
        MOCK_DOSAGE_RECORDS.push(newRecord);
        return newRecord;
      }
      return apiClient.post<DosageRecord>(this.baseUrl, dosage);
    } catch (error) {
      console.warn('Create dosage record API failed, switching to mock data:', error);
      this.useMockData = true;
      const newRecord: DosageRecord = {
        id: (MOCK_DOSAGE_RECORDS.length + 1).toString(),
        ...dosage,
        scheduledTime: this.calculateScheduledTime(dosage.schedule),
        administered: false,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      MOCK_DOSAGE_RECORDS.push(newRecord);
      return newRecord;
    }
  }

  // Update dosage record (typically to mark as administered)
  async updateDosageRecord(id: string, update: DosageUpdateRequest): Promise<DosageRecord> {
    try {
      if (this.useMockData) {
        const index = MOCK_DOSAGE_RECORDS.findIndex(r => r.id === id);
        if (index === -1) throw new Error('Dosage record not found');
        
        MOCK_DOSAGE_RECORDS[index] = {
          ...MOCK_DOSAGE_RECORDS[index],
          ...update,
          administeredTime: update.administered ? (update.administeredTime || new Date().toISOString()) : undefined,
          updatedAt: new Date().toISOString()
        };
        return MOCK_DOSAGE_RECORDS[index];
      }
      return apiClient.put<DosageRecord>(`${this.baseUrl}/${id}`, update);
    } catch (error) {
      console.warn('Update dosage record API failed, switching to mock data:', error);
      this.useMockData = true;
      const index = MOCK_DOSAGE_RECORDS.findIndex(r => r.id === id);
      if (index === -1) throw new Error('Dosage record not found');
      
      MOCK_DOSAGE_RECORDS[index] = {
        ...MOCK_DOSAGE_RECORDS[index],
        ...update,
        administeredTime: update.administered ? (update.administeredTime || new Date().toISOString()) : undefined,
        updatedAt: new Date().toISOString()
      };
      return MOCK_DOSAGE_RECORDS[index];
    }
  }

  // Mark medication as administered
  async administerMedication(
    medicationId: string, 
    patientId: string,
    schedule: 'AM' | 'PM', 
    dosageAmount: number, 
    notes?: string,
    administeredBy?: string
  ): Promise<DosageRecord> {
    try {
      // First, check if there's already a record for today
      const today = new Date().toISOString().split('T')[0];
      const existingRecord = MOCK_DOSAGE_RECORDS.find(record => 
        record.medicationId === medicationId &&
        record.patientId === patientId &&
        record.schedule === schedule &&
        record.scheduledTime.startsWith(today)
      );

      if (existingRecord) {
        // Update existing record
        return this.updateDosageRecord(existingRecord.id, {
          administered: true,
          administeredTime: new Date().toISOString(),
          notes,
          administeredBy: administeredBy || 'Current User'
        });
      } else {
        // Create new record and immediately mark as administered
        const newRecord = await this.createDosageRecord({
          medicationId,
          patientId,
          schedule,
          dosageAmount,
          notes
        });

        return this.updateDosageRecord(newRecord.id, {
          administered: true,
          administeredTime: new Date().toISOString(),
          administeredBy: administeredBy || 'Current User'
        });
      }
    } catch (error) {
      console.error('Failed to administer medication:', error);
      throw error;
    }
  }

  // Delete dosage record
  async deleteDosageRecord(id: string): Promise<void> {
    try {
      if (this.useMockData) {
        const index = MOCK_DOSAGE_RECORDS.findIndex(r => r.id === id);
        if (index === -1) throw new Error('Dosage record not found');
        MOCK_DOSAGE_RECORDS.splice(index, 1);
        return;
      }
      return apiClient.delete<void>(`${this.baseUrl}/${id}`);
    } catch (error) {
      console.warn('Delete dosage record API failed, switching to mock data:', error);
      this.useMockData = true;
      const index = MOCK_DOSAGE_RECORDS.findIndex(r => r.id === id);
      if (index === -1) throw new Error('Dosage record not found');
      MOCK_DOSAGE_RECORDS.splice(index, 1);
    }
  }

  // Helper method to calculate scheduled time
  private calculateScheduledTime(schedule: 'AM' | 'PM'): string {
    const today = new Date();
    const scheduledHour = schedule === 'AM' ? 8 : 20; // 8 AM or 8 PM
    today.setHours(scheduledHour, 0, 0, 0);
    return today.toISOString();
  }

  // Mock data helper methods
  private getMockDosageRecords(params: DosageSearchParams = {}): PaginatedResponse<DosageRecord> {
    let filteredRecords = [...MOCK_DOSAGE_RECORDS];
    
    // Apply patient filter
    if (params.patientId) {
      filteredRecords = filteredRecords.filter(record => record.patientId === params.patientId);
    }
    
    // Apply medication filter
    if (params.medicationId) {
      filteredRecords = filteredRecords.filter(record => record.medicationId === params.medicationId);
    }
    
    // Apply administered filter
    if (params.administered !== undefined) {
      filteredRecords = filteredRecords.filter(record => record.administered === params.administered);
    }
    
    // Apply schedule filter
    if (params.schedule) {
      filteredRecords = filteredRecords.filter(record => record.schedule === params.schedule);
    }
    
    // Apply date range filter
    if (params.dateFrom || params.dateTo) {
      filteredRecords = filteredRecords.filter(record => {
        const recordDate = new Date(record.scheduledTime);
        if (params.dateFrom && recordDate < new Date(params.dateFrom)) return false;
        if (params.dateTo && recordDate > new Date(params.dateTo)) return false;
        return true;
      });
    }
    
    // Sort by scheduled time (newest first)
    filteredRecords.sort((a, b) => 
      new Date(b.scheduledTime).getTime() - new Date(a.scheduledTime).getTime()
    );
    
    // Simple pagination
    const page = params.page || 0;
    const size = params.size || 50;
    const startIndex = page * size;
    const endIndex = startIndex + size;
    const paginatedRecords = filteredRecords.slice(startIndex, endIndex);
    
    return {
      content: paginatedRecords,
      totalElements: filteredRecords.length,
      totalPages: Math.ceil(filteredRecords.length / size),
      page,
      size,
      first: page === 0,
      last: endIndex >= filteredRecords.length
    };
  }

  // Get adherence statistics
  getAdherenceStats(records: DosageRecord[], medicationId: string, days: number = 7) {
    const cutoffDate = new Date(Date.now() - days * 24 * 60 * 60 * 1000);
    const recentRecords = records.filter(record => 
      record.medicationId === medicationId &&
      new Date(record.scheduledTime) >= cutoffDate
    );

    const totalExpected = recentRecords.length;
    const totalAdministered = recentRecords.filter(record => record.administered).length;
    const adherencePercentage = totalExpected > 0 ? Math.round((totalAdministered / totalExpected) * 100) : 0;

    const missedDoses = recentRecords.filter(record => !record.administered);
    const lateDoses = recentRecords.filter(record => 
      record.administered && 
      record.administeredTime && 
      new Date(record.administeredTime) > new Date(record.scheduledTime + 30 * 60 * 1000) // 30 minutes late
    );

    return {
      totalExpected,
      totalAdministered,
      adherencePercentage,
      missedDoses: missedDoses.length,
      lateDoses: lateDoses.length,
      recentRecords
    };
  }

  // Format time until next dose
  getTimeUntilNextDose(schedule: 'AM' | 'PM'): string {
    const now = new Date();
    const currentHour = now.getHours();
    
    let nextDoseHour: number;
    let daysUntil = 0;
    
    if (schedule === 'AM') {
      nextDoseHour = 8; // 8 AM
      if (currentHour >= 8) {
        daysUntil = 1; // Tomorrow morning
      }
    } else {
      nextDoseHour = 20; // 8 PM
      if (currentHour >= 20) {
        daysUntil = 1; // Tomorrow evening
      }
    }
    
    const nextDose = new Date();
    nextDose.setDate(nextDose.getDate() + daysUntil);
    nextDose.setHours(nextDoseHour, 0, 0, 0);
    
    const diffMs = nextDose.getTime() - now.getTime();
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
    
    if (diffHours < 1) {
      return `${diffMinutes} minutes`;
    } else if (diffHours < 24) {
      return `${diffHours}h ${diffMinutes}m`;
    } else {
      const days = Math.floor(diffHours / 24);
      const hours = diffHours % 24;
      return `${days}d ${hours}h`;
    }
  }
}

// Create and export singleton instance
export const dosageService = new DosageService();
export default dosageService;