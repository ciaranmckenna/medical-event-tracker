import { useState } from 'react';
import type { MedicalEvent } from '../../types/api';
import { medicalEventService } from '../../services/api/medicalEventService';
import { patientService } from '../../services/api/patientService';
import { 
  EVENT_TYPE_DISPLAY, 
  SEIZURE_TYPE_DISPLAY, 
  SEVERITY_DISPLAY 
} from '../../services/validation/medicalEventValidation';

interface MedicalEventCardProps {
  event: MedicalEvent;
  patientName?: string;
  onEdit: (event: MedicalEvent) => void;
  onDelete: (eventId: string) => void;
  showPatient?: boolean;
  compact?: boolean;
}

export const MedicalEventCard: React.FC<MedicalEventCardProps> = ({
  event,
  patientName,
  onEdit,
  onDelete,
  showPatient = true,
  compact = false
}) => {
  const [showDetails, setShowDetails] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const eventTypeDisplay = EVENT_TYPE_DISPLAY[event.type];
  const severityDisplay = SEVERITY_DISPLAY[event.severity];
  const seizureDisplay = event.seizureType ? SEIZURE_TYPE_DISPLAY[event.seizureType] : null;

  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this medical event? This action cannot be undone.')) {
      return;
    }

    setIsDeleting(true);
    try {
      await onDelete(event.id);
    } catch (error) {
      console.error('Failed to delete event:', error);
      alert('Failed to delete event. Please try again.');
    } finally {
      setIsDeleting(false);
    }
  };

  const formatEventTime = (timestamp: string) => {
    const date = new Date(timestamp);
    return {
      date: date.toLocaleDateString('en-GB'),
      time: date.toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' })
    };
  };

  const { date, time } = formatEventTime(event.eventTimestamp);
  const timeSince = medicalEventService.formatTimeSince(event.eventTimestamp);
  const duration = medicalEventService.formatDuration(event.duration);

  // Card styles
  const cardStyle = {
    backgroundColor: 'white',
    border: `1px solid ${severityDisplay.color}20`,
    borderLeft: `4px solid ${severityDisplay.color}`,
    borderRadius: '8px',
    padding: compact ? '12px' : '16px',
    marginBottom: '12px',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
    transition: 'all 0.2s ease'
  };

  const headerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: compact ? '8px' : '12px'
  };

  const titleStyle = {
    fontSize: compact ? '16px' : '18px',
    fontWeight: '600',
    color: '#1f2937',
    margin: '0',
    display: 'flex',
    alignItems: 'center',
    gap: '8px'
  };

  const metaStyle = {
    fontSize: '13px',
    color: '#6b7280',
    margin: '4px 0'
  };

  const badgeStyle = (color: string, size: 'small' | 'medium' = 'medium') => ({
    backgroundColor: `${color}15`,
    color: color,
    border: `1px solid ${color}30`,
    borderRadius: '12px',
    padding: size === 'small' ? '2px 8px' : '4px 12px',
    fontSize: size === 'small' ? '11px' : '12px',
    fontWeight: '500',
    display: 'inline-block'
  });

  const buttonStyle = (variant: 'primary' | 'secondary' | 'danger' = 'secondary') => {
    const variants = {
      primary: { backgroundColor: '#2563eb', color: 'white', border: 'none' },
      secondary: { backgroundColor: 'white', color: '#374151', border: '1px solid #d1d5db' },
      danger: { backgroundColor: '#dc2626', color: 'white', border: 'none' }
    };

    return {
      ...variants[variant],
      padding: '6px 12px',
      borderRadius: '6px',
      fontSize: '14px',
      cursor: isDeleting && variant === 'danger' ? 'not-allowed' : 'pointer',
      opacity: isDeleting && variant === 'danger' ? 0.6 : 1,
      transition: 'all 0.2s ease'
    };
  };

  const actionsStyle = {
    display: 'flex',
    gap: '8px',
    alignItems: 'center'
  };

  const detailsStyle = {
    marginTop: '12px',
    padding: '12px',
    backgroundColor: '#f9fafb',
    borderRadius: '6px',
    fontSize: '14px',
    lineHeight: '1.5'
  };

  const gridStyle = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
    gap: '8px',
    marginTop: '8px'
  };

  return (
    <div style={cardStyle}>
      <div style={headerStyle}>
        <div>
          <h3 style={titleStyle}>
            <span style={{ fontSize: '18px' }}>{eventTypeDisplay.icon}</span>
            {event.title}
          </h3>
          
          {showPatient && patientName && (
            <div style={metaStyle}>
              <strong>Patient:</strong> {patientName}
            </div>
          )}
          
          <div style={metaStyle}>
            <strong>{date}</strong> at <strong>{time}</strong> ({timeSince})
          </div>
          
          <div style={{ display: 'flex', gap: '8px', marginTop: '8px', flexWrap: 'wrap' }}>
            <span style={badgeStyle(eventTypeDisplay.color, 'small')}>
              {eventTypeDisplay.label}
            </span>
            <span style={badgeStyle(severityDisplay.color, 'small')}>
              {severityDisplay.label}
            </span>
            {seizureDisplay && (
              <span style={badgeStyle('#8b5cf6', 'small')}>
                {seizureDisplay.label}
              </span>
            )}
            {event.duration && (
              <span style={badgeStyle('#10b981', 'small')}>
                {duration}
              </span>
            )}
          </div>
        </div>

        <div style={actionsStyle}>
          {!compact && (
            <button
              onClick={() => setShowDetails(!showDetails)}
              style={buttonStyle('secondary')}
            >
              {showDetails ? 'Hide' : 'Details'}
            </button>
          )}
          
          <button
            onClick={() => onEdit(event)}
            style={buttonStyle('primary')}
          >
            Edit
          </button>
          
          <button
            onClick={handleDelete}
            disabled={isDeleting}
            style={buttonStyle('danger')}
          >
            {isDeleting ? 'Deleting...' : 'Delete'}
          </button>
        </div>
      </div>

      {!compact && (
        <div style={{ fontSize: '14px', color: '#4b5563', lineHeight: '1.5' }}>
          {event.description.length > 150 && !showDetails
            ? `${event.description.substring(0, 150)}...`
            : event.description
          }
        </div>
      )}

      {showDetails && !compact && (
        <div style={detailsStyle}>
          <div style={gridStyle}>
            {event.location && (
              <div>
                <strong>Location:</strong><br />
                {event.location}
              </div>
            )}
            
            {event.triggers && event.triggers.length > 0 && (
              <div>
                <strong>Triggers:</strong><br />
                {event.triggers.join(', ')}
              </div>
            )}
            
            {event.medicationGiven && (
              <div>
                <strong>Medication:</strong><br />
                {event.medicationGiven}
                {event.dosageGiven && ` (${event.dosageGiven}mg)`}
              </div>
            )}
            
            {event.witnessedBy && event.witnessedBy.length > 0 && (
              <div>
                <strong>Witnessed by:</strong><br />
                {event.witnessedBy.join(', ')}
              </div>
            )}
            
            <div>
              <strong>Status:</strong><br />
              <span style={badgeStyle(
                event.status === 'RESOLVED' ? '#10b981' : 
                event.status === 'ACTIVE' ? '#f59e0b' : '#6b7280',
                'small'
              )}>
                {event.status.replace('_', ' ')}
              </span>
            </div>

            <div>
              <strong>Emergency Response:</strong><br />
              {event.emergencyContactCalled ? '✅ Contact called' : '❌ No contact'}<br />
              {event.hospitalRequired ? '🏥 Hospital visit' : '🏠 Treated at home'}
            </div>
          </div>

          {event.notes && (
            <div style={{ marginTop: '12px' }}>
              <strong>Notes:</strong><br />
              <div style={{ 
                fontStyle: 'italic', 
                padding: '8px', 
                backgroundColor: 'white', 
                borderRadius: '4px',
                marginTop: '4px'
              }}>
                {event.notes}
              </div>
            </div>
          )}

          <div style={{ 
            marginTop: '12px', 
            fontSize: '12px', 
            color: '#9ca3af',
            borderTop: '1px solid #e5e7eb',
            paddingTop: '8px'
          }}>
            <div>Reported by: {event.reportedBy}</div>
            <div>Created: {new Date(event.createdAt).toLocaleString('en-GB')}</div>
            {event.updatedAt !== event.createdAt && (
              <div>Updated: {new Date(event.updatedAt).toLocaleString('en-GB')}</div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};