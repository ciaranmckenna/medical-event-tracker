import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { registerSchema, type RegisterFormData } from '../../services/validation/authValidation';

export const RegisterPage: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [successMessage, setSuccessMessage] = useState<string>('');
  const navigate = useNavigate();
  const { register: registerUser, isAuthenticated } = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema)
  });

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  const onSubmit = async (data: RegisterFormData) => {
    setIsLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      await registerUser({
        username: data.username,
        email: data.email,
        password: data.password,
        firstName: data.firstName,
        lastName: data.lastName
      });
      
      setSuccessMessage('Registration successful! Redirecting to dashboard...');
      setTimeout(() => navigate('/dashboard'), 2000);
    } catch (error: any) {
      setErrorMessage(
        error.response?.data?.message || 
        'Registration failed. Please check your information and try again.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const formStyle = {
    maxWidth: '500px',
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
    width: '100%',
    padding: '12px',
    backgroundColor: '#28a745',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    fontSize: '16px',
    cursor: isLoading ? 'not-allowed' : 'pointer',
    opacity: isLoading ? 0.7 : 1
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
    <div style={{ padding: '40px 20px', textAlign: 'center' }}>
      <h1>📝 Create Medical Tracker Account</h1>
      <p style={{ marginBottom: '30px', color: '#666' }}>
        Join our patient care management system
      </p>

      <form onSubmit={handleSubmit(onSubmit)} style={formStyle}>
        {errorMessage && (
          <div style={{ ...errorStyle, marginBottom: '20px', padding: '10px', backgroundColor: '#f8d7da', borderRadius: '4px' }}>
            {errorMessage}
          </div>
        )}

        {successMessage && (
          <div style={{ color: '#155724', marginBottom: '20px', padding: '10px', backgroundColor: '#d4edda', borderRadius: '4px' }}>
            {successMessage}
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
              placeholder="Enter your first name"
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
              placeholder="Enter your last name"
              {...register('lastName')}
              aria-invalid={!!errors.lastName}
            />
            {errors.lastName && (
              <div style={errorStyle}>{errors.lastName.message}</div>
            )}
          </div>
        </div>

        <div style={{ marginBottom: '20px', textAlign: 'left' }}>
          <label htmlFor="username" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Username *
          </label>
          <input
            id="username"
            type="text"
            style={inputStyle}
            placeholder="Choose a username"
            {...register('username')}
            aria-invalid={!!errors.username}
          />
          {errors.username && (
            <div style={errorStyle}>{errors.username.message}</div>
          )}
        </div>

        <div style={{ marginBottom: '20px', textAlign: 'left' }}>
          <label htmlFor="email" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Email Address *
          </label>
          <input
            id="email"
            type="email"
            style={inputStyle}
            placeholder="Enter your email address"
            {...register('email')}
            aria-invalid={!!errors.email}
          />
          {errors.email && (
            <div style={errorStyle}>{errors.email.message}</div>
          )}
        </div>

        <div style={{ marginBottom: '20px', textAlign: 'left' }}>
          <label htmlFor="password" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Password *
          </label>
          <input
            id="password"
            type="password"
            style={inputStyle}
            placeholder="Create a secure password"
            {...register('password')}
            aria-invalid={!!errors.password}
          />
          {errors.password && (
            <div style={errorStyle}>{errors.password.message}</div>
          )}
          <div style={{ fontSize: '12px', color: '#666', marginTop: '5px' }}>
            Password must contain: 8+ characters, uppercase, lowercase, number, special character
          </div>
        </div>

        <div style={{ marginBottom: '30px', textAlign: 'left' }}>
          <label htmlFor="confirmPassword" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Confirm Password *
          </label>
          <input
            id="confirmPassword"
            type="password"
            style={inputStyle}
            placeholder="Confirm your password"
            {...register('confirmPassword')}
            aria-invalid={!!errors.confirmPassword}
          />
          {errors.confirmPassword && (
            <div style={errorStyle}>{errors.confirmPassword.message}</div>
          )}
        </div>

        <button
          type="submit"
          disabled={isLoading}
          style={buttonStyle}
        >
          {isLoading ? 'Creating Account...' : 'Create Account'}
        </button>

        <div style={{ marginTop: '20px', fontSize: '14px' }}>
          <p>
            Already have an account?{' '}
            <Link to="/login" style={{ color: '#007bff', textDecoration: 'none' }}>
              Sign in here
            </Link>
          </p>
        </div>
      </form>
    </div>
  );
};