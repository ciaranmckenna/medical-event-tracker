import { apiClient } from './apiClient';
import type {
  MedicalEventResponse,
  CreateMedicalEventRequest,
  MedicalEventCategory,
  MedicalEventSeverity
} from '../../types/medical';
import { sanitizeEventDescription, sanitizePatientNotes } from '../../utils/sanitization';

export interface MedicalEventSearchParams {
  patientId?: string;
  category?: MedicalEventCategory;
  severity?: MedicalEventSeverity;
  medicationId?: string;
  startDate?: string;
  endDate?: string;
  searchText?: string;
  page?: number;
  size?: number;
}

export interface PagedMedicalEventResponse {
  content: MedicalEventResponse[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  first: boolean;
  last: boolean;
}

/**
 * Service for managing medical events with the new backend architecture
 */
export class MedicalEventService {
  private readonly baseUrl = '/api/medical-events';

  // Get all medical events with pagination and search
  async getMedicalEvents(params: MedicalEventSearchParams = {}): Promise<PagedMedicalEventResponse> {
    try {
      const searchParams = new URLSearchParams();
      
      if (params.patientId) searchParams.append('patientId', params.patientId);
      if (params.category) searchParams.append('category', params.category);
      if (params.severity) searchParams.append('severity', params.severity);
      if (params.medicationId) searchParams.append('medicationId', params.medicationId);
      if (params.startDate) searchParams.append('startDate', params.startDate);
      if (params.endDate) searchParams.append('endDate', params.endDate);
      if (params.searchText) searchParams.append('searchText', params.searchText);
      if (params.page !== undefined) searchParams.append('page', params.page.toString());
      if (params.size !== undefined) searchParams.append('size', params.size.toString());

      const url = `${this.baseUrl}${searchParams.toString() ? `?${searchParams.toString()}` : ''}`;
      return await apiClient.get<PagedMedicalEventResponse>(url);
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to fetch medical events';
      throw new Error(message);
    }
  }

  // Get single medical event by ID
  async getMedicalEvent(id: string): Promise<MedicalEventResponse> {
    try {
      return await apiClient.get<MedicalEventResponse>(`${this.baseUrl}/${id}`);
    } catch (error: any) {
      const message = error.response?.data?.message || 'Medical event not found';
      throw new Error(message);
    }
  }

  // Create new medical event
  async createMedicalEvent(eventData: CreateMedicalEventRequest): Promise<MedicalEventResponse> {
    try {
      // Sanitize input data
      const sanitizedData = {
        ...eventData,
        title: sanitizeEventDescription(eventData.title),
        description: sanitizeEventDescription(eventData.description)
      };

      return await apiClient.post<MedicalEventResponse>(this.baseUrl, sanitizedData);
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to create medical event';
      throw new Error(message);
    }
  }

  // Update existing medical event
  async updateMedicalEvent(id: string, eventData: Partial<CreateMedicalEventRequest>): Promise<MedicalEventResponse> {
    try {
      // Sanitize input data
      const sanitizedData: any = { ...eventData };
      if (eventData.title) sanitizedData.title = sanitizeEventDescription(eventData.title);
      if (eventData.description) sanitizedData.description = sanitizeEventDescription(eventData.description);

      return await apiClient.put<MedicalEventResponse>(`${this.baseUrl}/${id}`, sanitizedData);
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to update medical event';
      throw new Error(message);
    }
  }

  // Delete medical event
  async deleteMedicalEvent(id: string): Promise<void> {
    try {
      await apiClient.delete<void>(`${this.baseUrl}/${id}`);
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to delete medical event';
      throw new Error(message);
    }
  }

  // Search medical events by patient and filters
  async searchMedicalEvents(
    patientId: string,
    searchParams: Omit<MedicalEventSearchParams, 'patientId'>
  ): Promise<PagedMedicalEventResponse> {
    return this.getMedicalEvents({ ...searchParams, patientId });
  }

  // Get recent medical events for a patient
  async getRecentMedicalEvents(patientId: string, days: number = 30): Promise<MedicalEventResponse[]> {
    const endDate = new Date().toISOString().split('T')[0];
    const startDate = new Date(Date.now() - days * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
    
    const response = await this.getMedicalEvents({
      patientId,
      startDate,
      endDate,
      size: 100 // Get more recent events
    });
    
    return response.content;
  }

  // Get events by category for a patient
  async getEventsByCategory(
    patientId: string, 
    category: MedicalEventCategory
  ): Promise<MedicalEventResponse[]> {
    const response = await this.getMedicalEvents({
      patientId,
      category,
      size: 100
    });
    
    return response.content;
  }

  // Get events by severity for a patient
  async getEventsBySeverity(
    patientId: string, 
    severity: MedicalEventSeverity
  ): Promise<MedicalEventResponse[]> {
    const response = await this.getMedicalEvents({
      patientId,
      severity,
      size: 100
    });
    
    return response.content;
  }

  // Utility methods for data processing and display
  formatEventTime(eventTime: string): string {
    const date = new Date(eventTime);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatTimeSince(eventTime: string): string {
    const eventDate = new Date(eventTime);
    const now = new Date();
    const diffMs = now.getTime() - eventDate.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffMinutes = Math.floor(diffMs / (1000 * 60));

    if (diffDays > 0) {
      return `${diffDays} day${diffDays !== 1 ? 's' : ''} ago`;
    } else if (diffHours > 0) {
      return `${diffHours} hour${diffHours !== 1 ? 's' : ''} ago`;
    } else if (diffMinutes > 0) {
      return `${diffMinutes} minute${diffMinutes !== 1 ? 's' : ''} ago`;
    } else {
      return 'Just now';
    }
  }

  // Get event statistics for dashboard
  getEventStatistics(events: MedicalEventResponse[]) {
    const now = new Date();
    const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    const oneMonthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);

    const eventsThisWeek = events.filter(e => new Date(e.eventTime) >= oneWeekAgo).length;
    const eventsThisMonth = events.filter(e => new Date(e.eventTime) >= oneMonthAgo).length;

    // Category distribution
    const categoryStats = events.reduce((acc, event) => {
      acc[event.category] = (acc[event.category] || 0) + 1;
      return acc;
    }, {} as Record<MedicalEventCategory, number>);

    // Severity distribution
    const severityStats = events.reduce((acc, event) => {
      acc[event.severity] = (acc[event.severity] || 0) + 1;
      return acc;
    }, {} as Record<MedicalEventSeverity, number>);

    return {
      totalEvents: events.length,
      eventsThisWeek,
      eventsThisMonth,
      averagePerWeek: eventsThisMonth > 0 ? Math.round((eventsThisMonth / 4) * 10) / 10 : 0,
      categoryDistribution: categoryStats,
      severityDistribution: severityStats,
      mostCommonCategory: Object.entries(categoryStats).reduce((a, b) => 
        categoryStats[a[0] as MedicalEventCategory] > categoryStats[b[0] as MedicalEventCategory] ? a : b, 
        ['SYMPTOM', 0]
      )[0] as MedicalEventCategory,
      criticalEvents: events.filter(e => e.severity === 'CRITICAL').length
    };
  }

  // Validation helper
  validateEventData(eventData: CreateMedicalEventRequest): { isValid: boolean; errors: string[] } {
    const errors: string[] = [];

    if (!eventData.patientId || eventData.patientId.trim() === '') {
      errors.push('Patient ID is required');
    }

    if (!eventData.title || eventData.title.trim() === '') {
      errors.push('Event title is required');
    } else if (eventData.title.length > 200) {
      errors.push('Event title must be 200 characters or less');
    }

    if (!eventData.description || eventData.description.trim() === '') {
      errors.push('Event description is required');
    } else if (eventData.description.length > 2000) {
      errors.push('Event description must be 2000 characters or less');
    }

    if (!eventData.category) {
      errors.push('Event category is required');
    }

    if (!eventData.severity) {
      errors.push('Event severity is required');
    }

    if (!eventData.eventTime) {
      errors.push('Event time is required');
    } else {
      const eventTime = new Date(eventData.eventTime);
      if (eventTime > new Date()) {
        errors.push('Event time cannot be in the future');
      }
    }

    return {
      isValid: errors.length === 0,
      errors
    };
  }

  // Date utility methods
  getDateRange(period: 'week' | 'month' | 'quarter' | 'year'): { start: string; end: string } {
    const end = new Date();
    const start = new Date();

    switch (period) {
      case 'week':
        start.setDate(end.getDate() - 7);
        break;
      case 'month':
        start.setMonth(end.getMonth() - 1);
        break;
      case 'quarter':
        start.setMonth(end.getMonth() - 3);
        break;
      case 'year':
        start.setFullYear(end.getFullYear() - 1);
        break;
    }

    return {
      start: start.toISOString().split('T')[0],
      end: end.toISOString().split('T')[0]
    };
  }
}

// Create and export singleton instance
export const medicalEventService = new MedicalEventService();
export default medicalEventService;