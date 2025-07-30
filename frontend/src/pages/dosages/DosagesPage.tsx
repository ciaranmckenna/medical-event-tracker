import { useState, useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import { dosageService } from '../../services/api/dosageService';
import { patientService } from '../../services/api/patientService';
import { medicationService } from '../../services/api/medicationService';
import { DosageCard } from '../../components/medical/DosageCard';
import type { Patient, Medication } from '../../types/api';
import type { DosageRecord, DosageSearchParams } from '../../services/api/dosageService';

export const DosagesPage: React.FC = () => {
  const { user, hasRole } = useAuth();
  const navigate = useNavigate();
  
  // State management
  const [patients, setPatients] = useState<Patient[]>([]);
  const [medications, setMedications] = useState<Medication[]>([]);
  const [dosageRecords, setDosageRecords] = useState<DosageRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [selectedPatientId, setSelectedPatientId] = useState<string>('');
  const [showOverdue, setShowOverdue] = useState(false);

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }
    loadInitialData();
  }, [user, navigate]);

  useEffect(() => {
    if (selectedPatientId) {
      loadDosageData();
    }
  }, [selectedPatientId]);

  const loadInitialData = async () => {
    try {
      const [patientsResponse, medicationsResponse] = await Promise.all([
        patientService.getPatients(),
        medicationService.getMedications()
      ]);
      
      setPatients(patientsResponse.content);
      setMedications(medicationsResponse.content.filter(med => med.status === 'ACTIVE'));
      
      // Auto-select first patient if available
      if (patientsResponse.content.length > 0) {
        setSelectedPatientId(patientsResponse.content[0].id);
      }
    } catch (error) {
      console.error('Failed to load initial data:', error);
      setError('Failed to load patients and medications');
    } finally {
      setLoading(false);
    }
  };

  const loadDosageData = async () => {
    if (!selectedPatientId) return;
    
    try {
      // Load last 14 days of dosage records for the selected patient
      const fourteenDaysAgo = new Date(Date.now() - 14 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
      const searchParams: DosageSearchParams = {
        patientId: selectedPatientId,
        dateFrom: fourteenDaysAgo,
        size: 100 // Get enough records
      };
      
      const dosageResponse = await dosageService.getDosageRecords(searchParams);
      setDosageRecords(dosageResponse.content);
    } catch (error) {
      console.error('Failed to load dosage records:', error);
      setError('Failed to load dosage records');
    }
  };

  const handleAdministerMedication = async (
    medicationId: string, 
    schedule: 'AM' | 'PM', 
    dosage: number, 
    notes?: string
  ) => {
    try {
      const newRecord = await dosageService.administerMedication(
        medicationId,
        selectedPatientId,
        schedule,
        dosage,
        notes,
        user?.firstName + ' ' + user?.lastName
      );
      
      // Update local records
      setDosageRecords(prev => {
        // Remove any existing record for today and add the new one
        const today = new Date().toISOString().split('T')[0];
        const filtered = prev.filter(record => 
          !(record.medicationId === medicationId && 
            record.schedule === schedule && 
            record.scheduledTime.startsWith(today))
        );
        return [newRecord, ...filtered];
      });
    } catch (error) {
      console.error('Failed to administer medication:', error);
      throw error;
    }
  };

  const handleEditMedication = (medication: Medication) => {
    // Navigate to medications page with edit mode
    navigate(`/medications?edit=${medication.id}`);
  };

  const getPatientMedications = (patientId: string) => {
    return medications.filter(med => med.patientId === patientId && med.status === 'ACTIVE');
  };

  const getPatientName = (patientId: string) => {
    const patient = patients.find(p => p.id === patientId);
    return patient ? patientService.formatPatientName(patient) : 'Unknown Patient';
  };

  const getOverdueMedications = () => {
    if (!selectedPatientId) return [];
    
    const today = new Date().toISOString().split('T')[0];
    const currentHour = new Date().getHours();
    const patientMeds = getPatientMedications(selectedPatientId);
    
    return patientMeds.filter(medication => {
      // Check if AM dose is overdue (after 12 PM)
      const isAMOverdue = (medication.frequency === 'ONCE_DAILY' || medication.frequency === 'TWICE_DAILY') &&
        currentHour > 12 &&
        !dosageRecords.some(record => 
          record.medicationId === medication.id &&
          record.schedule === 'AM' &&
          record.administered &&
          record.scheduledTime.startsWith(today)
        );
      
      // Check if PM dose is overdue (after 10 PM)
      const isPMOverdue = medication.frequency === 'TWICE_DAILY' &&
        currentHour > 22 &&
        !dosageRecords.some(record => 
          record.medicationId === medication.id &&
          record.schedule === 'PM' &&
          record.administered &&
          record.scheduledTime.startsWith(today)
        );
      
      return isAMOverdue || isPMOverdue;
    });
  };

  const getAdherenceStats = () => {
    if (!selectedPatientId) return null;
    
    const patientMeds = getPatientMedications(selectedPatientId);
    const sevenDaysAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000);
    
    let totalExpected = 0;
    let totalAdministered = 0;
    
    patientMeds.forEach(medication => {
      const expectedPerDay = medication.frequency === 'TWICE_DAILY' ? 2 : 1;
      totalExpected += expectedPerDay * 7; // 7 days
      
      const administeredCount = dosageRecords.filter(record =>
        record.medicationId === medication.id &&
        record.administered &&
        new Date(record.scheduledTime) >= sevenDaysAgo
      ).length;
      
      totalAdministered += administeredCount;
    });
    
    const adherencePercentage = totalExpected > 0 ? Math.round((totalAdministered / totalExpected) * 100) : 0;
    
    return {
      totalExpected,
      totalAdministered,
      adherencePercentage,
      missedDoses: totalExpected - totalAdministered
    };
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

  const filtersStyle = {
    backgroundColor: 'white',
    padding: '20px',
    borderRadius: '8px',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
    marginBottom: '20px',
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
    gap: '15px'
  };

  const selectStyle = {
    padding: '8px 12px',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    fontSize: '14px',
    width: '100%'
  };

  const statsStyle = {
    backgroundColor: '#f3f4f6',
    padding: '20px',
    borderRadius: '8px',
    marginBottom: '20px',
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    gap: '15px'
  };

  const statCardStyle = {
    backgroundColor: 'white',
    padding: '15px',
    borderRadius: '6px',
    textAlign: 'center' as const
  };

  const statNumberStyle = {
    fontSize: '24px',
    fontWeight: '700',
    color: '#1f2937',
    margin: '0'
  };

  const statLabelStyle = {
    fontSize: '14px',
    color: '#6b7280',
    margin: '5px 0 0 0'
  };

  const alertStyle = {
    backgroundColor: '#fee2e2',
    border: '1px solid #fca5a5',
    borderRadius: '8px',
    padding: '15px',
    marginBottom: '20px',
    color: '#dc2626'
  };

  const adherenceStats = getAdherenceStats();
  const overdueMedications = getOverdueMedications();

  if (loading) {
    return (
      <div style={containerStyle}>
        <div style={{ textAlign: 'center', padding: '50px' }}>
          Loading medication schedules...
        </div>
      </div>
    );
  }

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <div>
          <h1 style={titleStyle}>💊 Medication Schedule</h1>
          <p style={{ color: '#6b7280', margin: '5px 0 0 0' }}>
            Track daily medication administration and adherence
          </p>
        </div>
      </div>

      {error && (
        <div style={alertStyle}>
          {error}
        </div>
      )}


      {/* Patient Selection */}
      <div style={filtersStyle}>
        <div>
          <label style={{ fontSize: '14px', fontWeight: '600', marginBottom: '5px', display: 'block' }}>
            Select Patient
          </label>
          <select
            value={selectedPatientId}
            onChange={(e) => setSelectedPatientId(e.target.value)}
            style={selectStyle}
          >
            <option value="">Choose a patient...</option>
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
            View Options
          </label>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <input
              type="checkbox"
              id="showOverdue"
              checked={showOverdue}
              onChange={(e) => setShowOverdue(e.target.checked)}
            />
            <label htmlFor="showOverdue" style={{ fontSize: '14px' }}>
              Show overdue only
            </label>
          </div>
        </div>
      </div>

      {selectedPatientId && (
        <>
          {/* Overdue Alert */}
          {overdueMedications.length > 0 && (
            <div style={alertStyle}>
              <strong>⚠️ {overdueMedications.length} overdue medication{overdueMedications.length !== 1 ? 's' : ''}</strong>
              <div style={{ marginTop: '8px' }}>
                {overdueMedications.map(med => (
                  <div key={med.id}>• {med.name}</div>
                ))}
              </div>
            </div>
          )}

          {/* Adherence Statistics */}
          {adherenceStats && (
            <div style={statsStyle}>
              <div style={statCardStyle}>
                <div style={{ 
                  ...statNumberStyle, 
                  color: adherenceStats.adherencePercentage >= 80 ? '#10b981' : '#dc2626' 
                }}>
                  {adherenceStats.adherencePercentage}%
                </div>
                <div style={statLabelStyle}>7-Day Adherence</div>
              </div>
              
              <div style={statCardStyle}>
                <div style={statNumberStyle}>{adherenceStats.totalAdministered}</div>
                <div style={statLabelStyle}>Doses Taken</div>
              </div>
              
              <div style={statCardStyle}>
                <div style={{ ...statNumberStyle, color: adherenceStats.missedDoses > 0 ? '#dc2626' : '#10b981' }}>
                  {adherenceStats.missedDoses}
                </div>
                <div style={statLabelStyle}>Missed Doses</div>
              </div>
              
              <div style={statCardStyle}>
                <div style={statNumberStyle}>{getPatientMedications(selectedPatientId).length}</div>
                <div style={statLabelStyle}>Active Medications</div>
              </div>
            </div>
          )}

          {/* Medication Cards */}
          <div>
            {getPatientMedications(selectedPatientId).length === 0 ? (
              <div style={{
                textAlign: 'center',
                padding: '60px 20px',
                backgroundColor: '#f9fafb',
                borderRadius: '8px',
                color: '#6b7280'
              }}>
                <div style={{ fontSize: '48px', marginBottom: '16px' }}>💊</div>
                <h3 style={{ margin: '0 0 8px 0' }}>No active medications</h3>
                <p style={{ margin: '0' }}>
                  This patient doesn't have any active medications to track.
                </p>
                {hasRole('PRIMARY_USER') && (
                  <button
                    onClick={() => navigate('/medications')}
                    style={{
                      marginTop: '20px',
                      padding: '12px 20px',
                      backgroundColor: '#2563eb',
                      color: 'white',
                      border: 'none',
                      borderRadius: '8px',
                      fontSize: '16px',
                      cursor: 'pointer'
                    }}
                  >
                    Add Medications
                  </button>
                )}
              </div>
            ) : (
              getPatientMedications(selectedPatientId)
                .filter(medication => !showOverdue || overdueMedications.includes(medication))
                .map(medication => (
                  <DosageCard
                    key={medication.id}
                    medication={medication}
                    patientName={getPatientName(selectedPatientId)}
                    dosageRecords={dosageRecords.filter(record => record.medicationId === medication.id)}
                    onAdminister={handleAdministerMedication}
                    onEdit={handleEditMedication}
                    canAdminister={hasRole('PRIMARY_USER') || hasRole('SECONDARY_USER')}
                  />
                ))
            )}
          </div>
        </>
      )}

      {!selectedPatientId && !loading && (
        <div style={{
          textAlign: 'center',
          padding: '60px 20px',
          backgroundColor: '#f9fafb',
          borderRadius: '8px',
          color: '#6b7280'
        }}>
          <div style={{ fontSize: '48px', marginBottom: '16px' }}>👤</div>
          <h3 style={{ margin: '0 0 8px 0' }}>Select a patient</h3>
          <p style={{ margin: '0' }}>
            Choose a patient from the dropdown above to view their medication schedule.
          </p>
        </div>
      )}
    </div>
  );
};