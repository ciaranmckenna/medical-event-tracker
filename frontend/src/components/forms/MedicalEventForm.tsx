import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useAuth } from '../../hooks/useAuth';
import { patientService } from '../../services/api/patientService';
import { 
  medicalEventFormSchema, 
  EVENT_TYPE_DISPLAY, 
  SEIZURE_TYPE_DISPLAY,
  SEVERITY_DISPLAY,
  type MedicalEventFormData 
} from '../../services/validation/medicalEventValidation';
import type { Patient, MedicalEvent } from '../../types/api';
import type { EventType, SeizureType, SeverityLevel } from '../../types/medical';

interface MedicalEventFormProps {
  event?: MedicalEvent;
  selectedPatientId?: string;
  onSubmit: (data: MedicalEventFormData) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  emergencyMode?: boolean; // For rapid seizure logging
}

export const MedicalEventForm: React.FC<MedicalEventFormProps> = ({
  event,
  selectedPatientId,
  onSubmit,
  onCancel,
  isLoading = false,
  emergencyMode = false
}) => {
  const { user } = useAuth();
  const [patients, setPatients] = useState<Patient[]>([]);
  const [loadingPatients, setLoadingPatients] = useState(false);
  const [error, setError] = useState<string>('');
  const [isSeizureType, setIsSeizureType] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors }
  } = useForm<MedicalEventFormData>({
    resolver: zodResolver(medicalEventFormSchema),
    defaultValues: event ? {
      patientId: event.patientId,
      type: event.type,
      title: event.title,
      description: event.description,
      seizureType: event.seizureType,
      duration: event.duration,
      severity: event.severity,
      location: event.location,
      triggers: event.triggers?.join(', '),
      medicationGiven: event.medicationGiven,
      dosageGiven: event.dosageGiven,
      emergencyContactCalled: event.emergencyContactCalled,
      hospitalRequired: event.hospitalRequired,
      eventDate: event.eventTimestamp?.split('T')[0] || '',
      eventTime: event.eventTimestamp?.split('T')[1]?.substring(0, 5) || '',
      witnessedBy: event.witnessedBy?.join(', '),
      notes: event.notes,
      // Patient measurements from existing event (if available)
      weightKg: (event as any).weightKg || undefined,
      heightCm: (event as any).heightCm || undefined
    } : {
      patientId: selectedPatientId || '',
      type: emergencyMode ? 'SEIZURE' : 'SYMPTOM',
      severity: 'MODERATE',
      eventDate: new Date().toISOString().split('T')[0],
      eventTime: new Date().toTimeString().substring(0, 5),
      title: '',
      description: '',
      // Default values for new required fields
      weightKg: undefined, // User must enter
      heightCm: undefined, // Optional
      dosageGiven: 0 // Default to 0 (no medication)
    }
  });

  const watchedType = watch('type');

  useEffect(() => {
    setIsSeizureType(watchedType === 'SEIZURE');
    if (watchedType === 'SEIZURE' && !watch('seizureType')) {
      setValue('seizureType', 'UNKNOWN');
    }
  }, [watchedType, setValue, watch]);

  useEffect(() => {
    loadPatients();
  }, []);

  const loadPatients = async () => {
    setLoadingPatients(true);
    try {
      const response = await patientService.getPatients();
      setPatients(response.content);
    } catch (error) {
      console.error('Failed to load patients:', error);
      setError('Failed to load patients');
    } finally {
      setLoadingPatients(false);
    }
  };

  const handleFormSubmit = async (data: MedicalEventFormData) => {
    setError('');
    try {
      await onSubmit(data);
    } catch (error: any) {
      setError(error.response?.data?.message || 'Failed to save medical event');
    }
  };

  const quickSetSeverity = (severity: SeverityLevel) => {
    setValue('severity', severity);
  };

  const quickSetSeizureType = (type: SeizureType) => {
    setValue('seizureType', type);
  };

  // Emergency mode styles for rapid input
  const emergencyStyle = emergencyMode ? {
    backgroundColor: '#fef2f2',
    border: '2px solid #dc2626',
    borderRadius: '8px'
  } : {};

  const containerStyle = {
    maxWidth: emergencyMode ? '600px' : '800px',
    margin: '0 auto',
    padding: '20px',
    ...emergencyStyle
  };

  const headerStyle = {
    textAlign: 'center' as const,
    marginBottom: '30px',
    color: emergencyMode ? '#dc2626' : '#333'
  };

  const formStyle = {
    display: 'grid',
    gap: '20px'
  };

  const rowStyle = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
    gap: '15px'
  };

  const fieldStyle = {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '5px'
  };

  const labelStyle = {
    fontWeight: '600',
    color: '#374151',
    fontSize: '14px'
  };

  const inputStyle = {
    padding: '12px',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    fontSize: '16px',
    fontFamily: 'inherit'
  };

  const selectStyle = {
    ...inputStyle,
    cursor: 'pointer'
  };

  const textareaStyle = {
    ...inputStyle,
    minHeight: '80px',
    resize: 'vertical' as const
  };

  const quickButtonStyle = (active: boolean = false) => ({
    padding: '8px 12px',
    border: active ? '2px solid #2563eb' : '1px solid #d1d5db',
    borderRadius: '6px',
    backgroundColor: active ? '#eff6ff' : 'white',
    cursor: 'pointer',
    fontSize: '12px',
    fontWeight: active ? '600' : '400',
    color: active ? '#2563eb' : '#6b7280'
  });

  const errorStyle = {
    color: '#dc2626',
    fontSize: '14px',
    marginTop: '5px'
  };

  const buttonGroupStyle = {
    display: 'flex',
    gap: '10px',
    justifyContent: 'flex-end',
    marginTop: '30px'
  };

  const cancelButtonStyle = {
    padding: '12px 24px',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    backgroundColor: 'white',
    color: '#374151',
    cursor: 'pointer',
    fontSize: '16px'
  };

  const submitButtonStyle = {
    padding: '12px 24px',
    border: 'none',
    borderRadius: '6px',
    backgroundColor: emergencyMode ? '#dc2626' : '#2563eb',
    color: 'white',
    cursor: isLoading ? 'not-allowed' : 'pointer',
    fontSize: '16px',
    opacity: isLoading ? 0.7 : 1
  };

  return (
    <div style={containerStyle}>
      {emergencyMode && (
        <div style={{
          backgroundColor: '#fee2e2',
          border: '1px solid #fca5a5',
          borderRadius: '6px',
          padding: '12px',
          marginBottom: '20px',
          textAlign: 'center'
        }}>
          <strong>🚨 Emergency Event Logging</strong>
          <p style={{ margin: '5px 0 0 0', fontSize: '14px' }}>
            Rapid seizure documentation mode - essential fields only
          </p>
        </div>
      )}

      <div style={headerStyle}>
        <h2>{event ? '✏️ Edit Medical Event' : '📋 Log Medical Event'}</h2>
        {!emergencyMode && (
          <p style={{ color: '#6b7280', margin: '5px 0 0 0' }}>
            Record medical events with precise timing and details for correlation analysis
          </p>
        )}
      </div>

      {error && (
        <div style={{
          backgroundColor: '#fee2e2',
          border: '1px solid #fca5a5',
          borderRadius: '6px',
          padding: '12px',
          marginBottom: '20px',
          color: '#dc2626'
        }}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit(handleFormSubmit)} style={formStyle}>
        {/* Patient Selection */}
        <div style={fieldStyle}>
          <label style={labelStyle}>Patient *</label>
          <select
            {...register('patientId')}
            style={selectStyle}
            disabled={loadingPatients}
          >
            <option value="">Select patient...</option>
            {patients.map(patient => (
              <option key={patient.id} value={patient.id}>
                {patientService.formatPatientName(patient)} 
                ({patientService.calculateAge(patient.dateOfBirth)} years old)
              </option>
            ))}
          </select>
          {errors.patientId && <div style={errorStyle}>{errors.patientId.message}</div>}
        </div>

        {/* Event Type and Severity Row */}
        <div style={rowStyle}>
          <div style={fieldStyle}>
            <label style={labelStyle}>Event Type *</label>
            <select {...register('type')} style={selectStyle}>
              {Object.entries(EVENT_TYPE_DISPLAY).map(([key, display]) => (
                <option key={key} value={key}>
                  {display.icon} {display.label}
                </option>
              ))}
            </select>
            {errors.type && <div style={errorStyle}>{errors.type.message}</div>}
          </div>

          <div style={fieldStyle}>
            <label style={labelStyle}>Severity *</label>
            <select {...register('severity')} style={selectStyle}>
              {Object.entries(SEVERITY_DISPLAY).map(([key, display]) => (
                <option key={key} value={key}>
                  {display.label} - {display.description}
                </option>
              ))}
            </select>
            {!emergencyMode && (
              <div style={{ display: 'flex', gap: '5px', marginTop: '5px' }}>
                {Object.entries(SEVERITY_DISPLAY).map(([key, display]) => (
                  <button
                    key={key}
                    type="button"
                    onClick={() => quickSetSeverity(key as SeverityLevel)}
                    style={{
                      ...quickButtonStyle(watch('severity') === key),
                      backgroundColor: display.color + '20',
                      borderColor: display.color
                    }}
                  >
                    {display.label}
                  </button>
                ))}
              </div>
            )}
            {errors.severity && <div style={errorStyle}>{errors.severity.message}</div>}
          </div>
        </div>

        {/* Seizure-specific fields */}
        {isSeizureType && (
          <div style={rowStyle}>
            <div style={fieldStyle}>
              <label style={labelStyle}>Seizure Type *</label>
              <select {...register('seizureType')} style={selectStyle}>
                {Object.entries(SEIZURE_TYPE_DISPLAY).map(([key, display]) => (
                  <option key={key} value={key}>
                    {display.label}
                  </option>
                ))}
              </select>
              {!emergencyMode && (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '5px', marginTop: '5px' }}>
                  {['TONIC_CLONIC', 'FOCAL_AWARE', 'ABSENCE', 'UNKNOWN'].map((type) => (
                    <button
                      key={type}
                      type="button"
                      onClick={() => quickSetSeizureType(type as SeizureType)}
                      style={quickButtonStyle(watch('seizureType') === type)}
                    >
                      {SEIZURE_TYPE_DISPLAY[type as SeizureType].label.split(' ')[0]}
                    </button>
                  ))}
                </div>
              )}
              {errors.seizureType && <div style={errorStyle}>{errors.seizureType.message}</div>}
            </div>

            <div style={fieldStyle}>
              <label style={labelStyle}>Duration (seconds)</label>
              <input
                type="number"
                step="0.5"
                min="0.5"
                max="86400"
                {...register('duration', { valueAsNumber: true })}
                style={inputStyle}
                placeholder="e.g., 120 (2 minutes)"
              />
              <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '2px' }}>
                Common: Tonic-clonic 60-180s, Absence 5-30s, Focal 30-120s
              </div>
              {errors.duration && <div style={errorStyle}>{errors.duration.message}</div>}
            </div>
          </div>
        )}

        {/* Title and Description */}
        <div style={fieldStyle}>
          <label style={labelStyle}>Event Title *</label>
          <input
            type="text"
            {...register('title')}
            style={inputStyle}
            placeholder={isSeizureType ? "e.g., Morning tonic-clonic seizure" : "Brief description of the event"}
          />
          {errors.title && <div style={errorStyle}>{errors.title.message}</div>}
        </div>

        <div style={fieldStyle}>
          <label style={labelStyle}>Description *</label>
          <textarea
            {...register('description')}
            style={textareaStyle}
            placeholder={isSeizureType ? 
              "Describe the seizure: what you observed, how it started, patient's condition, recovery time..." :
              "Detailed description of the medical event..."
            }
          />
          {errors.description && <div style={errorStyle}>{errors.description.message}</div>}
        </div>

        {/* Timing Row */}
        <div style={rowStyle}>
          <div style={fieldStyle}>
            <label style={labelStyle}>Event Date *</label>
            <input
              type="date"
              {...register('eventDate')}
              style={inputStyle}
              max={new Date().toISOString().split('T')[0]}
            />
            {errors.eventDate && <div style={errorStyle}>{errors.eventDate.message}</div>}
          </div>

          <div style={fieldStyle}>
            <label style={labelStyle}>Event Time *</label>
            <input
              type="time"
              {...register('eventTime')}
              style={inputStyle}
            />
            {errors.eventTime && <div style={errorStyle}>{errors.eventTime.message}</div>}
          </div>
        </div>

        {/* Patient Measurements at Time of Event (MVP Stage 3 Requirements) */}
        <div style={{
          backgroundColor: '#f0f9ff',
          border: '1px solid #bae6fd',
          borderRadius: '6px',
          padding: '15px',
          marginTop: '10px'
        }}>
          <div style={{ marginBottom: '10px', fontWeight: '600', color: '#0369a1' }}>
            📊 Patient Measurements at Time of Event
          </div>
          <div style={{ fontSize: '12px', color: '#64748b', marginBottom: '15px' }}>
            Record patient's current measurements for dosage correlation analysis
          </div>

          <div style={rowStyle}>
            <div style={fieldStyle}>
              <label style={labelStyle}>Patient Weight (kg) *</label>
              <input
                type="number"
                step="0.1"
                min="0.1"
                max="1000"
                {...register('weightKg', { valueAsNumber: true })}
                style={inputStyle}
                placeholder="e.g., 70.5"
              />
              <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '2px' }}>
                Weight impacts medication dosage calculations
              </div>
              {errors.weightKg && <div style={errorStyle}>{errors.weightKg.message}</div>}
            </div>

            <div style={fieldStyle}>
              <label style={labelStyle}>Patient Height (cm)</label>
              <input
                type="number"
                step="0.1"
                min="0.1"
                max="300"
                {...register('heightCm', { valueAsNumber: true })}
                style={inputStyle}
                placeholder="e.g., 175.0"
              />
              <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '2px' }}>
                Optional for patients over 20 years old
              </div>
              {errors.heightCm && <div style={errorStyle}>{errors.heightCm.message}</div>}
            </div>
          </div>

        </div>

        {/* Additional fields (collapsed in emergency mode) */}
        {!emergencyMode && (
          <>
            <div style={rowStyle}>
              <div style={fieldStyle}>
                <label style={labelStyle}>Location</label>
                <input
                  type="text"
                  {...register('location')}
                  style={inputStyle}
                  placeholder="e.g., bedroom, school, playground"
                />
                {errors.location && <div style={errorStyle}>{errors.location.message}</div>}
              </div>

              <div style={fieldStyle}>
                <label style={labelStyle}>Possible Triggers</label>
                <input
                  type="text"
                  {...register('triggers')}
                  style={inputStyle}
                  placeholder="e.g., missed medication, stress, flashing lights"
                />
                <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '2px' }}>
                  Separate multiple triggers with commas
                </div>
                {errors.triggers && <div style={errorStyle}>{errors.triggers.message}</div>}
              </div>
            </div>

            <div style={rowStyle}>
              <div style={fieldStyle}>
                <label style={labelStyle}>Medication Given</label>
                <input
                  type="text"
                  {...register('medicationGiven')}
                  style={inputStyle}
                  placeholder="e.g., Emergency Diazepam"
                />
                {errors.medicationGiven && <div style={errorStyle}>{errors.medicationGiven.message}</div>}
              </div>

              <div style={fieldStyle}>
                <label style={labelStyle}>Dosage Given *</label>
                <input
                  type="number"
                  step="0.1"
                  min="0"
                  max="10000"
                  {...register('dosageGiven', { valueAsNumber: true })}
                  style={inputStyle}
                  placeholder="Enter 0 if no medication was given"
                />
                <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '2px' }}>
                  Enter the dosage amount (use 0 if no medication was given during this event)
                </div>
                {errors.dosageGiven && <div style={errorStyle}>{errors.dosageGiven.message}</div>}
              </div>
            </div>

            <div style={rowStyle}>
              <div style={fieldStyle}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <input
                    type="checkbox"
                    {...register('emergencyContactCalled')}
                    id="emergencyContact"
                  />
                  <label htmlFor="emergencyContact" style={labelStyle}>
                    Emergency contact called
                  </label>
                </div>
              </div>

              <div style={fieldStyle}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <input
                    type="checkbox"
                    {...register('hospitalRequired')}
                    id="hospitalRequired"
                  />
                  <label htmlFor="hospitalRequired" style={labelStyle}>
                    Hospital visit required
                  </label>
                </div>
              </div>
            </div>

            <div style={fieldStyle}>
              <label style={labelStyle}>Witnessed By</label>
              <input
                type="text"
                {...register('witnessedBy')}
                style={inputStyle}
                placeholder="Names of people who witnessed the event"
              />
              {errors.witnessedBy && <div style={errorStyle}>{errors.witnessedBy.message}</div>}
            </div>

            <div style={fieldStyle}>
              <label style={labelStyle}>Additional Notes</label>
              <textarea
                {...register('notes')}
                style={textareaStyle}
                placeholder="Any other relevant information, recovery details, follow-up actions..."
              />
              {errors.notes && <div style={errorStyle}>{errors.notes.message}</div>}
            </div>
          </>
        )}

        <div style={buttonGroupStyle}>
          <button type="button" onClick={onCancel} style={cancelButtonStyle}>
            Cancel
          </button>
          <button 
            type="submit" 
            disabled={isLoading}
            style={submitButtonStyle}
          >
            {isLoading ? 'Saving...' : (event ? 'Update Event' : 'Log Event')}
          </button>
        </div>
      </form>
    </div>
  );
};