import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { loginSchema, type LoginFormData } from '../../services/validation/authValidation';

export const LoginPage: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string>('');
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isAuthenticated } = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema)
  });

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      const from = location.state?.from?.pathname || '/dashboard';
      navigate(from, { replace: true });
    }
  }, [isAuthenticated, navigate, location]);

  const onSubmit = async (data: LoginFormData) => {
    setIsLoading(true);
    setErrorMessage('');

    try {
      await login(data);
      const from = location.state?.from?.pathname || '/dashboard';
      navigate(from, { replace: true });
    } catch (error: any) {
      setErrorMessage(
        error.response?.data?.message || 
        'Login failed. Please check your credentials and try again.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const formStyle = {
    maxWidth: '400px',
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
    backgroundColor: '#007bff',
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

  return (
    <div style={{ padding: '40px 20px', textAlign: 'center' }}>
      <h1>🔐 Login to Medical Tracker</h1>
      <p style={{ marginBottom: '30px', color: '#666' }}>
        Access your patient care management system
      </p>

      <form onSubmit={handleSubmit(onSubmit)} style={formStyle}>
        {errorMessage && (
          <div style={{ ...errorStyle, marginBottom: '20px', padding: '10px', backgroundColor: '#f8d7da', borderRadius: '4px' }}>
            {errorMessage}
          </div>
        )}

        <div style={{ marginBottom: '20px', textAlign: 'left' }}>
          <label htmlFor="usernameOrEmail" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Username or Email *
          </label>
          <input
            id="usernameOrEmail"
            type="text"
            style={inputStyle}
            placeholder="Enter your username or email"
            {...register('usernameOrEmail')}
            aria-invalid={!!errors.usernameOrEmail}
          />
          {errors.usernameOrEmail && (
            <div style={errorStyle}>{errors.usernameOrEmail.message}</div>
          )}
        </div>

        <div style={{ marginBottom: '30px', textAlign: 'left' }}>
          <label htmlFor="password" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
            Password *
          </label>
          <input
            id="password"
            type="password"
            style={inputStyle}
            placeholder="Enter your password"
            {...register('password')}
            aria-invalid={!!errors.password}
          />
          {errors.password && (
            <div style={errorStyle}>{errors.password.message}</div>
          )}
        </div>

        <button
          type="submit"
          disabled={isLoading}
          style={buttonStyle}
        >
          {isLoading ? 'Signing In...' : 'Sign In'}
        </button>

        <div style={{ marginTop: '20px', fontSize: '14px' }}>
          <p>
            Don't have an account?{' '}
            <Link to="/register" style={{ color: '#007bff', textDecoration: 'none' }}>
              Register here
            </Link>
          </p>
        </div>
      </form>
    </div>
  );
};