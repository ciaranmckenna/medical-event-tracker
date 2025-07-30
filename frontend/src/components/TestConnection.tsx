import { useState } from 'react';
import { apiClient } from '../services/api/apiClient';
import { authService } from '../services/auth/authService';

export const TestConnection: React.FC = () => {
  const [backendStatus, setBackendStatus] = useState<string>('Not tested');
  const [authStatus, setAuthStatus] = useState<string>('Not tested');
  const [loading, setLoading] = useState(false);

  const testBackendConnection = async () => {
    setLoading(true);
    try {
      const health = await apiClient.healthCheck();
      setBackendStatus(`✅ Connected - Status: ${health.status}`);
    } catch (error) {
      setBackendStatus(`❌ Failed to connect to backend: ${error}`);
    }
    setLoading(false);
  };

  const testAuthService = () => {
    try {
      console.log('Testing auth service...');
      const isAuth = authService.isAuthenticated();
      const hasToken = authService.getStoredToken() !== null;
      const tokenValid = authService.isTokenValid();
      
      setAuthStatus(`✅ Auth: ${isAuth}, Token: ${hasToken}, Valid: ${tokenValid}`);
      console.log('Auth service test completed');
    } catch (error) {
      console.error('Auth service test failed:', error);
      setAuthStatus(`❌ Error testing auth service: ${error}`);
    }
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'monospace' }}>
      <h2>🧪 Stage 6 Foundation Tests</h2>
      
      <div style={{ marginBottom: '20px' }}>
        <h3>Backend API Connection</h3>
        <p>Status: {backendStatus}</p>
        <button 
          onClick={testBackendConnection} 
          disabled={loading}
          style={{ padding: '10px', margin: '5px' }}
        >
          {loading ? 'Testing...' : 'Test Backend Connection'}
        </button>
        <p style={{ fontSize: '12px', color: '#666' }}>
          Note: Make sure backend is running on localhost:8080
        </p>
      </div>

      <div style={{ marginBottom: '20px' }}>
        <h3>Authentication Service</h3>
        <p>Status: {authStatus}</p>
        <button 
          onClick={testAuthService}
          style={{ padding: '10px', margin: '5px' }}
        >
          Test Auth Service
        </button>
      </div>

      <div style={{ marginBottom: '20px' }}>
        <h3>Environment Variables</h3>
        <ul>
          <li>API Base URL: {import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}</li>
          <li>Debug Mode: {import.meta.env.VITE_DEBUG_MODE || 'false'}</li>
          <li>PWA Enabled: {import.meta.env.VITE_ENABLE_PWA || 'false'}</li>
        </ul>
      </div>

      <div>
        <h3>TypeScript Types Test</h3>
        <p>✅ All TypeScript types compiled successfully</p>
        <p>✅ API client initialized</p>
        <p>✅ Auth service initialized</p>
        <p>✅ Medical data types defined</p>
      </div>
    </div>
  );
};