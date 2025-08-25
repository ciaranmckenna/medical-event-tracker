import { useState, useEffect } from 'react';
import { MedicationCard } from '../../components/medical/MedicationCard';
import { MedicationForm } from '../../components/forms/MedicationForm';
import { medicationService } from '../../services/api/medicationService';
import { patientService } from '../../services/api/patientService';
import type { Medication, MedicationCatalog, Patient, PaginatedResponse } from '../../types/api';
import type { MedicationFormData } from '../../services/validation/medicationValidation';

type ViewMode = 'list' | 'add' | 'edit' | 'view' | 'record-dose';

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
  }
];

const MOCK_CATALOG_MEDICATIONS: MedicationCatalog[] = [
  {
    id: '1',
    name: 'Paracetamol',
    genericName: 'Acetaminophen',
    type: 'TABLET',
    strength: 500,
    unit: 'mg',
    manufacturer: 'Generic Pharma',
    description: 'Pain reliever and fever reducer',
    active: true,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z'
  },
  {
    id: '2',
    name: 'Ibuprofen',
    genericName: 'Ibuprofen',
    type: 'TABLET',
    strength: 200,
    unit: 'mg',
    manufacturer: 'Advil',
    description: 'Anti-inflammatory pain reliever',
    active: true,
    createdAt: '2024-01-15T00:00:00Z',
    updatedAt: '2024-01-15T00:00:00Z'
  },
  {
    id: '3',
    name: 'Amoxicillin',
    genericName: 'Amoxicillin',
    type: 'CAPSULE',
    strength: 250,
    unit: 'mg',
    manufacturer: 'Generic Antibiotics Ltd',
    description: 'Antibiotic for bacterial infections',
    active: false,
    createdAt: '2024-01-10T00:00:00Z',
    updatedAt: '2024-01-20T00:00:00Z'
  }
];

export const MedicationsPage: React.FC = () => {
  const [viewMode, setViewMode] = useState<ViewMode>('list');
  const [medications, setMedications] = useState<MedicationCatalog[]>([]);
  const [patients, setPatients] = useState<Patient[]>([]);
  const [selectedMedication, setSelectedMedication] = useState<MedicationCatalog | null>(null);
  const [selectedPatientId, setSelectedPatientId] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterActive, setFilterActive] = useState<boolean | undefined>(true);
  const [error, setError] = useState<string>('');
  const [useMockData, setUseMockData] = useState(false);

  // Load medications and patients on component mount
  useEffect(() => {
    loadMedications();
    loadPatients();
  }, []);

  const loadMedications = async () => {
    setIsLoading(true);
    setError('');
    try {
      if (useMockData) {
        // Use mock data
        let filteredMedications = MOCK_CATALOG_MEDICATIONS;
        
        // Catalog medications don't have patientId, so skip patient filtering
        
        if (filterActive !== undefined) {
          filteredMedications = filteredMedications.filter(m => m.active === filterActive);
        }
        
        if (searchTerm) {
          const searchLower = searchTerm.toLowerCase();
          filteredMedications = filteredMedications.filter(m => 
            m.name.toLowerCase().includes(searchLower)
          );
        }
        
        setMedications(filteredMedications);
      } else {
        // Try to load from API
        const response: PaginatedResponse<MedicationCatalog> = await medicationService.getMedications({
          medicationName: searchTerm || undefined,
          active: filterActive
        });
        setMedications(response.content);
      }
    } catch (error: any) {
      console.error('Failed to load medications:', error);
      if (!useMockData) {
        setError('Failed to connect to backend. Switching to demo mode with mock data.');
        setUseMockData(true);
        // Don't retry immediately - let the useEffect handle the reload
      } else {
        setError('Failed to load medications. Please try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const loadPatients = async () => {
    try {
      if (useMockData) {
        setPatients(MOCK_PATIENTS);
      } else {
        const response: PaginatedResponse<Patient> = await patientService.getPatients();
        setPatients(response.content);
      }
    } catch (error) {
      console.error('Failed to load patients:', error);
      if (!useMockData) {
        setUseMockData(true);
        setPatients(MOCK_PATIENTS);
      }
    }
  };

  const handleAddMedication = async (data: MedicationFormData) => {
    if (useMockData) {
      // Simulate adding catalog medication with mock data
      const newMedication: MedicationCatalog = {
        id: (MOCK_CATALOG_MEDICATIONS.length + 1).toString(),
        name: data.name,
        genericName: data.genericName,
        type: data.type,
        strength: data.strength,
        unit: data.unit,
        manufacturer: data.manufacturer,
        description: data.description,
        active: true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      
      MOCK_CATALOG_MEDICATIONS.push(newMedication);
      await loadMedications();
      setViewMode('list');
      return;
    }

    const medicationData = medicationService.transformMedicationFormToApiRequest(data);
    try {
      await medicationService.createMedication(medicationData);
      await loadMedications();
      setViewMode('list');
    } catch (error) {
      throw error; // Let MedicationForm handle the error display
    }
  };

  const handleEditMedication = async (data: MedicationFormData) => {
    if (!selectedMedication) return;
    
    if (useMockData) {
      // Simulate editing catalog medication with mock data
      const index = MOCK_CATALOG_MEDICATIONS.findIndex(m => m.id === selectedMedication.id);
      if (index !== -1) {
        MOCK_CATALOG_MEDICATIONS[index] = {
          ...MOCK_CATALOG_MEDICATIONS[index],
          name: data.name,
          genericName: data.genericName,
          type: data.type,
          strength: data.strength,
          unit: data.unit,
          manufacturer: data.manufacturer,
          description: data.description,
          updatedAt: new Date().toISOString()
        };
      }
      
      await loadMedications();
      setViewMode('list');
      setSelectedMedication(null);
      return;
    }
    
    const medicationData = medicationService.transformMedicationFormToApiRequest(data);
    try {
      await medicationService.updateMedication(selectedMedication.id, {
        ...medicationData,
        id: selectedMedication.id
      });
      await loadMedications();
      setViewMode('list');
      setSelectedMedication(null);
    } catch (error) {
      throw error; // Let MedicationForm handle the error display
    }
  };

  const handleDeleteMedication = async (medication: MedicationCatalog) => {
    if (useMockData) {
      // Simulate deleting catalog medication with mock data
      const index = MOCK_CATALOG_MEDICATIONS.findIndex(m => m.id === medication.id);
      if (index !== -1) {
        MOCK_CATALOG_MEDICATIONS.splice(index, 1);
      }
      await loadMedications();
      return;
    }

    try {
      await medicationService.deleteMedication(medication.id);
      await loadMedications();
    } catch (error: any) {
      setError(`Failed to delete ${medication.name}. Please try again.`);
    }
  };

  const handleSearch = async () => {
    await loadMedications();
  };

  // Reload when filters change
  useEffect(() => {
    loadMedications();
  }, [filterActive, useMockData]);

  const filteredMedications = medications;

  const containerStyle = {
    padding: '20px',
    maxWidth: '1200px',
    margin: '0 auto'
  };

  const headerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '30px',
    flexWrap: 'wrap' as const
  };

  const filtersStyle = {
    display: 'flex',
    gap: '15px',
    marginBottom: '20px',
    alignItems: 'center',
    flexWrap: 'wrap' as const
  };

  const inputStyle = {
    padding: '10px',
    border: '1px solid #ddd',
    borderRadius: '4px',
    fontSize: '16px',
    width: '250px'
  };

  const selectStyle = {
    ...inputStyle,
    cursor: 'pointer'
  };

  const buttonStyle = {
    padding: '10px 20px',
    border: 'none',
    borderRadius: '4px',
    fontSize: '16px',
    cursor: 'pointer'
  };

  const addButtonStyle = {
    ...buttonStyle,
    backgroundColor: '#28a745',
    color: 'white'
  };

  const searchButtonStyle = {
    ...buttonStyle,
    backgroundColor: '#007bff',
    color: 'white'
  };

  const backButtonStyle = {
    ...buttonStyle,
    backgroundColor: '#6c757d',
    color: 'white',
    marginBottom: '20px'
  };

  const filterButtonStyle = (active: boolean) => ({
    ...buttonStyle,
    backgroundColor: active ? '#007bff' : '#e9ecef',
    color: active ? 'white' : '#6c757d',
    border: active ? 'none' : '1px solid #ddd'
  });

  const errorStyle = {
    color: '#dc3545',
    backgroundColor: '#f8d7da',
    padding: '10px',
    borderRadius: '4px',
    marginBottom: '20px'
  };

  const successStyle = {
    color: '#155724',
    backgroundColor: '#d4edda',
    padding: '10px',
    borderRadius: '4px',
    marginBottom: '20px'
  };

  const loadingStyle = {
    textAlign: 'center' as const,
    padding: '40px',
    fontSize: '18px',
    color: '#666'
  };

  const emptyStateStyle = {
    textAlign: 'center' as const,
    padding: '40px',
    color: '#666'
  };

  if (viewMode === 'add') {
    return (
      <div style={containerStyle}>
        <button onClick={() => setViewMode('list')} style={backButtonStyle}>
          ← Back to Medications
        </button>
        {useMockData && (
          <div style={successStyle}>
            🧪 Demo Mode: Changes will be saved temporarily for testing
          </div>
        )}
        <MedicationForm
          selectedPatientId={selectedPatientId}
          onSubmit={handleAddMedication}
          onCancel={() => setViewMode('list')}
          isLoading={isLoading}
        />
      </div>
    );
  }

  if (viewMode === 'edit' && selectedMedication) {
    return (
      <div style={containerStyle}>
        <button onClick={() => setViewMode('list')} style={backButtonStyle}>
          ← Back to Medications
        </button>
        {useMockData && (
          <div style={successStyle}>
            🧪 Demo Mode: Changes will be saved temporarily for testing
          </div>
        )}
        <MedicationForm
          medication={selectedMedication}
          onSubmit={handleEditMedication}
          onCancel={() => {
            setViewMode('list');
            setSelectedMedication(null);
          }}
          isLoading={isLoading}
        />
      </div>
    );
  }

  if (viewMode === 'view' && selectedMedication) {
    const status = medicationService.getMedicationStatus(selectedMedication);
    const daysRemaining = medicationService.calculateDaysRemaining(selectedMedication);
    
    return (
      <div style={containerStyle}>
        <button onClick={() => setViewMode('list')} style={backButtonStyle}>
          ← Back to Medications
        </button>
        <div style={{ maxWidth: '600px', margin: '0 auto', padding: '30px', border: '1px solid #ddd', borderRadius: '8px', backgroundColor: '#f8f9fa' }}>
          <h2 style={{ textAlign: 'center', marginBottom: '30px' }}>
            💊 {medicationService.formatMedicationDisplay(selectedMedication)}
          </h2>
          
          <div style={{ marginBottom: '20px' }}>
            <strong>Status:</strong> <span style={{ 
              color: status === 'Active' ? '#28a745' : 
                     status === 'Inactive' ? '#6c757d' : 
                     status === 'Expired' ? '#dc3545' : '#ffc107'
            }}>{status}</span><br />
            <strong>Frequency:</strong> {medicationService.formatFrequencyDisplay(selectedMedication.frequency)}<br />
            <strong>Start Date:</strong> {new Date(selectedMedication.startDate).toLocaleDateString()}<br />
            {selectedMedication.endDate && (
              <><strong>End Date:</strong> {new Date(selectedMedication.endDate).toLocaleDateString()}
                {daysRemaining !== null && (
                  <span style={{ marginLeft: '10px', color: daysRemaining <= 7 ? '#dc3545' : '#666' }}>
                    ({daysRemaining > 0 ? `${daysRemaining} days remaining` : 'Expired'})
                  </span>
                )}<br />
              </>
            )}
          </div>

          {selectedMedication.notes && (
            <div style={{ marginBottom: '20px' }}>
              <strong>Notes:</strong><br />
              <div style={{ padding: '10px', backgroundColor: 'white', border: '1px solid #ddd', borderRadius: '4px', marginTop: '5px' }}>
                {selectedMedication.notes}
              </div>
            </div>
          )}

          <div style={{ textAlign: 'center', marginTop: '30px' }}>
            <button 
              onClick={() => setViewMode('edit')} 
              style={{...buttonStyle, backgroundColor: '#28a745', color: 'white', marginRight: '10px'}}
            >
              ✏️ Edit Medication
            </button>
            {medicationService.isActiveMedication(selectedMedication) && (
              <button 
                onClick={() => setViewMode('record-dose')} 
                style={{...buttonStyle, backgroundColor: '#007bff', color: 'white'}}
              >
                📝 Record Dose
              </button>
            )}
          </div>
        </div>
      </div>
    );
  }

  if (viewMode === 'record-dose' && selectedMedication) {
    return (
      <div style={containerStyle}>
        <button onClick={() => setViewMode('list')} style={backButtonStyle}>
          ← Back to Medications
        </button>
        <div style={{ textAlign: 'center', padding: '40px' }}>
          <h2>📝 Record Dose</h2>
          <p>Dose recording functionality will be implemented in the next phase</p>
          <p>For medication: {medicationService.formatMedicationDisplay(selectedMedication)}</p>
        </div>
      </div>
    );
  }

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <h1>💊 Medication Management</h1>
        <button onClick={() => setViewMode('add')} style={addButtonStyle}>
          ➕ Add New Medication
        </button>
      </div>

      {useMockData && (
        <div style={successStyle}>
          🧪 <strong>Demo Mode Active:</strong> Using sample data for testing. Backend connection failed - this is normal for frontend-only testing.
        </div>
      )}

      <div style={filtersStyle}>
        <input
          type="text"
          placeholder="Search medications by name..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={inputStyle}
          onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
        />
        
        {/* Patient filter removed for medication catalog */}

        <div>
          <button 
            onClick={() => setFilterActive(true)}
            style={filterButtonStyle(filterActive === true)}
          >
            Active
          </button>
          <button 
            onClick={() => setFilterActive(false)}
            style={filterButtonStyle(filterActive === false)}
          >
            Inactive
          </button>
          <button 
            onClick={() => setFilterActive(undefined)}
            style={filterButtonStyle(filterActive === undefined)}
          >
            All
          </button>
        </div>

        <button onClick={handleSearch} style={searchButtonStyle}>
          🔍 Search
        </button>
        
        {searchTerm && (
          <button 
            onClick={() => { 
              setSearchTerm(''); 
              setFilterActive(true);
            }} 
            style={{...buttonStyle, backgroundColor: '#6c757d', color: 'white'}}
          >
            Clear Filters
          </button>
        )}
      </div>

      {error && !useMockData && (
        <div style={errorStyle}>
          {error}
        </div>
      )}

      {isLoading ? (
        <div style={loadingStyle}>
          Loading medications...
        </div>
      ) : filteredMedications.length === 0 ? (
        <div style={emptyStateStyle}>
          {searchTerm ? (
            <>
              <h3>No medications found</h3>
              <p>Try adjusting your search or filters, or add a new medication.</p>
            </>
          ) : (
            <>
              <h3>No medications yet</h3>
              <p>Get started by adding your first medication.</p>
              <button onClick={() => setViewMode('add')} style={addButtonStyle}>
                ➕ Add First Medication
              </button>
            </>
          )}
        </div>
      ) : (
        <div>
          <div style={{ marginBottom: '20px', color: '#666' }}>
            Showing {filteredMedications.length} medication{filteredMedications.length !== 1 ? 's' : ''}
            {filterActive !== undefined && ` (${filterActive ? 'Active' : 'Inactive'} only)`}
            {searchTerm && ` matching "${searchTerm}"`}
          </div>
          
          {filteredMedications.map((medication) => (
            <MedicationCard
              key={medication.id}
              medication={medication}
              onEdit={(medication) => {
                setSelectedMedication(medication);
                setViewMode('edit');
              }}
              onDelete={handleDeleteMedication}
              onView={(medication) => {
                setSelectedMedication(medication);
                setViewMode('view');
              }}
              onRecordDose={(medication) => {
                setSelectedMedication(medication);
                setViewMode('record-dose');
              }}
            />
          ))}
        </div>
      )}
    </div>
  );
};