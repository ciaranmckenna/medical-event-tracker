import type { MedicationCatalog } from '../../types/api';

interface MedicationCardProps {
  medication: MedicationCatalog;
  onEdit: (medication: MedicationCatalog) => void;
  onDelete: (medication: MedicationCatalog) => void;
  onView: (medication: MedicationCatalog) => void;
  onRecordDose: (medication: MedicationCatalog) => void;
}

export const MedicationCard: React.FC<MedicationCardProps> = ({
  medication,
  onEdit,
  onDelete,
  onView,
  onRecordDose
}) => {
  const formatMedicationDisplay = () => {
    const parts = [medication.name];
    if (medication.strength && medication.unit) {
      parts.push(`${medication.strength}${medication.unit}`);
    }
    return parts.join(' ');
  };

  const formatTypeDisplay = (type: string) => {
    return type.charAt(0) + type.slice(1).toLowerCase().replace('_', ' ');
  };

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
    backgroundColor: medication.active ? '#28a745' : '#6c757d'
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
          <h3 style={nameStyle}>💊 {formatMedicationDisplay()}</h3>
          <div style={statusBadgeStyle}>{medication.active ? 'Active' : 'Inactive'}</div>
        </div>
        <div>
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
          <strong>Type:</strong> {formatTypeDisplay(medication.type)}
        </div>
        {medication.genericName && (
          <div style={infoItemStyle}>
            <strong>Generic:</strong> {medication.genericName}
          </div>
        )}
        {medication.manufacturer && (
          <div style={infoItemStyle}>
            <strong>Manufacturer:</strong> {medication.manufacturer}
          </div>
        )}
      </div>

      {medication.description && (
        <div style={{ marginTop: '10px' }}>
          <div style={{ fontSize: '14px', color: '#666' }}>
            <strong>Description:</strong> {medication.description.length > 100 
              ? `${medication.description.substring(0, 100)}...` 
              : medication.description
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

      {!medication.active && (
        <div style={{ 
          position: 'absolute',
          top: '10px',
          right: '10px',
          backgroundColor: 'rgba(108, 117, 125, 0.1)',
          color: '#6c757d',
          padding: '5px 10px',
          borderRadius: '4px',
          fontSize: '12px',
          fontWeight: 'bold'
        }}>
          INACTIVE
        </div>
      )}
    </div>
  );
};