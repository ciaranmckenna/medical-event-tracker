import { useState } from 'react';
import { PatientDashboard } from '../../components/dashboard/PatientDashboard';
import { PatientForm } from '../../components/forms/PatientForm';
import { patientService } from '../../services/api/patientService';
import type { Patient } from '../../types/api';
import type { PatientFormData } from '../../services/validation/patientValidation';

type ViewMode = 'dashboard' | 'add' | 'edit' | 'view';

export const PatientsPage: React.FC = () => {
  const [viewMode, setViewMode] = useState<ViewMode>('dashboard');
  const [selectedPatient, setSelectedPatient] = useState<Patient | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string>('');

  const handleAddPatient = async (data: PatientFormData) => {
    const patientData = patientService.transformFormToApiRequest(data);
    try {
      await patientService.createPatient(patientData);
      setViewMode('dashboard');
    } catch (error) {
      throw error; // Let PatientForm handle the error display
    }
  };

  const handleEditPatient = async (data: PatientFormData) => {
    if (!selectedPatient) return;
    
    const patientData = patientService.transformFormToApiRequest(data);
    try {
      await patientService.updatePatient(selectedPatient.id, {
        ...patientData,
        id: selectedPatient.id
      });
      setViewMode('dashboard');
      setSelectedPatient(null);
    } catch (error) {
      throw error; // Let PatientForm handle the error display
    }
  };

  const handleDeletePatient = async (patient: Patient) => {
    try {
      await patientService.deletePatient(patient.id);
      // Dashboard will automatically refresh
    } catch (error: any) {
      setError(`Failed to delete ${patientService.formatPatientName(patient)}. Please try again.`);
    }
  };

  const containerStyle = {
    padding: '20px',
    maxWidth: '1200px',
    margin: '0 auto'
  };

  const backButtonStyle = {
    padding: '10px 20px',
    border: 'none',
    borderRadius: '4px',
    fontSize: '16px',
    cursor: 'pointer',
    backgroundColor: '#6c757d',
    color: 'white',
    marginBottom: '20px'
  };

  const errorStyle = {
    color: '#dc3545',
    backgroundColor: '#f8d7da',
    padding: '10px',
    borderRadius: '4px',
    marginBottom: '20px'
  };

  if (viewMode === 'add') {
    return (
      <div style={containerStyle}>
        <button onClick={() => setViewMode('dashboard')} style={backButtonStyle}>
          ← Back to Dashboard
        </button>
        {error && <div style={errorStyle}>{error}</div>}
        <PatientForm
          onSubmit={handleAddPatient}
          onCancel={() => setViewMode('dashboard')}
          isLoading={isLoading}
        />
      </div>
    );
  }

  if (viewMode === 'edit' && selectedPatient) {
    return (
      <div style={containerStyle}>
        <button onClick={() => setViewMode('dashboard')} style={backButtonStyle}>
          ← Back to Dashboard
        </button>
        {error && <div style={errorStyle}>{error}</div>}
        <PatientForm
          patient={selectedPatient}
          onSubmit={handleEditPatient}
          onCancel={() => {
            setViewMode('dashboard');
            setSelectedPatient(null);
          }}
          isLoading={isLoading}
        />
      </div>
    );
  }

  if (viewMode === 'view' && selectedPatient) {
    const age = patientService.calculateAge(selectedPatient.dateOfBirth);
    return (
      <div style={containerStyle}>
        <button onClick={() => setViewMode('dashboard')} style={backButtonStyle}>
          ← Back to Dashboard
        </button>
        <div style={{ maxWidth: '600px', margin: '0 auto', padding: '30px', border: '1px solid #ddd', borderRadius: '8px', backgroundColor: '#f8f9fa' }}>
          <h2 style={{ textAlign: 'center', marginBottom: '30px' }}>
            👤 {patientService.formatPatientName(selectedPatient)}
          </h2>
          
          <div style={{ marginBottom: '20px' }}>
            <strong>Age:</strong> {age} years old<br />
            <strong>Date of Birth:</strong> {new Date(selectedPatient.dateOfBirth).toLocaleDateString()}<br />
            {selectedPatient.height && <><strong>Height:</strong> {selectedPatient.height} cm<br /></>}
            {selectedPatient.weight && <><strong>Weight:</strong> {selectedPatient.weight} kg<br /></>}
            {selectedPatient.emergencyContact && (
              <><strong>Emergency Contact:</strong> {selectedPatient.emergencyContact}
                {selectedPatient.emergencyPhone && ` (${selectedPatient.emergencyPhone})`}<br /></>
            )}
          </div>

          {selectedPatient.notes && (
            <div style={{ marginBottom: '20px' }}>
              <strong>Notes:</strong><br />
              <div style={{ padding: '10px', backgroundColor: 'white', border: '1px solid #ddd', borderRadius: '4px', marginTop: '5px' }}>
                {selectedPatient.notes}
              </div>
            </div>
          )}

          <div style={{ textAlign: 'center', marginTop: '30px' }}>
            <button 
              onClick={() => setViewMode('edit')} 
              style={{
                padding: '10px 20px',
                border: 'none',
                borderRadius: '4px',
                fontSize: '16px',
                cursor: 'pointer',
                backgroundColor: '#28a745', 
                color: 'white', 
                marginRight: '10px'
              }}
            >
              ✏️ Edit Patient
            </button>
          </div>
        </div>
      </div>
    );
  }

  // Default view is the dashboard
  return (
    <PatientDashboard
      onAddPatient={() => setViewMode('add')}
      onEditPatient={(patient) => {
        setSelectedPatient(patient);
        setViewMode('edit');
      }}
      onViewPatient={(patient) => {
        setSelectedPatient(patient);
        setViewMode('view');
      }}
      onDeletePatient={handleDeletePatient}
    />
  );
};