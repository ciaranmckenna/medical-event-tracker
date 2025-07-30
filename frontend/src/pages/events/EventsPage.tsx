import { useState, useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import { medicalEventService } from '../../services/api/medicalEventService';
import { patientService } from '../../services/api/patientService';
import { MedicalEventForm } from '../../components/forms/MedicalEventForm';
import { MedicalEventCard } from '../../components/medical/MedicalEventCard';
import type { MedicalEvent, Patient, PaginatedResponse } from '../../types/api';
import type { MedicalEventFormData, MedicalEventSearchParams } from '../../types/medical';
import { 
  EVENT_TYPE_DISPLAY, 
  SEVERITY_DISPLAY 
} from '../../services/validation/medicalEventValidation';

export const EventsPage: React.FC = () => {
  const { user, hasRole } = useAuth();
  const navigate = useNavigate();
  
  // State management
  const [events, setEvents] = useState<MedicalEvent[]>([]);
  const [patients, setPatients] = useState<Patient[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [showForm, setShowForm] = useState(false);
  const [editingEvent, setEditingEvent] = useState<MedicalEvent | undefined>();
  const [selectedPatientId, setSelectedPatientId] = useState<string>('');
  const [emergencyMode, setEmergencyMode] = useState(false);
  
  // Search and filtering
  const [searchParams, setSearchParams] = useState<MedicalEventSearchParams>({
    page: 0,
    size: 20
  });
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    loadInitialData();
  }, [user, navigate]);

  useEffect(() => {
    loadEvents();
  }, [searchParams]);

  const loadInitialData = async () => {
    try {
      const [eventsResponse, patientsResponse] = await Promise.all([
        medicalEventService.getMedicalEvents(searchParams),
        patientService.getPatients()
      ]);
      
      setEvents(eventsResponse.content);
      setTotalPages(eventsResponse.totalPages);
      setTotalElements(eventsResponse.totalElements);
      setPatients(patientsResponse.content);
    } catch (error) {
      console.error('Failed to load data:', error);
      setError('Failed to load medical events');
    } finally {
      setLoading(false);
    }
  };

  const loadEvents = async () => {
    try {
      const response = await medicalEventService.getMedicalEvents(searchParams);
      setEvents(response.content);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (error) {
      console.error('Failed to load events:', error);
      setError('Failed to load medical events');
    }
  };

  const handleCreateEvent = async (formData: MedicalEventFormData) => {
    try {
      const apiRequest = medicalEventService.transformFormToApiRequest(formData);
      const newEvent = await medicalEventService.createMedicalEvent(apiRequest);
      setEvents(prev => [newEvent, ...prev]);
      setShowForm(false);
      setEmergencyMode(false);
    } catch (error) {
      console.error('Failed to create event:', error);
      throw error;
    }
  };

  const handleUpdateEvent = async (formData: MedicalEventFormData) => {
    if (!editingEvent) return;
    
    try {
      const apiRequest = medicalEventService.transformFormToApiRequest(formData);
      const updatedEvent = await medicalEventService.updateMedicalEvent(
        editingEvent.id, 
        { ...apiRequest, id: editingEvent.id }
      );
      
      setEvents(prev => prev.map(event => 
        event.id === editingEvent.id ? updatedEvent : event
      ));
      setEditingEvent(undefined);
      setShowForm(false);
    } catch (error) {
      console.error('Failed to update event:', error);
      throw error;
    }
  };

  const handleDeleteEvent = async (eventId: string) => {
    try {
      await medicalEventService.deleteMedicalEvent(eventId);
      setEvents(prev => prev.filter(event => event.id !== eventId));
    } catch (error) {
      console.error('Failed to delete event:', error);
      throw error;
    }
  };

  const handleEditEvent = (event: MedicalEvent) => {
    setEditingEvent(event);
    setShowForm(true);
    setEmergencyMode(false);
  };

  const handleCancelForm = () => {
    setShowForm(false);
    setEditingEvent(undefined);
    setEmergencyMode(false);
  };

  const handleQuickSeizureLog = () => {
    setEmergencyMode(true);
    setShowForm(true);
    setEditingEvent(undefined);
  };

  const handleSearchChange = (field: keyof MedicalEventSearchParams, value: any) => {
    setSearchParams(prev => ({
      ...prev,
      [field]: value,
      page: 0 // Reset to first page when searching
    }));
  };

  const handlePageChange = (newPage: number) => {
    setSearchParams(prev => ({ ...prev, page: newPage }));
  };

  const getPatientName = (patientId: string) => {
    const patient = patients.find(p => p.id === patientId);
    return patient ? patientService.formatPatientName(patient) : 'Unknown Patient';
  };

  // Styles
  const containerStyle = {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '20px'
  };

  const headerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '30px',
    flexWrap: 'wrap' as const,
    gap: '15px'
  };

  const titleStyle = {
    fontSize: '28px',
    fontWeight: '700',
    color: '#1f2937',
    margin: '0'
  };

  const buttonGroupStyle = {
    display: 'flex',
    gap: '12px',
    flexWrap: 'wrap' as const
  };

  const buttonStyle = (variant: 'primary' | 'emergency' | 'secondary' = 'primary') => {
    const variants = {
      primary: { backgroundColor: '#2563eb', color: 'white' },
      emergency: { backgroundColor: '#dc2626', color: 'white' },
      secondary: { backgroundColor: 'white', color: '#374151', border: '1px solid #d1d5db' }
    };

    return {
      ...variants[variant],
      padding: '12px 20px',
      borderRadius: '8px',
      border: variant === 'secondary' ? '1px solid #d1d5db' : 'none',
      fontSize: '16px',
      fontWeight: '600',
      cursor: 'pointer',
      transition: 'all 0.2s ease',
      textDecoration: 'none',
      display: 'inline-flex',
      alignItems: 'center',
      gap: '8px'
    };
  };

  const filtersStyle = {
    backgroundColor: 'white',
    padding: '20px',
    borderRadius: '8px',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
    marginBottom: '20px',
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    gap: '15px'
  };

  const inputStyle = {
    padding: '8px 12px',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    fontSize: '14px'
  };

  const statsStyle = {
    backgroundColor: '#f3f4f6',
    padding: '15px',
    borderRadius: '8px',
    marginBottom: '20px',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    flexWrap: 'wrap' as const,
    gap: '10px'
  };

  const paginationStyle = {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    gap: '10px',
    marginTop: '30px'
  };

  const pageButtonStyle = (active: boolean = false) => ({
    padding: '8px 12px',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    backgroundColor: active ? '#2563eb' : 'white',
    color: active ? 'white' : '#374151',
    cursor: 'pointer',
    fontSize: '14px'
  });

  const emergencyBannerStyle = {
    backgroundColor: '#fee2e2',
    border: '2px solid #dc2626',
    borderRadius: '8px',
    padding: '15px',
    marginBottom: '20px',
    textAlign: 'center' as const
  };

  if (loading) {
    return (
      <div style={containerStyle}>
        <div style={{ textAlign: 'center', padding: '50px' }}>
          Loading medical events...
        </div>
      </div>
    );
  }

  if (showForm) {
    return (
      <div style={containerStyle}>
        {emergencyMode && (
          <div style={emergencyBannerStyle}>
            <strong>🚨 EMERGENCY SEIZURE LOGGING MODE</strong>
            <p style={{ margin: '5px 0 0 0' }}>
              Quick documentation for active medical emergency
            </p>
          </div>
        )}
        
        <MedicalEventForm
          event={editingEvent}
          selectedPatientId={selectedPatientId}
          onSubmit={editingEvent ? handleUpdateEvent : handleCreateEvent}
          onCancel={handleCancelForm}
          emergencyMode={emergencyMode}
        />
      </div>
    );
  }

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <div>
          <h1 style={titleStyle}>📋 Medical Events</h1>
          <p style={{ color: '#6b7280', margin: '5px 0 0 0' }}>
            Track and manage medical incidents, seizures, and medication responses
          </p>
        </div>

        {hasRole('PRIMARY_USER') && (
          <div style={buttonGroupStyle}>
            <button
              onClick={handleQuickSeizureLog}
              style={buttonStyle('emergency')}
            >
              🚨 Emergency Log
            </button>
            <button
              onClick={() => setShowForm(true)}
              style={buttonStyle('primary')}
            >
              ➕ Log Event
            </button>
          </div>
        )}
      </div>

      {error && (
        <div style={{
          backgroundColor: '#fee2e2',
          border: '1px solid #fca5a5',
          borderRadius: '6px',
          padding: '12px',
          marginBottom: '20px',
          color: '#dc2626'
        }}>
          {error}
        </div>
      )}

      {/* Search and Filters */}
      <div style={filtersStyle}>
        <div>
          <label style={{ fontSize: '14px', fontWeight: '600', marginBottom: '5px', display: 'block' }}>
            Patient
          </label>
          <select
            value={searchParams.patientId || ''}
            onChange={(e) => handleSearchChange('patientId', e.target.value || undefined)}
            style={inputStyle}
          >
            <option value="">All Patients</option>
            {patients.map(patient => (
              <option key={patient.id} value={patient.id}>
                {patientService.formatPatientName(patient)}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label style={{ fontSize: '14px', fontWeight: '600', marginBottom: '5px', display: 'block' }}>
            Event Type
          </label>
          <select
            value={searchParams.type || ''}
            onChange={(e) => handleSearchChange('type', e.target.value || undefined)}
            style={inputStyle}
          >
            <option value="">All Types</option>
            {Object.entries(EVENT_TYPE_DISPLAY).map(([key, display]) => (
              <option key={key} value={key}>
                {display.icon} {display.label}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label style={{ fontSize: '14px', fontWeight: '600', marginBottom: '5px', display: 'block' }}>
            Severity
          </label>
          <select
            value={searchParams.severity || ''}
            onChange={(e) => handleSearchChange('severity', e.target.value || undefined)}
            style={inputStyle}
          >
            <option value="">All Severities</option>
            {Object.entries(SEVERITY_DISPLAY).map(([key, display]) => (
              <option key={key} value={key}>
                {display.label}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label style={{ fontSize: '14px', fontWeight: '600', marginBottom: '5px', display: 'block' }}>
            Search
          </label>
          <input
            type="text"
            placeholder="Search titles, descriptions..."
            value={searchParams.searchTerm || ''}
            onChange={(e) => handleSearchChange('searchTerm', e.target.value || undefined)}
            style={inputStyle}
          />
        </div>

        <div>
          <label style={{ fontSize: '14px', fontWeight: '600', marginBottom: '5px', display: 'block' }}>
            Date From
          </label>
          <input
            type="date"
            value={searchParams.dateFrom || ''}
            onChange={(e) => handleSearchChange('dateFrom', e.target.value || undefined)}
            style={inputStyle}
          />
        </div>

        <div>
          <label style={{ fontSize: '14px', fontWeight: '600', marginBottom: '5px', display: 'block' }}>
            Date To
          </label>
          <input
            type="date"
            value={searchParams.dateTo || ''}
            onChange={(e) => handleSearchChange('dateTo', e.target.value || undefined)}
            style={inputStyle}
          />
        </div>
      </div>

      {/* Statistics */}
      <div style={statsStyle}>
        <div>
          <strong>{totalElements}</strong> total events
        </div>
        <div>
          Page {searchParams.page! + 1} of {totalPages || 1}
        </div>
        <div>
          {events.filter(e => e.type === 'SEIZURE').length} seizures in current view
        </div>
      </div>

      {/* Events List */}
      <div>
        {events.length === 0 ? (
          <div style={{
            textAlign: 'center',
            padding: '60px 20px',
            backgroundColor: '#f9fafb',
            borderRadius: '8px',
            color: '#6b7280'
          }}>
            <div style={{ fontSize: '48px', marginBottom: '16px' }}>📋</div>
            <h3 style={{ margin: '0 0 8px 0' }}>No medical events found</h3>
            <p style={{ margin: '0' }}>
              {hasRole('PRIMARY_USER') 
                ? 'Start by logging your first medical event or seizure.'
                : 'No events have been logged yet.'
              }
            </p>
            {hasRole('PRIMARY_USER') && (
              <button
                onClick={() => setShowForm(true)}
                style={{ ...buttonStyle('primary'), marginTop: '20px' }}
              >
                Log First Event
              </button>
            )}
          </div>
        ) : (
          events.map(event => (
            <MedicalEventCard
              key={event.id}
              event={event}
              patientName={getPatientName(event.patientId)}
              onEdit={handleEditEvent}
              onDelete={handleDeleteEvent}
              showPatient={!searchParams.patientId}
            />
          ))
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div style={paginationStyle}>
          <button
            onClick={() => handlePageChange(Math.max(0, searchParams.page! - 1))}
            disabled={searchParams.page === 0}
            style={pageButtonStyle()}
          >
            Previous
          </button>
          
          {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
            const pageNum = Math.max(0, Math.min(searchParams.page! - 2, totalPages - 5)) + i;
            return (
              <button
                key={pageNum}
                onClick={() => handlePageChange(pageNum)}
                style={pageButtonStyle(pageNum === searchParams.page)}
              >
                {pageNum + 1}
              </button>
            );
          })}
          
          <button
            onClick={() => handlePageChange(Math.min(totalPages - 1, searchParams.page! + 1))}
            disabled={searchParams.page === totalPages - 1}
            style={pageButtonStyle()}
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
};