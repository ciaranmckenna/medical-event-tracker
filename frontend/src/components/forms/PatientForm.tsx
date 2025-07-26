import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { patientSchema, type PatientFormData } from '../../services/validation/patientValidation';
import type { Patient } from '../../types/api';

interface PatientFormProps {
  patient?: Patient;
  onSubmit: (data: PatientFormData) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
}

export const PatientForm: React.FC<PatientFormProps> = ({
  patient,
  onSubmit,
  onCancel,
  isLoading = false
}) => {
  const [errorMessage, setErrorMessage] = useState<string>('');

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<PatientFormData>({
    resolver: zodResolver(patientSchema),
    defaultValues: patient ? {
      firstName: patient.firstName,
      lastName: patient.lastName,
      dateOfBirth: patient.dateOfBirth,
      height: patient.height || undefined,
      weight: patient.weight || undefined,
      emergencyContact: patient.emergencyContact || '',
      emergencyPhone: patient.emergencyPhone || '',
      notes: patient.notes || ''
    } : undefined
  });

  const handleFormSubmit = async (data: PatientFormData) => {
    setErrorMessage('');
    try {
      await onSubmit(data);
    } catch (error: any) {
      setErrorMessage(
        error.response?.data?.message || 
        'Failed to save patient. Please try again.'
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

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} style={formStyle}>
      <h2 style={{ textAlign: 'center', marginBottom: '30px' }}>
        {patient ? '✏️ Edit Patient' : '➕ Add New Patient'}
      </h2>

      {errorMessage && (
        <div style={{ ...errorStyle, marginBottom: '20px', padding: '10px', backgroundColor: '#f8d7da', borderRadius: '4px' }}>
          {errorMessage}
        </div>
      )}

      <div style={rowStyle}>
        <div style={halfWidthStyle}>
          <label htmlFor="firstName" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            First Name *
          </label>
          <input
            id="firstName"
            type="text"
            style={inputStyle}
            placeholder="Enter first name"
            {...register('firstName')}
            aria-invalid={!!errors.firstName}
          />
          {errors.firstName && (
            <div style={errorStyle}>{errors.firstName.message}</div>
          )}
        </div>

        <div style={halfWidthStyle}>
          <label htmlFor="lastName" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Last Name *
          </label>
          <input
            id="lastName"
            type="text"
            style={inputStyle}
            placeholder="Enter last name"
            {...register('lastName')}
            aria-invalid={!!errors.lastName}
          />
          {errors.lastName && (
            <div style={errorStyle}>{errors.lastName.message}</div>
          )}
        </div>
      </div>

      <div style={{ marginBottom: '20px', textAlign: 'left' }}>
        <label htmlFor="dateOfBirth" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
          Date of Birth *
        </label>
        <input
          id="dateOfBirth"
          type="date"
          style={inputStyle}
          {...register('dateOfBirth')}
          aria-invalid={!!errors.dateOfBirth}
        />
        {errors.dateOfBirth && (
          <div style={errorStyle}>{errors.dateOfBirth.message}</div>
        )}
      </div>

      <div style={rowStyle}>
        <div style={halfWidthStyle}>
          <label htmlFor="height" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Height (cm)
          </label>
          <input
            id="height"
            type="number"
            step="0.1"
            min="30"
            max="300"
            style={inputStyle}
            placeholder="Enter height in cm"
            {...register('height', { valueAsNumber: true })}
            aria-invalid={!!errors.height}
          />
          {errors.height && (
            <div style={errorStyle}>{errors.height.message}</div>
          )}
        </div>

        <div style={halfWidthStyle}>
          <label htmlFor="weight" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Weight (kg)
          </label>
          <input
            id="weight"
            type="number"
            step="0.1"
            min="0.5"
            max="1000"
            style={inputStyle}
            placeholder="Enter weight in kg"
            {...register('weight', { valueAsNumber: true })}
            aria-invalid={!!errors.weight}
          />
          {errors.weight && (
            <div style={errorStyle}>{errors.weight.message}</div>
          )}
        </div>
      </div>

      <div style={rowStyle}>
        <div style={halfWidthStyle}>
          <label htmlFor="emergencyContact" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Emergency Contact
          </label>
          <input
            id="emergencyContact"
            type="text"
            style={inputStyle}
            placeholder="Emergency contact name"
            {...register('emergencyContact')}
            aria-invalid={!!errors.emergencyContact}
          />
          {errors.emergencyContact && (
            <div style={errorStyle}>{errors.emergencyContact.message}</div>
          )}
        </div>

        <div style={halfWidthStyle}>
          <label htmlFor="emergencyPhone" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Emergency Phone
          </label>
          <input
            id="emergencyPhone"
            type="tel"
            style={inputStyle}
            placeholder="Emergency contact phone"
            {...register('emergencyPhone')}
            aria-invalid={!!errors.emergencyPhone}
          />
          {errors.emergencyPhone && (
            <div style={errorStyle}>{errors.emergencyPhone.message}</div>
          )}
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
          placeholder="Additional notes about the patient (medical conditions, allergies, etc.)"
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
          {isLoading ? 'Saving...' : patient ? 'Update Patient' : 'Create Patient'}
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