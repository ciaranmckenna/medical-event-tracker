import { useState, useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import { medicalEventService } from '../../services/api/medicalEventService';
import { medicationService } from '../../services/api/medicationService';
import { dosageService } from '../../services/api/dosageService';
import { patientService } from '../../services/api/patientService';
import { CorrelationChart } from '../../components/charts/CorrelationChart';
import type { MedicalEvent, Patient, Medication } from '../../types/api';
import type { DosageRecord } from '../../services/api/dosageService';
import { mockAnalyticsData } from '../../services/mockData/analyticsData';

export const AnalyticsPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  
  // State management
  const [patients, setPatients] = useState<Patient[]>([]);
  const [medications, setMedications] = useState<Medication[]>([]);
  const [events, setEvents] = useState<MedicalEvent[]>([]);
  const [dosageRecords, setDosageRecords] = useState<DosageRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');
  
  // Filter controls
  const [selectedPatientId, setSelectedPatientId] = useState<string>('');
  const [timeRange, setTimeRange] = useState<{start: Date; end: Date}>(() => {
    const end = new Date();
    const start = new Date(end.getTime() - 30 * 24 * 60 * 60 * 1000); // 30 days ago
    return { start, end };
  });
  
  // Development toggle for mock data
  const [useMockData, setUseMockData] = useState<boolean>(false);

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    loadInitialData();
  }, [user, navigate, useMockData]);

  useEffect(() => {
    if (selectedPatientId) {
      loadAnalyticsData();
    }
  }, [selectedPatientId, timeRange]);

  const loadInitialData = async () => {
    try {
      if (useMockData) {
        // Use mock data for demonstration
        setPatients(mockAnalyticsData.patients);
        setMedications(mockAnalyticsData.medications);
        
        // Auto-select first patient if available
        if (mockAnalyticsData.patients.length > 0) {
          setSelectedPatientId(mockAnalyticsData.patients[0].id);
        }
      } else {
        // Use real API data
        const [patientsResponse, medicationsResponse] = await Promise.all([
          patientService.getPatients(),
          medicationService.getMedications()
        ]);
        
        setPatients(patientsResponse.content);
        setMedications(medicationsResponse.content);
        
        // Auto-select first patient if available
        if (patientsResponse.content.length > 0) {
          setSelectedPatientId(patientsResponse.content[0].id);
        }
      }
    } catch (error) {
      console.error('Failed to load initial data:', error);
      setError('Failed to load patients and medications');
    } finally {
      setLoading(false);
    }
  };

  const loadAnalyticsData = async () => {
    if (!selectedPatientId) return;
    
    try {
      if (useMockData) {
        // Filter mock data for selected patient and time range
        const patientEvents = mockAnalyticsData.medicalEvents.filter(event => {
          const eventDate = new Date(event.eventTime);
          return event.patientId === selectedPatientId && 
                 eventDate >= timeRange.start && 
                 eventDate <= timeRange.end;
        });
        
        const patientDosages = mockAnalyticsData.dosageRecords.filter(record => {
          const recordDate = new Date(record.scheduledTime);
          return record.patientId === selectedPatientId && 
                 recordDate >= timeRange.start && 
                 recordDate <= timeRange.end;
        });
        
        setEvents(patientEvents);
        setDosageRecords(patientDosages);
      } else {
        // Use real API data
        const [eventsResponse, dosageResponse] = await Promise.all([
          medicalEventService.getMedicalEvents({
            patientId: selectedPatientId,
            dateFrom: timeRange.start.toISOString().split('T')[0],
            dateTo: timeRange.end.toISOString().split('T')[0],
            size: 1000
          }),
          dosageService.getDosageRecords({
            patientId: selectedPatientId,
            dateFrom: timeRange.start.toISOString().split('T')[0],
            dateTo: timeRange.end.toISOString().split('T')[0],
            size: 1000
          })
        ]);
        
        setEvents(eventsResponse.content);
        setDosageRecords(dosageResponse.content);
      }
    } catch (error) {
      console.error('Failed to load analytics data:', error);
      setError('Failed to load analytics data');
    }
  };

  const getPatientMedications = (patientId: string) => {
    return medications.filter(med => med.patientId === patientId);
  };

  const getPatientName = (patientId: string) => {
    const patient = patients.find(p => p.id === patientId);
    return patient ? patientService.formatPatientName(patient) : 'Unknown Patient';
  };

  // Calculate key statistics
  const getKeyStatistics = () => {
    const seizureEvents = events.filter(event => event.category === 'SYMPTOM' && event.title.toLowerCase().includes('seizure'));
    const totalDays = Math.ceil((timeRange.end.getTime() - timeRange.start.getTime()) / (1000 * 60 * 60 * 24));
    
    const patientMeds = getPatientMedications(selectedPatientId);
    const expectedDoses = dosageRecords.length;
    const administeredDoses = dosageRecords.filter(record => record.administered).length;
    const adherenceRate = expectedDoses > 0 ? Math.round((administeredDoses / expectedDoses) * 100) : 0;
    
    // Calculate seizure frequency trends
    const firstHalf = events.filter(event => {
      const eventDate = new Date(event.eventTime);
      const midPoint = new Date(timeRange.start.getTime() + (timeRange.end.getTime() - timeRange.start.getTime()) / 2);
      return eventDate >= timeRange.start && eventDate <= midPoint && event.category === 'SYMPTOM' && event.title.toLowerCase().includes('seizure');
    });
    
    const secondHalf = events.filter(event => {
      const eventDate = new Date(event.eventTime);
      const midPoint = new Date(timeRange.start.getTime() + (timeRange.end.getTime() - timeRange.start.getTime()) / 2);
      return eventDate > midPoint && eventDate <= timeRange.end && event.category === 'SYMPTOM' && event.title.toLowerCase().includes('seizure');
    });
    
    const firstHalfRate = firstHalf.length / (totalDays / 2);
    const secondHalfRate = secondHalf.length / (totalDays / 2);
    const trendDirection = secondHalfRate > firstHalfRate ? 'increasing' : 
                          secondHalfRate < firstHalfRate ? 'decreasing' : 'stable';
    
    return {
      totalSeizures: seizureEvents.length,
      seizureFrequency: totalDays > 0 ? Math.round((seizureEvents.length / totalDays) * 10) / 10 : 0,
      adherenceRate,
      totalEvents: events.length,
      activeMedications: patientMeds.filter(med => med.active === true).length,
      trendDirection,
      averageSeverity: seizureEvents.length > 0 ? 
        seizureEvents.reduce((sum, event) => {
          const severityMap = { 'MILD': 1, 'MODERATE': 2, 'SEVERE': 3, 'CRITICAL': 4 };
          return sum + severityMap[event.severity];
        }, 0) / seizureEvents.length : 0
    };
  };

  const handleTimeRangeChange = (days: number) => {
    const end = new Date();
    const start = new Date(end.getTime() - days * 24 * 60 * 60 * 1000);
    setTimeRange({ start, end });
  };

  // Styles
  const containerStyle = {
    maxWidth: '1400px',
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

  const controlsStyle = {
    backgroundColor: 'white',
    padding: '20px',
    borderRadius: '8px',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
    marginBottom: '20px',
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    gap: '15px'
  };

  const selectStyle = {
    padding: '8px 12px',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    fontSize: '14px',
    width: '100%'
  };

  const buttonGroupStyle = {
    display: 'flex',
    gap: '8px',
    flexWrap: 'wrap' as const
  };

  const timeButtonStyle = (days: number) => {
    const isActive = Math.abs(timeRange.end.getTime() - timeRange.start.getTime()) === days * 24 * 60 * 60 * 1000;
    return {
      padding: '6px 12px',
      border: isActive ? 'none' : '1px solid #d1d5db',
      borderRadius: '6px',
      backgroundColor: isActive ? '#2563eb' : 'white',
      color: isActive ? 'white' : '#374151',
      cursor: 'pointer',
      fontSize: '14px',
      fontWeight: isActive ? '600' : '400'
    };
  };

  const statsGridStyle = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    gap: '16px',
    marginBottom: '30px'
  };

  const statCardStyle = {
    backgroundColor: 'white',
    padding: '20px',
    borderRadius: '8px',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
    textAlign: 'center' as const
  };

  const statNumberStyle = (color: string) => ({
    fontSize: '32px',
    fontWeight: '700',
    color,
    margin: '0 0 8px 0'
  });

  const statLabelStyle = {
    fontSize: '14px',
    color: '#6b7280',
    margin: '0',
    fontWeight: '500'
  };

  const alertStyle = {
    backgroundColor: '#fee2e2',
    border: '1px solid #fca5a5',
    borderRadius: '8px',
    padding: '15px',
    marginBottom: '20px',
    color: '#dc2626'
  };

  if (loading) {
    return (
      <div style={containerStyle}>
        <div style={{ textAlign: 'center', padding: '50px' }}>
          Loading analytics data...
        </div>
      </div>
    );
  }

  if (!selectedPatientId && patients.length === 0) {
    return (
      <div style={containerStyle}>
        <div style={{
          textAlign: 'center',
          padding: '60px 20px',
          backgroundColor: '#f9fafb',
          borderRadius: '8px',
          color: '#6b7280'
        }}>
          <div style={{ fontSize: '48px', marginBottom: '16px' }}>📊</div>
          <h3 style={{ margin: '0 0 8px 0' }}>No patients available</h3>
          <p style={{ margin: '0' }}>
            Add patients to view medication and seizure correlation analytics.
          </p>
          {error && (
            <div style={{ marginTop: '16px', color: '#dc2626', fontSize: '14px' }}>
              Error: {error}
            </div>
          )}
        </div>
      </div>
    );
  }

  const stats = getKeyStatistics();

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <div>
          <h1 style={titleStyle}>📊 Clinical Analytics Dashboard</h1>
          <p style={{ color: '#6b7280', margin: '5px 0 0 0' }}>
            Medication adherence vs medical event correlation analysis
          </p>
          {useMockData && (
            <div style={{
              backgroundColor: '#fef3c7',
              border: '1px solid #f59e0b',
              borderRadius: '6px',
              padding: '8px 12px',
              marginTop: '8px',
              fontSize: '12px',
              color: '#92400e'
            }}>
              📊 Using demonstration data with realistic correlations
            </div>
          )}
        </div>
        <div>
          <button
            onClick={() => setUseMockData(!useMockData)}
            style={{
              padding: '8px 16px',
              border: '1px solid #d1d5db',
              borderRadius: '6px',
              backgroundColor: useMockData ? '#2563eb' : 'white',
              color: useMockData ? 'white' : '#374151',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: useMockData ? '600' : '400'
            }}
          >
            {useMockData ? '📊 Demo Data' : '🔄 Real Data'}
          </button>
        </div>
      </div>

      {error && (
        <div style={alertStyle}>
          {error}
        </div>
      )}

      {/* Controls */}
      <div style={controlsStyle}>
        <div>
          <label style={{ fontSize: '14px', fontWeight: '600', marginBottom: '5px', display: 'block' }}>
            Patient
          </label>
          <select
            value={selectedPatientId}
            onChange={(e) => setSelectedPatientId(e.target.value)}
            style={selectStyle}
          >
            <option value="">Select patient...</option>
            {patients.map(patient => (
              <option key={patient.id} value={patient.id}>
                {patientService.formatPatientName(patient)} 
                ({patientService.calculateAge(patient.dateOfBirth)} years old)
              </option>
            ))}
          </select>
        </div>

        <div>
          <label style={{ fontSize: '14px', fontWeight: '600', marginBottom: '5px', display: 'block' }}>
            Time Period
          </label>
          <div style={buttonGroupStyle}>
            <button onClick={() => handleTimeRangeChange(7)} style={timeButtonStyle(7)}>
              7 days
            </button>
            <button onClick={() => handleTimeRangeChange(30)} style={timeButtonStyle(30)}>
              30 days
            </button>
            <button onClick={() => handleTimeRangeChange(90)} style={timeButtonStyle(90)}>
              90 days
            </button>
          </div>
        </div>

        <div>
          <label style={{ fontSize: '14px', fontWeight: '600', marginBottom: '5px', display: 'block' }}>
            Date Range
          </label>
          <div style={{ fontSize: '14px', color: '#6b7280' }}>
            {timeRange.start.toLocaleDateString('en-GB')} - {timeRange.end.toLocaleDateString('en-GB')}
          </div>
        </div>
      </div>

      {/* Key Statistics */}
      <div style={statsGridStyle}>
        <div style={statCardStyle}>
          <div style={statNumberStyle('#dc2626')}>{stats.totalSeizures}</div>
          <div style={statLabelStyle}>Total Seizures</div>
          <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '4px' }}>
            {stats.seizureFrequency}/day average
          </div>
        </div>

        <div style={statCardStyle}>
          <div style={statNumberStyle(stats.adherenceRate >= 80 ? '#10b981' : '#f59e0b')}>
            {stats.adherenceRate}%
          </div>
          <div style={statLabelStyle}>Medication Adherence</div>
          <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '4px' }}>
            {stats.activeMedications} active medication{stats.activeMedications !== 1 ? 's' : ''}
          </div>
        </div>

        <div style={statCardStyle}>
          <div style={statNumberStyle(
            stats.trendDirection === 'decreasing' ? '#10b981' : 
            stats.trendDirection === 'increasing' ? '#dc2626' : '#6b7280'
          )}>
            {stats.trendDirection === 'decreasing' ? '↓' : 
             stats.trendDirection === 'increasing' ? '↑' : '→'}
          </div>
          <div style={statLabelStyle}>Seizure Trend</div>
          <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '4px' }}>
            {stats.trendDirection}
          </div>
        </div>

        <div style={statCardStyle}>
          <div style={statNumberStyle('#8b5cf6')}>
            {Math.round(stats.averageSeverity * 10) / 10}
          </div>
          <div style={statLabelStyle}>Average Severity</div>
          <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '4px' }}>
            1=Mild, 4=Critical
          </div>
        </div>

        <div style={statCardStyle}>
          <div style={statNumberStyle('#06b6d4')}>{stats.totalEvents}</div>
          <div style={statLabelStyle}>Total Events</div>
          <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '4px' }}>
            All medical events
          </div>
        </div>
      </div>

      {/* Correlation Chart */}
      {selectedPatientId && events.length >= 0 && dosageRecords.length >= 0 ? (
        <div style={{
          backgroundColor: 'white',
          borderRadius: '8px',
          padding: '20px',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          marginBottom: '20px'
        }}>
          <h3 style={{ margin: '0 0 16px 0', fontSize: '18px', fontWeight: '600' }}>
            📊 Medication vs Seizure Correlation Analysis - {getPatientName(selectedPatientId)}
          </h3>
          
          <CorrelationChart
            events={events}
            medications={getPatientMedications(selectedPatientId)}
            dosageRecords={dosageRecords}
            timeRange={timeRange}
            patient={patients.find(p => p.id === selectedPatientId)!}
            patientName={getPatientName(selectedPatientId)}
          />
        </div>
      ) : (
        <div style={{
          backgroundColor: 'white',
          borderRadius: '8px',
          padding: '20px',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          marginBottom: '20px'
        }}>
          <div style={{ textAlign: 'center', padding: '40px', color: '#6b7280' }}>
            <div style={{ fontSize: '48px', marginBottom: '16px' }}>📊</div>
            <div>Select a patient to view correlation analysis</div>
          </div>
        </div>
      )}

      {/* Insights and Recommendations */}
      <div style={{
        backgroundColor: 'white',
        borderRadius: '8px',
        padding: '20px',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        marginTop: '20px'
      }}>
        <h3 style={{ margin: '0 0 16px 0', fontSize: '18px', fontWeight: '600' }}>
          💡 Clinical Insights & Recommendations
        </h3>
        
        <div style={{ display: 'grid', gap: '12px' }}>
          {stats.adherenceRate < 80 && (
            <div style={{
              padding: '12px',
              backgroundColor: '#fef3c7',
              border: '1px solid #f59e0b',
              borderRadius: '6px',
              color: '#92400e'
            }}>
              <strong>⚠️ Low Adherence:</strong> Medication adherence is below 80%. 
              Consider reviewing medication schedule and barriers to compliance.
            </div>
          )}
          
          {stats.trendDirection === 'increasing' && (
            <div style={{
              padding: '12px',
              backgroundColor: '#fee2e2',
              border: '1px solid #dc2626',
              borderRadius: '6px',
              color: '#991b1b'
            }}>
              <strong>📈 Increasing Seizures:</strong> Seizure frequency is increasing. 
              Review medication effectiveness and consider dosage adjustments.
            </div>
          )}
          
          {stats.trendDirection === 'decreasing' && stats.adherenceRate >= 80 && (
            <div style={{
              padding: '12px',
              backgroundColor: '#d1fae5',
              border: '1px solid #10b981',
              borderRadius: '6px',
              color: '#065f46'
            }}>
              <strong>✅ Positive Trend:</strong> Excellent adherence with decreasing seizure frequency. 
              Current treatment plan appears effective.
            </div>
          )}
          
          {stats.averageSeverity >= 3 && (
            <div style={{
              padding: '12px',
              backgroundColor: '#fee2e2',
              border: '1px solid #dc2626',
              borderRadius: '6px',
              color: '#991b1b'
            }}>
              <strong>🚨 High Severity:</strong> Average seizure severity is concerning. 
              Consider emergency action plan review and medication optimization.
            </div>
          )}
          
          {events.length < 3 && (
            <div style={{
              padding: '12px',
              backgroundColor: '#f3f4f6',
              border: '1px solid #9ca3af',
              borderRadius: '6px',
              color: '#374151'
            }}>
              <strong>📊 Limited Data:</strong> More data points needed for reliable correlation analysis. 
              Continue tracking for {30 - Math.ceil((timeRange.end.getTime() - timeRange.start.getTime()) / (1000 * 60 * 60 * 24))} more days for better insights.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};