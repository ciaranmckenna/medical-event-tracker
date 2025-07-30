import { useState } from 'react';
import type { Medication } from '../../types/api';
import { patientService } from '../../services/api/patientService';

interface DosageRecord {
  id: string;
  medicationId: string;
  scheduledTime: string;
  administeredTime?: string;
  dosageAmount: number;
  schedule: 'AM' | 'PM';
  administered: boolean;
  notes?: string;
  administeredBy?: string;
  patientId: string;
  createdAt: string;
}

interface DosageCardProps {
  medication: Medication;
  patientName: string;
  dosageRecords: DosageRecord[];
  onAdminister: (medicationId: string, schedule: 'AM' | 'PM', dosage: number, notes?: string) => Promise<void>;
  onEdit: (medication: Medication) => void;
  canAdminister?: boolean;
}

export const DosageCard: React.FC<DosageCardProps> = ({
  medication,
  patientName,
  dosageRecords,
  onAdminister,
  onEdit,
  canAdminister = true
}) => {
  const [showDetails, setShowDetails] = useState(false);
  const [administeringSchedule, setAdministeringSchedule] = useState<'AM' | 'PM' | null>(null);
  const [dosageNotes, setDosageNotes] = useState('');

  // Get today's dosage records for this medication
  const today = new Date().toISOString().split('T')[0];
  const todayRecords = dosageRecords.filter(record => 
    record.medicationId === medication.id && 
    record.scheduledTime.startsWith(today)
  );

  const amRecord = todayRecords.find(record => record.schedule === 'AM');
  const pmRecord = todayRecords.find(record => record.schedule === 'PM');

  const isAMDue = medication.frequency === 'TWICE_DAILY' || medication.frequency === 'ONCE_DAILY';
  const isPMDue = medication.frequency === 'TWICE_DAILY';

  const handleAdminister = async (schedule: 'AM' | 'PM') => {
    setAdministeringSchedule(schedule);
    try {
      await onAdminister(medication.id, schedule, medication.dosage, dosageNotes);
      setDosageNotes('');
    } catch (error) {
      console.error('Failed to administer medication:', error);
      alert('Failed to record medication administration');
    } finally {
      setAdministeringSchedule(null);
    }
  };

  const getScheduleStatus = (schedule: 'AM' | 'PM') => {
    const record = schedule === 'AM' ? amRecord : pmRecord;
    const isRequired = schedule === 'AM' ? isAMDue : isPMDue;
    
    if (!isRequired) return { status: 'not-required', label: 'Not Required', color: '#9ca3af' };
    if (record?.administered) return { status: 'administered', label: 'Administered', color: '#10b981' };
    
    const now = new Date();
    const currentHour = now.getHours();
    const isOverdue = (schedule === 'AM' && currentHour > 12) || (schedule === 'PM' && currentHour > 22);
    
    if (isOverdue) return { status: 'overdue', label: 'Overdue', color: '#dc2626' };
    return { status: 'pending', label: 'Due', color: '#f59e0b' };
  };

  const amStatus = getScheduleStatus('AM');
  const pmStatus = getScheduleStatus('PM');

  // Calculate adherence percentage for last 7 days
  const sevenDaysAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
  const recentRecords = dosageRecords.filter(record => 
    record.medicationId === medication.id && 
    record.scheduledTime >= sevenDaysAgo &&
    record.administered
  );
  
  const expectedDoses = medication.frequency === 'TWICE_DAILY' ? 14 : 7; // 7 days
  const adherencePercentage = Math.round((recentRecords.length / expectedDoses) * 100);

  // Styles
  const cardStyle = {
    backgroundColor: 'white',
    border: '1px solid #e5e7eb',
    borderRadius: '8px',
    padding: '16px',
    marginBottom: '16px',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
  };

  const headerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: '12px'
  };

  const medicationNameStyle = {
    fontSize: '18px',
    fontWeight: '600',
    color: '#1f2937',
    margin: '0'
  };

  const metaStyle = {
    fontSize: '14px',
    color: '#6b7280',
    margin: '4px 0'
  };

  const scheduleRowStyle = {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '12px',
    marginBottom: '16px'
  };

  const scheduleCardStyle = (status: any) => ({
    padding: '12px',
    borderRadius: '6px',
    border: `1px solid ${status.color}30`,
    backgroundColor: `${status.color}10`
  });

  const statusBadgeStyle = (color: string) => ({
    backgroundColor: `${color}20`,
    color: color,
    padding: '2px 8px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: '500'
  });

  const buttonStyle = (variant: 'primary' | 'secondary' | 'success' = 'primary', disabled = false) => {
    const variants = {
      primary: { backgroundColor: '#2563eb', color: 'white' },
      secondary: { backgroundColor: 'white', color: '#374151', border: '1px solid #d1d5db' },
      success: { backgroundColor: '#10b981', color: 'white' }
    };

    return {
      ...variants[variant],
      padding: '8px 16px',
      borderRadius: '6px',
      border: variant === 'secondary' ? '1px solid #d1d5db' : 'none',
      fontSize: '14px',
      cursor: disabled ? 'not-allowed' : 'pointer',
      opacity: disabled ? 0.6 : 1,
      transition: 'all 0.2s ease'
    };
  };

  const adherenceStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    marginTop: '8px'
  };

  const adherenceBarStyle = {
    width: '100px',
    height: '8px',
    backgroundColor: '#e5e7eb',
    borderRadius: '4px',
    overflow: 'hidden'
  };

  const adherenceProgressStyle = {
    height: '100%',
    backgroundColor: adherencePercentage >= 80 ? '#10b981' : adherencePercentage >= 60 ? '#f59e0b' : '#dc2626',
    width: `${adherencePercentage}%`,
    transition: 'width 0.3s ease'
  };

  return (
    <div style={cardStyle}>
      <div style={headerStyle}>
        <div>
          <h3 style={medicationNameStyle}>💊 {medication.name}</h3>
          <div style={metaStyle}>
            <strong>Patient:</strong> {patientName}
          </div>
          <div style={metaStyle}>
            <strong>Dosage:</strong> {medication.dosage}{medication.unit} • 
            <strong> Frequency:</strong> {medication.frequency.replace('_', ' ').toLowerCase()}
          </div>
          {medication.status === 'ACTIVE' ? (
            <span style={statusBadgeStyle('#10b981')}>Active</span>
          ) : (
            <span style={statusBadgeStyle('#6b7280')}>Inactive</span>
          )}
        </div>

        <div style={{ display: 'flex', gap: '8px' }}>
          <button
            onClick={() => setShowDetails(!showDetails)}
            style={buttonStyle('secondary')}
          >
            {showDetails ? 'Hide' : 'Details'}
          </button>
          <button
            onClick={() => onEdit(medication)}
            style={buttonStyle('primary')}
          >
            Edit
          </button>
        </div>
      </div>

      {/* Today's Schedule */}
      <div style={scheduleRowStyle}>
        {/* AM Schedule */}
        {isAMDue && (
          <div style={scheduleCardStyle(amStatus)}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <strong>🌅 Morning (AM)</strong>
              <span style={statusBadgeStyle(amStatus.color)}>{amStatus.label}</span>
            </div>
            
            {amRecord?.administered ? (
              <div style={{ fontSize: '14px', color: '#6b7280' }}>
                <div>Administered: {new Date(amRecord.administeredTime!).toLocaleTimeString('en-GB')}</div>
                <div>By: {amRecord.administeredBy || 'Unknown'}</div>
                {amRecord.notes && <div>Notes: {amRecord.notes}</div>}
              </div>
            ) : (
              canAdminister && (
                <button
                  onClick={() => handleAdminister('AM')}
                  disabled={administeringSchedule === 'AM'}
                  style={buttonStyle('success', administeringSchedule === 'AM')}
                >
                  {administeringSchedule === 'AM' ? 'Recording...' : 'Mark as Taken'}
                </button>
              )
            )}
          </div>
        )}

        {/* PM Schedule */}
        {isPMDue && (
          <div style={scheduleCardStyle(pmStatus)}>
            <div style={{ display: 'flex', justifyContent: 'space-between', 'alignItems': 'center', marginBottom: '8px' }}>
              <strong>🌙 Evening (PM)</strong>
              <span style={statusBadgeStyle(pmStatus.color)}>{pmStatus.label}</span>
            </div>
            
            {pmRecord?.administered ? (
              <div style={{ fontSize: '14px', color: '#6b7280' }}>
                <div>Administered: {new Date(pmRecord.administeredTime!).toLocaleTimeString('en-GB')}</div>
                <div>By: {pmRecord.administeredBy || 'Unknown'}</div>
                {pmRecord.notes && <div>Notes: {pmRecord.notes}</div>}
              </div>
            ) : (
              canAdminister && (
                <button
                  onClick={() => handleAdminister('PM')}
                  disabled={administeringSchedule === 'PM'}
                  style={buttonStyle('success', administeringSchedule === 'PM')}
                >
                  {administeringSchedule === 'PM' ? 'Recording...' : 'Mark as Taken'}
                </button>
              )
            )}
          </div>
        )}
      </div>

      {/* Adherence */}
      <div style={adherenceStyle}>
        <span style={{ fontSize: '14px', fontWeight: '500' }}>7-day adherence:</span>
        <div style={adherenceBarStyle}>
          <div style={adherenceProgressStyle}></div>
        </div>
        <span style={{ fontSize: '14px', color: adherencePercentage >= 80 ? '#10b981' : '#dc2626' }}>
          {adherencePercentage}%
        </span>
      </div>

      {/* Administration Notes Input */}
      {canAdminister && (
        <div style={{ marginTop: '12px' }}>
          <input
            type="text"
            placeholder="Optional notes for next administration..."
            value={dosageNotes}
            onChange={(e) => setDosageNotes(e.target.value)}
            style={{
              width: '100%',
              padding: '8px 12px',
              border: '1px solid #d1d5db',
              borderRadius: '6px',
              fontSize: '14px'
            }}
          />
        </div>
      )}

      {/* Detailed History */}
      {showDetails && (
        <div style={{
          marginTop: '16px',
          padding: '12px',
          backgroundColor: '#f9fafb',
          borderRadius: '6px'
        }}>
          <h4 style={{ margin: '0 0 12px 0', fontSize: '16px' }}>Recent History (Last 7 Days)</h4>
          
          {recentRecords.length === 0 ? (
            <div style={{ color: '#6b7280', fontSize: '14px' }}>No recent administrations recorded</div>
          ) : (
            <div style={{ display: 'grid', gap: '8px' }}>
              {recentRecords.slice(0, 10).map(record => (
                <div
                  key={record.id}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '8px',
                    backgroundColor: 'white',
                    borderRadius: '4px',
                    fontSize: '14px'
                  }}
                >
                  <div>
                    <strong>{record.schedule}</strong> - {new Date(record.administeredTime!).toLocaleDateString('en-GB')}
                  </div>
                  <div style={{ color: '#6b7280' }}>
                    {new Date(record.administeredTime!).toLocaleTimeString('en-GB')}
                  </div>
                </div>
              ))}
            </div>
          )}

          {medication.notes && (
            <div style={{ marginTop: '12px' }}>
              <strong>Medication Notes:</strong>
              <div style={{ marginTop: '4px', fontStyle: 'italic', color: '#6b7280' }}>
                {medication.notes}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};