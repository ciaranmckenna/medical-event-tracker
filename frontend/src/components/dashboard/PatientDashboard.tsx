import { useState, useEffect, useMemo } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { Card } from '../ui/Card';
import { StatCard } from '../ui/StatCard';
import { FilterGroup } from '../ui/FilterGroup';
import { PatientCard } from '../medical/PatientCard';
import { patientService } from '../../services/api/patientService';
import type { Patient, PaginatedResponse } from '../../types/api';

interface PatientDashboardProps {
  onAddPatient: () => void;
  onEditPatient: (patient: Patient) => void;
  onViewPatient: (patient: Patient) => void;
  onDeletePatient: (patient: Patient) => void;
}

interface PatientStats {
  total: number;
  children: number; // < 18
  adults: number;   // 18-65
  seniors: number;  // > 65
  recentlyAdded: number; // last 30 days
}

export const PatientDashboard: React.FC<PatientDashboardProps> = ({
  onAddPatient,
  onEditPatient,
  onViewPatient,
  onDeletePatient,
}) => {
  const { user, isPrimaryUser, isSecondaryUser } = useAuth();
  const [patients, setPatients] = useState<Patient[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedAgeGroup, setSelectedAgeGroup] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    loadPatients();
  }, []);

  const loadPatients = async () => {
    setIsLoading(true);
    setError('');
    try {
      const response: PaginatedResponse<Patient> = await patientService.getPatients({
        searchTerm: searchTerm || undefined
      });
      setPatients(response.content);
    } catch (error: any) {
      setError('Failed to load patients. Please try again.');
      console.error('Failed to load patients:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSearch = async () => {
    await loadPatients();
  };

  const stats: PatientStats = useMemo(() => {
    const now = new Date();
    const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);

    return patients.reduce((acc, patient) => {
      acc.total++;
      
      const age = patientService.calculateAge(patient.dateOfBirth);
      if (age < 18) acc.children++;
      else if (age <= 65) acc.adults++;
      else acc.seniors++;

      const createdAt = new Date(patient.createdAt);
      if (createdAt >= thirtyDaysAgo) acc.recentlyAdded++;

      return acc;
    }, { total: 0, children: 0, adults: 0, seniors: 0, recentlyAdded: 0 });
  }, [patients]);

  const filteredPatients = useMemo(() => {
    return patients.filter(patient => {
      // Search filter
      if (searchTerm) {
        const searchLower = searchTerm.toLowerCase();
        const matchesSearch = (
          patient.firstName.toLowerCase().includes(searchLower) ||
          patient.lastName.toLowerCase().includes(searchLower) ||
          (patient.emergencyContact && patient.emergencyContact.toLowerCase().includes(searchLower))
        );
        if (!matchesSearch) return false;
      }

      // Age group filter
      if (selectedAgeGroup) {
        const age = patientService.calculateAge(patient.dateOfBirth);
        switch (selectedAgeGroup) {
          case 'children':
            return age < 18;
          case 'adults':
            return age >= 18 && age <= 65;
          case 'seniors':
            return age > 65;
          default:
            return true;
        }
      }

      return true;
    });
  }, [patients, searchTerm, selectedAgeGroup]);

  const ageGroupOptions = [
    { label: 'Children', value: 'children', count: stats.children },
    { label: 'Adults', value: 'adults', count: stats.adults },
    { label: 'Seniors', value: 'seniors', count: stats.seniors },
  ];

  const containerStyle: React.CSSProperties = {
    padding: '20px',
    maxWidth: '1400px',
    margin: '0 auto',
  };

  const headerStyle: React.CSSProperties = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '30px',
    flexWrap: 'wrap',
  };

  const statsGridStyle: React.CSSProperties = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
    gap: '20px',
    marginBottom: '30px',
  };

  const contentGridStyle: React.CSSProperties = {
    display: 'grid',
    gridTemplateColumns: '300px 1fr',
    gap: '30px',
    alignItems: 'start',
  };

  const searchBarStyle: React.CSSProperties = {
    display: 'flex',
    gap: '10px',
    marginBottom: '20px',
    alignItems: 'center',
  };

  const inputStyle: React.CSSProperties = {
    padding: '10px',
    border: '1px solid #ddd',
    borderRadius: '4px',
    fontSize: '16px',
    width: '100%',
  };

  const buttonStyle: React.CSSProperties = {
    padding: '10px 20px',
    border: 'none',
    borderRadius: '4px',
    fontSize: '16px',
    cursor: 'pointer',
  };

  const addButtonStyle: React.CSSProperties = {
    ...buttonStyle,
    backgroundColor: '#28a745',
    color: 'white',
  };

  const searchButtonStyle: React.CSSProperties = {
    ...buttonStyle,
    backgroundColor: '#007bff',
    color: 'white',
  };

  const clearButtonStyle: React.CSSProperties = {
    ...buttonStyle,
    backgroundColor: '#6c757d',
    color: 'white',
  };

  const errorStyle: React.CSSProperties = {
    color: '#dc3545',
    backgroundColor: '#f8d7da',
    padding: '10px',
    borderRadius: '4px',
    marginBottom: '20px',
  };

  const loadingStyle: React.CSSProperties = {
    textAlign: 'center',
    padding: '40px',
    fontSize: '18px',
    color: '#666',
  };

  const emptyStateStyle: React.CSSProperties = {
    textAlign: 'center',
    padding: '40px',
    color: '#666',
  };

  const patientsGridStyle: React.CSSProperties = {
    display: 'grid',
    gap: '16px',
  };

  const resultsHeaderStyle: React.CSSProperties = {
    marginBottom: '20px',
    color: '#666',
    fontSize: '14px',
  };

  return (
    <div style={containerStyle}>
      {/* Header */}
      <div style={headerStyle}>
        <div>
          <h1>👥 Patient Dashboard</h1>
          <p style={{ color: '#666', margin: '5px 0 0 0' }}>
            Welcome back, {user?.firstName}! You have {stats.total} patient{stats.total !== 1 ? 's' : ''} under care.
          </p>
        </div>
        {isPrimaryUser() && (
          <button onClick={onAddPatient} style={addButtonStyle}>
            ➕ Add New Patient
          </button>
        )}
      </div>

      {/* Statistics Cards */}
      <div style={statsGridStyle}>
        <StatCard
          title="Total Patients"
          value={stats.total}
          icon="👥"
          subtitle="All patients under care"
          color="primary"
        />
        <StatCard
          title="Children"
          value={stats.children}
          icon="👶"
          subtitle="Under 18 years old"
          color="info"
        />
        <StatCard
          title="Adults"
          value={stats.adults}
          icon="👤"
          subtitle="18-65 years old"
          color="success"
        />
        <StatCard
          title="Seniors"
          value={stats.seniors}
          icon="👴"
          subtitle="Over 65 years old"
          color="warning"
        />
        <StatCard
          title="Recent Additions"
          value={stats.recentlyAdded}
          icon="🆕"
          subtitle="Added in last 30 days"
          color="primary"
        />
      </div>

      {error && <div style={errorStyle}>{error}</div>}

      {/* Main Content */}
      <div style={contentGridStyle}>
        {/* Filters Sidebar */}
        <Card title="🔍 Search & Filters" padding="20px">
          <div style={searchBarStyle}>
            <input
              type="text"
              placeholder="Search patients..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={inputStyle}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            />
          </div>
          
          <div style={{ display: 'flex', gap: '5px', marginBottom: '20px' }}>
            <button onClick={handleSearch} style={searchButtonStyle}>
              Search
            </button>
            {(searchTerm || selectedAgeGroup) && (
              <button 
                onClick={() => { 
                  setSearchTerm(''); 
                  setSelectedAgeGroup('');
                  loadPatients(); 
                }} 
                style={clearButtonStyle}
              >
                Clear
              </button>
            )}
          </div>

          <FilterGroup
            title="Age Groups"
            options={ageGroupOptions}
            selectedValue={selectedAgeGroup}
            onSelectionChange={setSelectedAgeGroup}
            showCounts={true}
          />

          {/* Role-based information */}
          <Card 
            style={{ marginTop: '20px', backgroundColor: '#f8f9fa' }}
            padding="12px"
          >
            <div style={{ fontSize: '12px', color: '#666' }}>
              <strong>Your Role:</strong> {user?.role}<br />
              <strong>Permissions:</strong> {
                isPrimaryUser() ? 'Full access - Create, edit, delete patients' :
                isSecondaryUser() ? 'Read-only access - View patient information' :
                'Administrative access'
              }
            </div>
          </Card>
        </Card>

        {/* Patients List */}
        <Card title="📋 Patient List">
          {isLoading ? (
            <div style={loadingStyle}>Loading patients...</div>
          ) : filteredPatients.length === 0 ? (
            <div style={emptyStateStyle}>
              {searchTerm || selectedAgeGroup ? (
                <>
                  <h3>No patients found</h3>
                  <p>Try adjusting your search or filters, or add a new patient.</p>
                </>
              ) : (
                <>
                  <h3>No patients yet</h3>
                  <p>Get started by adding your first patient.</p>
                  {isPrimaryUser() && (
                    <button onClick={onAddPatient} style={addButtonStyle}>
                      ➕ Add First Patient
                    </button>
                  )}
                </>
              )}
            </div>
          ) : (
            <>
              <div style={resultsHeaderStyle}>
                Showing {filteredPatients.length} of {stats.total} patient{filteredPatients.length !== 1 ? 's' : ''}
                {searchTerm && ` matching "${searchTerm}"`}
                {selectedAgeGroup && ` in age group "${ageGroupOptions.find(o => o.value === selectedAgeGroup)?.label}"`}
              </div>
              
              <div style={patientsGridStyle}>
                {filteredPatients.map((patient) => (
                  <PatientCard
                    key={patient.id}
                    patient={patient}
                    onEdit={isPrimaryUser() ? onEditPatient : undefined}
                    onDelete={isPrimaryUser() ? onDeletePatient : undefined}
                    onView={onViewPatient}
                  />
                ))}
              </div>
            </>
          )}
        </Card>
      </div>
    </div>
  );
};