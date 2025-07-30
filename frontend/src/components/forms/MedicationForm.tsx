import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { medicationSchema, type MedicationFormData, getFrequencyDisplayName, getFrequencyDescription } from '../../services/validation/medicationValidation';
import { patientService } from '../../services/api/patientService';
import type { Medication, Patient } from '../../types/api';

interface MedicationFormProps {
  medication?: Medication;
  selectedPatientId?: string;
  onSubmit: (data: MedicationFormData) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
}

const FREQUENCY_OPTIONS = [
  'ONCE_DAILY',
  'TWICE_DAILY', 
  'THREE_TIMES_DAILY',
  'AS_NEEDED'
] as const;

const COMMON_UNITS = [
  'mg', 'g', 'ml', 'tablets', 'capsules', 'drops', 'puffs', 'patches', 'units'
];

export const MedicationForm: React.FC<MedicationFormProps> = ({
  medication,
  selectedPatientId,
  onSubmit,
  onCancel,
  isLoading = false
}) => {
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [patients, setPatients] = useState<Patient[]>([]);
  const [loadingPatients, setLoadingPatients] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    watch,
    setValue
  } = useForm<MedicationFormData>({
    resolver: zodResolver(medicationSchema),
    defaultValues: medication ? {
      patientId: medication.patientId,
      name: medication.name,
      dosage: medication.dosage,
      unit: medication.unit,
      frequency: medication.frequency,
      startDate: medication.startDate,
      endDate: medication.endDate || '',
      notes: medication.notes || ''
    } : {
      patientId: selectedPatientId || '',
      name: '',
      dosage: 0,
      unit: '',
      frequency: 'ONCE_DAILY' as const,
      startDate: new Date().toISOString().split('T')[0], // Today's date
      endDate: '',
      notes: ''
    }
  });

  const selectedFrequency = watch('frequency');

  // Load patients for dropdown
  useEffect(() => {
    const loadPatients = async () => {
      setLoadingPatients(true);
      try {
        const response = await patientService.getPatients();
        setPatients(response.content);
      } catch (error) {
        console.error('Failed to load patients:', error);
      } finally {
        setLoadingPatients(false);
      }
    };

    loadPatients();
  }, []);

  const handleFormSubmit = async (data: MedicationFormData) => {
    setErrorMessage('');
    try {
      await onSubmit(data);
    } catch (error: any) {
      setErrorMessage(
        error.response?.data?.message || 
        'Failed to save medication. Please try again.'
      );
    }
  };

  const formStyle = {
    maxWidth: '600px',
    margin: '0 auto',
    padding: '30px',
    border: '1px solid #ddd',
    borderRadius: '8px',
    backgroundColor: '#f8f9fa'
  };

  const inputStyle = {
    width: '100%',
    padding: '12px',
    margin: '8px 0',
    border: '1px solid #ddd',
    borderRadius: '4px',
    fontSize: '16px'
  };

  const selectStyle = {
    ...inputStyle,
    cursor: 'pointer'
  };

  const buttonStyle = {
    padding: '12px 24px',
    margin: '0 10px',
    border: 'none',
    borderRadius: '4px',
    fontSize: '16px',
    cursor: isLoading ? 'not-allowed' : 'pointer',
    opacity: isLoading ? 0.7 : 1
  };

  const saveButtonStyle = {
    ...buttonStyle,
    backgroundColor: '#28a745',
    color: 'white'
  };

  const cancelButtonStyle = {
    ...buttonStyle,
    backgroundColor: '#6c757d',
    color: 'white'
  };

  const errorStyle = {
    color: '#dc3545',
    fontSize: '14px',
    marginTop: '4px'
  };

  const rowStyle = {
    display: 'flex',
    gap: '15px',
    marginBottom: '20px'
  };

  const halfWidthStyle = {
    flex: 1,
    textAlign: 'left' as const
  };

  const helpTextStyle = {
    fontSize: '12px',
    color: '#666',
    marginTop: '5px'
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} style={formStyle}>
      <h2 style={{ textAlign: 'center', marginBottom: '30px' }}>
        {medication ? '✏️ Edit Medication' : '💊 Add New Medication'}
      </h2>

      {errorMessage && (
        <div style={{ ...errorStyle, marginBottom: '20px', padding: '10px', backgroundColor: '#f8d7da', borderRadius: '4px' }}>
          {errorMessage}
        </div>
      )}

      <div style={{ marginBottom: '20px', textAlign: 'left' }}>
        <label htmlFor="patientId" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
          Patient *
        </label>
        <select
          id="patientId"
          style={selectStyle}
          {...register('patientId')}
          disabled={!!selectedPatientId || loadingPatients}
          aria-invalid={!!errors.patientId}
        >
          <option value="">
            {loadingPatients ? 'Loading patients...' : 'Select a patient'}
          </option>
          {patients.map((patient) => (
            <option key={patient.id} value={patient.id}>
              {patientService.formatPatientName(patient)} ({patientService.calculateAge(patient.dateOfBirth)} years old)
            </option>
          ))}
        </select>
        {errors.patientId && (
          <div style={errorStyle}>{errors.patientId.message}</div>
        )}
      </div>

      <div style={{ marginBottom: '20px', textAlign: 'left' }}>
        <label htmlFor="name" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
          Medication Name *
        </label>
        <input
          id="name"
          type="text"
          style={inputStyle}
          placeholder="Enter medication name"
          {...register('name')}
          aria-invalid={!!errors.name}
        />
        {errors.name && (
          <div style={errorStyle}>{errors.name.message}</div>
        )}
        <div style={helpTextStyle}>
          Example: Paracetamol, Ibuprofen, Amoxicillin
        </div>
      </div>

      <div style={rowStyle}>
        <div style={halfWidthStyle}>
          <label htmlFor="dosage" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Dosage Amount *
          </label>
          <input
            id="dosage"
            type="number"
            step="0.1"
            min="0.1"
            max="10000"
            style={inputStyle}
            placeholder="e.g., 500"
            {...register('dosage', { valueAsNumber: true })}
            aria-invalid={!!errors.dosage}
          />
          {errors.dosage && (
            <div style={errorStyle}>{errors.dosage.message}</div>
          )}
        </div>

        <div style={halfWidthStyle}>
          <label htmlFor="unit" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Unit *
          </label>
          <select
            id="unit"
            style={selectStyle}
            {...register('unit')}
            aria-invalid={!!errors.unit}
          >
            <option value="">Select unit</option>
            {COMMON_UNITS.map((unit) => (
              <option key={unit} value={unit}>
                {unit}
              </option>
            ))}
          </select>
          {errors.unit && (
            <div style={errorStyle}>{errors.unit.message}</div>
          )}
        </div>
      </div>

      <div style={{ marginBottom: '20px', textAlign: 'left' }}>
        <label htmlFor="frequency" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
          Frequency *
        </label>
        <select
          id="frequency"
          style={selectStyle}
          {...register('frequency')}
          aria-invalid={!!errors.frequency}
        >
          {FREQUENCY_OPTIONS.map((freq) => (
            <option key={freq} value={freq}>
              {getFrequencyDisplayName(freq)}
            </option>
          ))}
        </select>
        {errors.frequency && (
          <div style={errorStyle}>{errors.frequency.message}</div>
        )}
        <div style={helpTextStyle}>
          {getFrequencyDescription(selectedFrequency)}
        </div>
      </div>

      <div style={rowStyle}>
        <div style={halfWidthStyle}>
          <label htmlFor="startDate" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Start Date *
          </label>
          <input
            id="startDate"
            type="date"
            style={inputStyle}
            {...register('startDate')}
            aria-invalid={!!errors.startDate}
          />
          {errors.startDate && (
            <div style={errorStyle}>{errors.startDate.message}</div>
          )}
        </div>

        <div style={halfWidthStyle}>
          <label htmlFor="endDate" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            End Date (Optional)
          </label>
          <input
            id="endDate"
            type="date"
            style={inputStyle}
            {...register('endDate')}
            aria-invalid={!!errors.endDate}
          />
          {errors.endDate && (
            <div style={errorStyle}>{errors.endDate.message}</div>
          )}
          <div style={helpTextStyle}>
            Leave blank for ongoing medication
          </div>
        </div>
      </div>

      <div style={{ marginBottom: '30px', textAlign: 'left' }}>
        <label htmlFor="notes" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
          Notes
        </label>
        <textarea
          id="notes"
          rows={4}
          style={{...inputStyle, resize: 'vertical'}}
          placeholder="Additional notes about this medication (instructions, side effects to monitor, etc.)"
          {...register('notes')}
          aria-invalid={!!errors.notes}
        />
        {errors.notes && (
          <div style={errorStyle}>{errors.notes.message}</div>
        )}
      </div>

      <div style={{ textAlign: 'center' }}>
        <button
          type="submit"
          disabled={isLoading}
          style={saveButtonStyle}
        >
          {isLoading ? 'Saving...' : medication ? 'Update Medication' : 'Add Medication'}
        </button>
        <button
          type="button"
          onClick={onCancel}
          disabled={isLoading}
          style={cancelButtonStyle}
        >
          Cancel
        </button>
      </div>
    </form>
  );
};