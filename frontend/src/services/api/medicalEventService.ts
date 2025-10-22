import { apiClient } from './apiClient';
import type { MedicalEvent, PaginatedResponse } from '../../types/api';
import type { 
  MedicalEventFormData, 
  MedicalEventSearchParams,
  EventType, 
  SeverityLevel,
  SeizureType 
} from '../../types/medical';

export interface MedicalEventCreateRequest {
  patientId: string;
  type: EventType;
  title: string;
  description: string;
  seizureType?: SeizureType;
  duration?: number;
  severity: SeverityLevel;
  location?: string;
  triggers?: string[];
  medicationGiven?: string;
  dosageGiven?: number;
  emergencyContactCalled?: boolean;
  hospitalRequired?: boolean;
  eventTimestamp: string;
  witnessedBy?: string[];
  notes?: string;
}

export interface MedicalEventUpdateRequest extends MedicalEventCreateRequest {
  id: string;
}

// localStorage helper functions
const STORAGE_KEY = 'medical-events-data';

const loadMockEventsFromStorage = (): MedicalEvent[] => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      return JSON.parse(stored);
    }
  } catch (error) {
    console.warn('Failed to load events from localStorage:', error);
  }
  return DEFAULT_MOCK_EVENTS;
};

const saveMockEventsToStorage = (events: MedicalEvent[]): void => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(events));
  } catch (error) {
    console.warn('Failed to save events to localStorage:', error);
  }
};

// Default mock data for testing when backend is not available
const DEFAULT_MOCK_EVENTS: MedicalEvent[] = [
  // John Doe - Recent Events (Last 30 days)
  {
    id: '1',
    patientId: '1', // John Doe
    type: 'SEIZURE',
    title: 'Morning Tonic-Clonic Seizure',
    description: 'Patient experienced a generalized tonic-clonic seizure in bedroom. Started with stiffening, followed by rhythmic jerking movements. Patient was unconscious throughout. Recovery took approximately 10 minutes.',
    seizureType: 'TONIC_CLONIC',
    duration: 180, // 3 minutes
    severity: 'SEVERE',
    status: 'RESOLVED',
    location: 'Bedroom',
    triggers: ['missed morning medication', 'sleep deprivation'],
    medicationGiven: 'Emergency Diazepam',
    dosageGiven: 5.0,
    emergencyContactCalled: true,
    hospitalRequired: false,
    eventTimestamp: '2025-07-25T08:30:00Z',
    reportedTimestamp: '2025-07-25T08:45:00Z',
    resolvedTimestamp: '2025-07-25T08:50:00Z',
    reportedBy: 'Primary Caregiver',
    witnessedBy: ['Jane Doe', 'Nurse Sarah'],
    notes: 'Patient disoriented for 15 minutes post-seizure. No injuries. Medication schedule reviewed.',
    createdAt: '2025-07-25T08:45:00Z',
    updatedAt: '2025-07-25T08:45:00Z'
  },
  {
    id: '10',
    patientId: '1', // John Doe
    type: 'SEIZURE',
    title: 'Evening Focal Seizure',
    description: 'Brief focal seizure with preserved awareness. Patient reported strange taste and tingling in left arm. No loss of consciousness.',
    seizureType: 'FOCAL_AWARE',
    duration: 45,
    severity: 'MILD',
    status: 'RESOLVED',
    location: 'Living room',
    triggers: ['stress', 'late evening medication'],
    emergencyContactCalled: false,
    hospitalRequired: false,
    eventTimestamp: '2025-07-23T19:15:00Z',
    reportedTimestamp: '2025-07-23T19:20:00Z',
    resolvedTimestamp: '2025-07-23T19:16:00Z',
    reportedBy: 'Patient',
    witnessedBy: ['Jane Doe'],
    notes: 'Patient remained fully conscious and communicative. Medication taken late that evening.',
    createdAt: '2025-07-23T19:20:00Z',
    updatedAt: '2025-07-23T19:20:00Z'
  },
  {
    id: '11',
    patientId: '1', // John Doe
    type: 'SEIZURE',
    title: 'Breakthrough Tonic-Clonic Seizure',
    description: 'Unexpected major seizure despite good medication adherence. Started suddenly while watching TV. Duration longer than usual.',
    seizureType: 'TONIC_CLONIC',
    duration: 240, // 4 minutes
    severity: 'SEVERE',
    status: 'RESOLVED',
    location: 'Living room',
    triggers: ['unknown'],
    medicationGiven: 'Emergency Diazepam',
    dosageGiven: 5.0,
    emergencyContactCalled: true,
    hospitalRequired: false,
    eventTimestamp: '2025-07-20T15:45:00Z',
    reportedTimestamp: '2025-07-20T16:00:00Z',
    resolvedTimestamp: '2025-07-20T16:15:00Z',
    reportedBy: 'Jane Doe',
    witnessedBy: ['Jane Doe', 'Neighbor'],
    notes: 'Concerning as medications were taken correctly. Scheduling doctor review.',
    createdAt: '2025-07-20T16:00:00Z',
    updatedAt: '2025-07-20T16:00:00Z'
  },
  {
    id: '12',
    patientId: '1', // John Doe
    type: 'SEIZURE',
    title: 'Absence-like Episode',
    description: 'Brief episode of unresponsiveness while eating breakfast. Lasted about 20 seconds. No convulsions observed.',
    seizureType: 'ABSENCE',
    duration: 20,
    severity: 'MILD',
    status: 'RESOLVED',
    location: 'Kitchen',
    emergencyContactCalled: false,
    hospitalRequired: false,
    eventTimestamp: '2025-07-18T08:15:00Z',
    reportedTimestamp: '2025-07-18T08:20:00Z',
    resolvedTimestamp: '2025-07-18T08:15:20Z',
    reportedBy: 'Jane Doe',
    witnessedBy: ['Jane Doe'],
    notes: 'Unusual for John - typically has tonic-clonic seizures. Will monitor.',
    createdAt: '2025-07-18T08:20:00Z',
    updatedAt: '2025-07-18T08:20:00Z'
  },
  {
    id: '13',
    patientId: '1', // John Doe
    type: 'SEIZURE',
    title: 'Late Night Seizure',
    description: 'Seizure occurred during sleep. Jane woke up to find John in post-ictal state. Likely tonic-clonic based on bed disturbance.',
    seizureType: 'TONIC_CLONIC',
    duration: 120, // 2 minutes (estimated)
    severity: 'MODERATE',
    status: 'RESOLVED',
    location: 'Bedroom',
    triggers: ['missed evening medication'],
    emergencyContactCalled: false,
    hospitalRequired: false,
    eventTimestamp: '2025-07-15T03:30:00Z',
    reportedTimestamp: '2025-07-15T03:45:00Z',
    resolvedTimestamp: '2025-07-15T04:00:00Z',
    reportedBy: 'Jane Doe',
    witnessedBy: ['Jane Doe'],
    notes: 'Found John confused and disoriented. Evening medication had been forgotten.',
    createdAt: '2025-07-15T03:45:00Z',
    updatedAt: '2025-07-15T03:45:00Z'
  },
  {
    id: '14',
    patientId: '1', // John Doe
    type: 'SEIZURE',
    title: 'Morning Focal Seizure',
    description: 'Focal seizure with impaired awareness. John stopped mid-conversation and stared blankly. Responded to voice after 1 minute.',
    seizureType: 'FOCAL_IMPAIRED',
    duration: 75,
    severity: 'MODERATE',
    status: 'RESOLVED',
    location: 'Kitchen',
    emergencyContactCalled: false,
    hospitalRequired: false,
    eventTimestamp: '2025-07-12T09:00:00Z',
    reportedTimestamp: '2025-07-12T09:05:00Z',
    resolvedTimestamp: '2025-07-12T09:01:15Z',
    reportedBy: 'Jane Doe',
    witnessedBy: ['Jane Doe'],
    notes: 'Good medication adherence this week. No obvious triggers identified.',
    createdAt: '2025-07-12T09:05:00Z',
    updatedAt: '2025-07-12T09:05:00Z'
  },
  {
    id: '15',
    patientId: '1', // John Doe
    type: 'SEIZURE',
    title: 'Stress-Related Seizure',
    description: 'Tonic-clonic seizure following stressful family meeting. John had been anxious all morning.',
    seizureType: 'TONIC_CLONIC',
    duration: 150,
    severity: 'SEVERE',
    status: 'RESOLVED',
    location: 'Living room',
    triggers: ['emotional stress', 'anxiety'],
    medicationGiven: 'Emergency Diazepam',
    dosageGiven: 5.0,
    emergencyContactCalled: true,
    hospitalRequired: false,
    eventTimestamp: '2025-07-10T14:30:00Z',
    reportedTimestamp: '2025-07-10T14:45:00Z',
    resolvedTimestamp: '2025-07-10T14:55:00Z',
    reportedBy: 'Jane Doe',
    witnessedBy: ['Jane Doe', 'Brother'],
    notes: 'Clear emotional trigger. Discussing stress management strategies.',
    createdAt: '2025-07-10T14:45:00Z',
    updatedAt: '2025-07-10T14:45:00Z'
  },
  {
    id: '16',
    patientId: '1', // John Doe
    type: 'MEDICATION_REACTION',
    title: 'Mild Drowsiness from New Dosage',
    description: 'Patient reported increased drowsiness after recent medication adjustment. No serious side effects.',
    severity: 'MILD',
    status: 'ONGOING',
    location: 'Home',
    eventTimestamp: '2025-07-08T10:00:00Z',
    reportedTimestamp: '2025-07-08T10:00:00Z',
    reportedBy: 'Patient',
    notes: 'Monitoring new dosage effects. May need adjustment if drowsiness persists.',
    createdAt: '2025-07-08T10:00:00Z',
    updatedAt: '2025-07-08T10:00:00Z'
  },
  {
    id: '2',
    patientId: '3', // Emma Johnson (child)
    type: 'SEIZURE',
    title: 'Absence Seizure During Class',
    description: 'Brief absence seizure during math class. Teacher noticed student staring blankly and not responding to questions. Episode lasted approximately 15 seconds.',
    seizureType: 'ABSENCE',
    duration: 15,
    severity: 'MILD',
    status: 'RESOLVED',
    location: 'School classroom',
    triggers: ['stress from test'],
    emergencyContactCalled: true,
    hospitalRequired: false,
    eventTimestamp: '2024-07-24T14:20:00Z',
    reportedTimestamp: '2024-07-24T14:25:00Z',
    resolvedTimestamp: '2024-07-24T14:21:00Z',
    reportedBy: 'School Nurse',
    witnessedBy: ['Ms. Thompson (Teacher)', 'School nurse'],
    notes: 'Student returned to normal activities immediately. Parents notified.',
    createdAt: '2024-07-24T14:25:00Z',
    updatedAt: '2024-07-24T14:25:00Z'
  },
  {
    id: '3',
    patientId: '2', // Sarah Smith
    type: 'MEDICATION_REACTION',
    title: 'Mild Allergic Reaction to New Medication',
    description: 'Patient developed mild skin rash and itching after starting new antibiotic. No breathing difficulties or swelling observed.',
    severity: 'MODERATE',
    status: 'ONGOING',
    location: 'Home',
    medicationGiven: 'Antihistamine',
    dosageGiven: 25,
    emergencyContactCalled: false,
    hospitalRequired: false,
    eventTimestamp: '2024-07-23T16:00:00Z',
    reportedTimestamp: '2024-07-23T17:30:00Z',
    reportedBy: 'Patient',
    notes: 'Medication discontinued. Rash improving. Doctor consulted.',
    createdAt: '2024-07-23T17:30:00Z',
    updatedAt: '2024-07-23T17:30:00Z'
  },
  {
    id: '4',
    patientId: '1', // John Doe
    type: 'SEIZURE',
    title: 'Focal Aware Seizure',
    description: 'Patient experienced focal seizure with preserved awareness. Reported tingling sensation in left arm and unusual taste. No loss of consciousness.',
    seizureType: 'FOCAL_AWARE',
    duration: 45,
    severity: 'MILD',
    status: 'RESOLVED',
    location: 'Living room',
    emergencyContactCalled: false,
    hospitalRequired: false,
    eventTimestamp: '2024-07-22T19:15:00Z',
    reportedTimestamp: '2024-07-22T19:20:00Z',
    resolvedTimestamp: '2024-07-22T19:16:00Z',
    reportedBy: 'Patient',
    witnessedBy: ['Jane Doe'],
    notes: 'Patient able to communicate throughout. No post-ictal confusion.',
    createdAt: '2024-07-22T19:20:00Z',
    updatedAt: '2024-07-22T19:20:00Z'
  },
  {
    id: '5',
    patientId: '4', // George Wilson
    type: 'EMERGENCY',
    title: 'Blood Pressure Emergency',
    description: 'Patient experienced severe headache and dizziness. Blood pressure measured at 180/110. Ambulance called.',
    severity: 'CRITICAL',
    status: 'RESOLVED',
    location: 'Home',
    emergencyContactCalled: true,
    hospitalRequired: true,
    eventTimestamp: '2024-07-21T11:00:00Z',
    reportedTimestamp: '2024-07-21T11:05:00Z',
    resolvedTimestamp: '2024-07-21T14:30:00Z',
    reportedBy: 'Mary Wilson',
    witnessedBy: ['Mary Wilson', 'Paramedic'],
    notes: 'Patient hospitalized for 6 hours. Medication adjusted. Blood pressure normalized.',
    createdAt: '2024-07-21T11:05:00Z',
    updatedAt: '2024-07-21T11:05:00Z'
  }
];

// Persist mock data mode across service instances
const MOCK_MODE_KEY = 'medical-events-mock-mode';

const isMockModeEnabled = (): boolean => {
  return localStorage.getItem(MOCK_MODE_KEY) === 'true';
};

const setMockModeEnabled = (enabled: boolean): void => {
  localStorage.setItem(MOCK_MODE_KEY, enabled.toString());
};

export class MedicalEventService {
  private readonly baseUrl = '/api/medical-events';
  private useMockData = isMockModeEnabled(); // Restore from localStorage
  private mockEvents: MedicalEvent[] = loadMockEventsFromStorage();

  // Get all medical events with pagination and search
  async getMedicalEvents(params: MedicalEventSearchParams = {}): Promise<PaginatedResponse<MedicalEvent>> {
    try {
      if (this.useMockData) {
        return this.getMockMedicalEvents(params);
      }

      // Backend expects POST to /search endpoint with request body
      const searchRequest = {
        patientId: params.patientId || null,
        type: params.type || null,
        severity: params.severity || null,
        status: params.status || null,
        dateFrom: params.dateFrom || null,
        dateTo: params.dateTo || null,
        searchTerm: params.searchTerm || null,
        page: params.page || 0,
        size: params.size || 20,
        sortBy: 'eventTime',
        sortDirection: 'DESC'
      };

      return apiClient.post<PaginatedResponse<MedicalEvent>>(`${this.baseUrl}/search`, searchRequest);
    } catch (error) {
      console.warn('Medical events API failed, switching to mock data:', error);
      this.useMockData = true;
      setMockModeEnabled(true);
      return this.getMockMedicalEvents(params);
    }
  }

  // Get single medical event by ID
  async getMedicalEvent(id: string): Promise<MedicalEvent> {
    try {
      if (this.useMockData) {
        const event = this.mockEvents.find(e => e.id === id);
        if (!event) throw new Error('Medical event not found');
        return event;
      }
      return apiClient.get<MedicalEvent>(`${this.baseUrl}/${id}`);
    } catch (error) {
      console.warn('Medical event API failed, switching to mock data:', error);
      this.useMockData = true;
      setMockModeEnabled(true);
      const event = this.mockEvents.find(e => e.id === id);
      if (!event) throw new Error('Medical event not found');
      return event;
    }
  }

  // Create new medical event
  async createMedicalEvent(event: MedicalEventCreateRequest): Promise<MedicalEvent> {
    try {
      if (this.useMockData) {
        // Generate unique ID to avoid collisions
        const existingIds = this.mockEvents.map(e => parseInt(e.id, 10)).filter(id => !isNaN(id));
        const maxId = existingIds.length > 0 ? Math.max(...existingIds) : 0;
        const newId = (maxId + 1).toString();
        
        const newEvent: MedicalEvent = {
          id: newId,
          ...event,
          status: 'ACTIVE',
          reportedTimestamp: new Date().toISOString(),
          reportedBy: 'Current User',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
        this.mockEvents.push(newEvent);
        saveMockEventsToStorage(this.mockEvents);
        return newEvent;
      }
      
      // Transform frontend request to backend format
      const backendRequest = this.transformToBackendRequest(event);
      return apiClient.post<MedicalEvent>(this.baseUrl, backendRequest);
    } catch (error) {
      console.warn('Create medical event API failed, switching to mock data:', error);
      this.useMockData = true;
      setMockModeEnabled(true);
      // Generate unique ID to avoid collisions
      const existingIds = this.mockEvents.map(e => parseInt(e.id, 10)).filter(id => !isNaN(id));
      const maxId = existingIds.length > 0 ? Math.max(...existingIds) : 0;
      const newId = (maxId + 1).toString();

      const newEvent: MedicalEvent = {
        id: newId,
        ...event,
        status: 'ACTIVE',
        reportedTimestamp: new Date().toISOString(),
        reportedBy: 'Current User',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      this.mockEvents.push(newEvent);
      saveMockEventsToStorage(this.mockEvents);
      return newEvent;
    }
  }

  // Update existing medical event
  async updateMedicalEvent(id: string, event: MedicalEventUpdateRequest): Promise<MedicalEvent> {
    try {
      if (this.useMockData) {
        const index = this.mockEvents.findIndex(e => e.id === id);
        if (index === -1) throw new Error('Medical event not found');
        this.mockEvents[index] = {
          ...this.mockEvents[index],
          ...event,
          updatedAt: new Date().toISOString()
        };
        saveMockEventsToStorage(this.mockEvents);
        return this.mockEvents[index];
      }
      return apiClient.put<MedicalEvent>(`${this.baseUrl}/${id}`, event);
    } catch (error) {
      console.warn('Update medical event API failed, switching to mock data:', error);
      this.useMockData = true;
      setMockModeEnabled(true);
      const index = this.mockEvents.findIndex(e => e.id === id);
      if (index === -1) throw new Error('Medical event not found');
      this.mockEvents[index] = {
        ...this.mockEvents[index],
        ...event,
        updatedAt: new Date().toISOString()
      };
      saveMockEventsToStorage(this.mockEvents);
      return this.mockEvents[index];
    }
  }

  // Delete medical event
  async deleteMedicalEvent(id: string): Promise<void> {
    try {
      if (this.useMockData) {
        const index = this.mockEvents.findIndex(e => e.id === id);
        if (index === -1) throw new Error('Medical event not found');
        this.mockEvents.splice(index, 1);
        saveMockEventsToStorage(this.mockEvents);
        return;
      }
      return apiClient.delete<void>(`${this.baseUrl}/${id}`);
    } catch (error) {
      console.warn('Delete medical event API failed, switching to mock data:', error);
      this.useMockData = true;
      setMockModeEnabled(true);
      const index = this.mockEvents.findIndex(e => e.id === id);
      if (index === -1) throw new Error('Medical event not found');
      this.mockEvents.splice(index, 1);
      saveMockEventsToStorage(this.mockEvents);
    }
  }

  // Transform frontend request to backend format
  private transformToBackendRequest(frontendRequest: MedicalEventCreateRequest): any {
    // Map frontend EventType to backend MedicalEventCategory
    const mapEventTypeToCategory = (type: EventType): string => {
      switch (type) {
        case 'SEIZURE':
          return 'SYMPTOM'; // Seizures are symptoms
        case 'MEDICATION_REACTION':
          return 'ADVERSE_REACTION';
        case 'EMERGENCY':
          return 'EMERGENCY';
        case 'APPOINTMENT':
          return 'APPOINTMENT';
        case 'MEDICATION':
          return 'MEDICATION';
        default:
          return 'OBSERVATION'; // Default fallback
      }
    };

    // Convert eventTimestamp string to LocalDateTime format expected by backend
    const convertToLocalDateTime = (timestampString: string): string => {
      // Remove 'Z' and convert ISO string to backend LocalDateTime format
      return timestampString.replace('Z', '');
    };

    // Create backend-compatible request
    const backendRequest = {
      patientId: frontendRequest.patientId,
      eventTime: convertToLocalDateTime(frontendRequest.eventTimestamp),
      title: frontendRequest.title,
      description: frontendRequest.description || '',
      severity: frontendRequest.severity, // Should match: MILD, MODERATE, SEVERE, CRITICAL
      category: mapEventTypeToCategory(frontendRequest.type),
      medicationId: frontendRequest.medicationGiven ? undefined : undefined // We don't have medication ID mapping yet
    };

    return backendRequest;
  }

  // Transform form data to API format
  transformFormToApiRequest(formData: MedicalEventFormData): MedicalEventCreateRequest {
    // Combine date and time
    const eventTimestamp = `${formData.eventDate}T${formData.eventTime}:00Z`;
    
    return {
      patientId: formData.patientId,
      type: formData.type,
      title: formData.title,
      description: formData.description,
      seizureType: formData.seizureType,
      duration: formData.duration,
      severity: formData.severity,
      location: formData.location,
      triggers: formData.triggers ? (
        Array.isArray(formData.triggers) 
          ? formData.triggers 
          : formData.triggers.split(',').map(t => t.trim()).filter(Boolean)
      ) : undefined,
      medicationGiven: formData.medicationGiven,
      dosageGiven: formData.dosageGiven,
      emergencyContactCalled: formData.emergencyContactCalled,
      hospitalRequired: formData.hospitalRequired,
      eventTimestamp,
      witnessedBy: formData.witnessedBy ? (
        Array.isArray(formData.witnessedBy)
          ? formData.witnessedBy
          : formData.witnessedBy.split(',').map(w => w.trim()).filter(Boolean)
      ) : undefined,
      notes: formData.notes
    };
  }

  // Mock data helper methods
  private getMockMedicalEvents(params: MedicalEventSearchParams = {}): PaginatedResponse<MedicalEvent> {
    let filteredEvents = [...this.mockEvents];
    
    // Apply patient filter
    if (params.patientId) {
      filteredEvents = filteredEvents.filter(event => event.patientId === params.patientId);
    }
    
    // Apply type filter
    if (params.type) {
      filteredEvents = filteredEvents.filter(event => event.type === params.type);
    }
    
    // Apply severity filter
    if (params.severity) {
      filteredEvents = filteredEvents.filter(event => event.severity === params.severity);
    }
    
    // Apply status filter
    if (params.status) {
      filteredEvents = filteredEvents.filter(event => event.status === params.status);
    }
    
    // Apply search term filter
    if (params.searchTerm) {
      const searchLower = params.searchTerm.toLowerCase();
      filteredEvents = filteredEvents.filter(event =>
        event.title.toLowerCase().includes(searchLower) ||
        event.description.toLowerCase().includes(searchLower) ||
        (event.notes && event.notes.toLowerCase().includes(searchLower))
      );
    }
    
    // Apply date range filter
    if (params.dateFrom || params.dateTo) {
      filteredEvents = filteredEvents.filter(event => {
        const eventDate = new Date(event.eventTimestamp);
        
        if (params.dateFrom) {
          const fromDate = new Date(params.dateFrom);
          fromDate.setHours(0, 0, 0, 0); // Start of day
          if (eventDate < fromDate) return false;
        }
        
        if (params.dateTo) {
          const toDate = new Date(params.dateTo);
          toDate.setHours(23, 59, 59, 999); // End of day
          if (eventDate > toDate) return false;
        }
        
        return true;
      });
    }
    
    // Sort by event timestamp (newest first)
    filteredEvents.sort((a, b) => 
      new Date(b.eventTimestamp).getTime() - new Date(a.eventTimestamp).getTime()
    );
    
    // Simple pagination
    const page = params.page || 0;
    const size = params.size || 20;
    const startIndex = page * size;
    const endIndex = startIndex + size;
    const paginatedEvents = filteredEvents.slice(startIndex, endIndex);
    
    return {
      content: paginatedEvents,
      totalElements: filteredEvents.length,
      totalPages: Math.ceil(filteredEvents.length / size),
      page,
      size,
      first: page === 0,
      last: endIndex >= filteredEvents.length
    };
  }

  // Format event duration for display
  formatDuration(durationInSeconds?: number): string {
    if (!durationInSeconds) return 'Unknown';
    
    if (durationInSeconds < 60) {
      return `${durationInSeconds}s`;
    } else if (durationInSeconds < 3600) {
      const minutes = Math.floor(durationInSeconds / 60);
      const seconds = durationInSeconds % 60;
      return seconds > 0 ? `${minutes}m ${seconds}s` : `${minutes}m`;
    } else {
      const hours = Math.floor(durationInSeconds / 3600);
      const minutes = Math.floor((durationInSeconds % 3600) / 60);
      return minutes > 0 ? `${hours}h ${minutes}m` : `${hours}h`;
    }
  }

  // Calculate time since event
  formatTimeSince(timestamp: string): string {
    const eventDate = new Date(timestamp);
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
  getEventStatistics(events: MedicalEvent[]) {
    const now = new Date();
    const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    const oneMonthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);

    const eventsThisWeek = events.filter(e => new Date(e.eventTimestamp) >= oneWeekAgo).length;
    const eventsThisMonth = events.filter(e => new Date(e.eventTimestamp) >= oneMonthAgo).length;

    const seizureEvents = events.filter(e => e.type === 'SEIZURE');
    const seizureDurations = seizureEvents
      .filter(e => e.duration)
      .map(e => e.duration!);

    return {
      totalEvents: events.length,
      eventsThisWeek,
      eventsThisMonth,
      averagePerWeek: eventsThisMonth > 0 ? Math.round((eventsThisMonth / 4) * 10) / 10 : 0,
      totalSeizures: seizureEvents.length,
      averageSeizureDuration: seizureDurations.length > 0 
        ? Math.round((seizureDurations.reduce((a, b) => a + b, 0) / seizureDurations.length) * 10) / 10
        : undefined,
      longestSeizure: seizureDurations.length > 0 ? Math.max(...seizureDurations) : undefined,
      shortestSeizure: seizureDurations.length > 0 ? Math.min(...seizureDurations) : undefined
    };
  }
}

// Create and export singleton instance
export const medicalEventService = new MedicalEventService();
export default medicalEventService;