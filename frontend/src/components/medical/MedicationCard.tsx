import type { Medication } from '../../types/api';
import { medicationService } from '../../services/api/medicationService';

interface MedicationCardProps {
  medication: Medication;
  onEdit: (medication: Medication) => void;
  onDelete: (medication: Medication) => void;
  onView: (medication: Medication) => void;
  onRecordDose: (medication: Medication) => void;
}

export const MedicationCard: React.FC<MedicationCardProps> = ({
  medication,
  onEdit,
  onDelete,
  onView,
  onRecordDose
}) => {
  const status = medicationService.getMedicationStatus(medication);
  const isActive = medicationService.isActiveMedication(medication);
  const daysRemaining = medicationService.calculateDaysRemaining(medication);
  const medicationDisplay = medicationService.formatMedicationDisplay(medication);
  const frequencyDisplay = medicationService.formatFrequencyDisplay(medication.frequency);

  const cardStyle = {
    border: '1px solid #ddd',
    borderRadius: '8px',
    padding: '20px',
    margin: '10px 0',
    backgroundColor: 'white',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
    transition: 'box-shadow 0.2s ease',
    position: 'relative' as const
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

  const statusBadgeStyle = {
    padding: '4px 8px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: 
      status === 'Active' ? '#28a745' :
      status === 'Inactive' ? '#6c757d' :
      status === 'Expired' ? '#dc3545' :
      '#ffc107' // Future
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

  const recordButtonStyle = {
    ...buttonStyle,
    backgroundColor: '#007bff',
    color: 'white'
  };

  const viewButtonStyle = {
    ...buttonStyle,
    backgroundColor: '#17a2b8',
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
    if (window.confirm(`Are you sure you want to delete ${medication.name}? This action cannot be undone.`)) {
      onDelete(medication);
    }
  };

  return (
    <div style={cardStyle}>
      <div style={headerStyle}>
        <div>
          <h3 style={nameStyle}>💊 {medicationDisplay}</h3>
          <div style={statusBadgeStyle}>{status}</div>
        </div>
        <div>
          {isActive && (
            <button onClick={() => onRecordDose(medication)} style={recordButtonStyle}>
              📝 Record Dose
            </button>
          )}
          <button onClick={() => onView(medication)} style={viewButtonStyle}>
            👁️ View
          </button>
          <button onClick={() => onEdit(medication)} style={editButtonStyle}>
            ✏️ Edit
          </button>
          <button onClick={handleDelete} style={deleteButtonStyle}>
            🗑️ Delete
          </button>
        </div>
      </div>

      <div style={infoRowStyle}>
        <div style={infoItemStyle}>
          <strong>Frequency:</strong> {frequencyDisplay}
        </div>
        <div style={infoItemStyle}>
          <strong>Started:</strong> {new Date(medication.startDate).toLocaleDateString()}
        </div>
        {medication.endDate && (
          <div style={infoItemStyle}>
            <strong>Ends:</strong> {new Date(medication.endDate).toLocaleDateString()}
            {daysRemaining !== null && (
              <span style={{ 
                color: daysRemaining <= 7 ? '#dc3545' : daysRemaining <= 30 ? '#ffc107' : '#28a745',
                marginLeft: '5px'
              }}>
                ({daysRemaining > 0 ? `${daysRemaining} days left` : 'Expired'})
              </span>
            )}
          </div>
        )}
      </div>

      {medication.notes && (
        <div style={{ marginTop: '10px' }}>
          <div style={{ fontSize: '14px', color: '#666' }}>
            <strong>Notes:</strong> {medication.notes.length > 100 
              ? `${medication.notes.substring(0, 100)}...` 
              : medication.notes
            }
          </div>
        </div>
      )}

      <div style={{ marginTop: '10px', fontSize: '12px', color: '#999' }}>
        Created: {new Date(medication.createdAt).toLocaleDateString()}
        {medication.updatedAt !== medication.createdAt && (
          <span> • Updated: {new Date(medication.updatedAt).toLocaleDateString()}</span>
        )}
      </div>

      {!isActive && status !== 'Future' && (
        <div style={{ 
          position: 'absolute',
          top: '10px',
          right: '10px',
          backgroundColor: 'rgba(220, 53, 69, 0.1)',
          color: '#dc3545',
          padding: '5px 10px',
          borderRadius: '4px',
          fontSize: '12px',
          fontWeight: 'bold'
        }}>
          {status === 'Expired' ? 'EXPIRED' : 'INACTIVE'}
        </div>
      )}
    </div>
  );
};