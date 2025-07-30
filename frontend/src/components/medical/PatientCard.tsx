import type { Patient } from '../../types/api';
import { patientService } from '../../services/api/patientService';

interface PatientCardProps {
  patient: Patient;
  onEdit?: (patient: Patient) => void;
  onDelete?: (patient: Patient) => void;
  onView?: (patient: Patient) => void;
}

export const PatientCard: React.FC<PatientCardProps> = ({
  patient,
  onEdit,
  onDelete,
  onView
}) => {
  const age = patientService.calculateAge(patient.dateOfBirth);
  const patientName = patientService.formatPatientName(patient);

  const cardStyle = {
    border: '1px solid #ddd',
    borderRadius: '8px',
    padding: '20px',
    margin: '10px 0',
    backgroundColor: 'white',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
    transition: 'box-shadow 0.2s ease'
  };

  const headerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '15px'
  };

  const nameStyle = {
    fontSize: '18px',
    fontWeight: 'bold',
    color: '#333',
    margin: 0
  };

  const infoRowStyle = {
    display: 'flex',
    gap: '20px',
    marginBottom: '10px',
    flexWrap: 'wrap' as const
  };

  const infoItemStyle = {
    fontSize: '14px',
    color: '#666'
  };

  const buttonStyle = {
    padding: '6px 12px',
    margin: '0 5px',
    border: 'none',
    borderRadius: '4px',
    fontSize: '12px',
    cursor: 'pointer',
    textDecoration: 'none'
  };

  const viewButtonStyle = {
    ...buttonStyle,
    backgroundColor: '#007bff',
    color: 'white'
  };

  const editButtonStyle = {
    ...buttonStyle,
    backgroundColor: '#28a745',
    color: 'white'
  };

  const deleteButtonStyle = {
    ...buttonStyle,
    backgroundColor: '#dc3545',
    color: 'white'
  };

  const handleDelete = () => {
    if (onDelete && window.confirm(`Are you sure you want to delete ${patientName}? This action cannot be undone.`)) {
      onDelete(patient);
    }
  };

  return (
    <div style={cardStyle}>
      <div style={headerStyle}>
        <h3 style={nameStyle}>👤 {patientName}</h3>
        <div>
          {onView && (
            <button onClick={() => onView(patient)} style={viewButtonStyle}>
              👁️ View
            </button>
          )}
          {onEdit && (
            <button onClick={() => onEdit(patient)} style={editButtonStyle}>
              ✏️ Edit
            </button>
          )}
          {onDelete && (
            <button onClick={handleDelete} style={deleteButtonStyle}>
              🗑️ Delete
            </button>
          )}
        </div>
      </div>

      <div style={infoRowStyle}>
        <div style={infoItemStyle}>
          <strong>Age:</strong> {age} years old
        </div>
        <div style={infoItemStyle}>
          <strong>DOB:</strong> {new Date(patient.dateOfBirth).toLocaleDateString()}
        </div>
        {patient.height && (
          <div style={infoItemStyle}>
            <strong>Height:</strong> {patient.height} cm
          </div>
        )}
        {patient.weight && (
          <div style={infoItemStyle}>
            <strong>Weight:</strong> {patient.weight} kg
          </div>
        )}
      </div>

      {patient.emergencyContact && (
        <div style={infoRowStyle}>
          <div style={infoItemStyle}>
            <strong>Emergency Contact:</strong> {patient.emergencyContact}
            {patient.emergencyPhone && ` (${patient.emergencyPhone})`}
          </div>
        </div>
      )}

      {patient.notes && (
        <div style={{ marginTop: '10px' }}>
          <div style={{ fontSize: '14px', color: '#666' }}>
            <strong>Notes:</strong> {patient.notes.length > 100 
              ? `${patient.notes.substring(0, 100)}...` 
              : patient.notes
            }
          </div>
        </div>
      )}

      <div style={{ marginTop: '10px', fontSize: '12px', color: '#999' }}>
        Created: {new Date(patient.createdAt).toLocaleDateString()}
        {patient.updatedAt !== patient.createdAt && (
          <span> • Updated: {new Date(patient.updatedAt).toLocaleDateString()}</span>
        )}
      </div>
    </div>
  );
};