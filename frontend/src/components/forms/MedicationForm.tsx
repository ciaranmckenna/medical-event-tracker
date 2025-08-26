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

const MEDICATION_TYPES = [
  'TABLET', 'CAPSULE', 'LIQUID', 'INJECTION', 'TOPICAL', 'INHALER', 'PATCH', 'SUPPOSITORY', 'OTHER'
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
      name: medication.name,
      type: 'TABLET' as const,
      genericName: '',
      strength: 0,
      unit: '',
      manufacturer: '',
      description: ''
    } : {
      name: '',
      type: 'TABLET' as const,
      genericName: '',
      strength: 0,
      unit: '',
      manufacturer: '',
      description: ''
    }
  });

  // No need to load patients for medication catalog

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

      <div style={{ marginBottom: '20px', textAlign: 'left' }}>
        <label htmlFor="type" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
          Medication Type *
        </label>
        <select
          id="type"
          style={selectStyle}
          {...register('type')}
          aria-invalid={!!errors.type}
        >
          <option value="">Select medication type</option>
          {MEDICATION_TYPES.map((type) => (
            <option key={type} value={type}>
              {type.charAt(0) + type.slice(1).toLowerCase().replace('_', ' ')}
            </option>
          ))}
        </select>
        {errors.type && (
          <div style={errorStyle}>{errors.type.message}</div>
        )}
        <div style={helpTextStyle}>
          Choose the form of medication (tablet, liquid, etc.)
        </div>
      </div>

      <div style={{ marginBottom: '20px', textAlign: 'left' }}>
        <label htmlFor="genericName" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
          Generic Name
        </label>
        <input
          id="genericName"
          type="text"
          style={inputStyle}
          placeholder="Enter generic name (optional)"
          {...register('genericName')}
          aria-invalid={!!errors.genericName}
        />
        {errors.genericName && (
          <div style={errorStyle}>{errors.genericName.message}</div>
        )}
        <div style={helpTextStyle}>
          Example: Acetaminophen (for Paracetamol)
        </div>
      </div>

      <div style={rowStyle}>
        <div style={halfWidthStyle}>
          <label htmlFor="strength" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Strength
          </label>
          <input
            id="strength"
            type="number"
            step="0.1"
            min="0.001"
            max="10000"
            style={inputStyle}
            placeholder="e.g., 500"
            {...register('strength', { valueAsNumber: true })}
            aria-invalid={!!errors.strength}
          />
          {errors.strength && (
            <div style={errorStyle}>{errors.strength.message}</div>
          )}
        </div>

        <div style={halfWidthStyle}>
          <label htmlFor="unit" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Unit
          </label>
          <select
            id="unit"
            style={selectStyle}
            {...register('unit')}
            aria-invalid={!!errors.unit}
          >
            <option value="">Select unit (optional)</option>
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
        <label htmlFor="manufacturer" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
          Manufacturer
        </label>
        <input
          id="manufacturer"
          type="text"
          style={inputStyle}
          placeholder="Enter manufacturer (optional)"
          {...register('manufacturer')}
          aria-invalid={!!errors.manufacturer}
        />
        {errors.manufacturer && (
          <div style={errorStyle}>{errors.manufacturer.message}</div>
        )}
        <div style={helpTextStyle}>
          Example: Pfizer, Johnson & Johnson, Generic
        </div>
      </div>

      <div style={{ marginBottom: '30px', textAlign: 'left' }}>
        <label htmlFor="description" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
          Description
        </label>
        <textarea
          id="description"
          rows={4}
          style={{...inputStyle, resize: 'vertical'}}
          placeholder="Additional information about this medication (uses, side effects, etc.)"
          {...register('description')}
          aria-invalid={!!errors.description}
        />
        {errors.description && (
          <div style={errorStyle}>{errors.description.message}</div>
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