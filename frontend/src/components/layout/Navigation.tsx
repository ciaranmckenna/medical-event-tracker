import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

export const Navigation: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const navStyle = {
    padding: '20px',
    borderBottom: '1px solid #ddd',
    marginBottom: '20px',
    backgroundColor: '#f8f9fa',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center'
  };

  const linkStyle = {
    margin: '0 15px',
    textDecoration: 'none',
    padding: '8px 16px',
    borderRadius: '4px',
    backgroundColor: '#007bff',
    color: 'white',
    fontSize: '14px'
  };

  const activeLinkStyle = {
    ...linkStyle,
    backgroundColor: '#0056b3'
  };

  const logoutButtonStyle = {
    ...linkStyle,
    backgroundColor: '#dc3545',
    cursor: 'pointer',
    border: 'none'
  };

  return (
    <nav style={navStyle}>
      <div>
        {isAuthenticated ? (
          <>
            <Link 
              to="/dashboard" 
              style={location.pathname === '/dashboard' ? activeLinkStyle : linkStyle}
            >
              📊 Dashboard
            </Link>
            <Link 
              to="/patients" 
              style={location.pathname === '/patients' ? activeLinkStyle : linkStyle}
            >
              👥 Patients
            </Link>
            <Link 
              to="/medications" 
              style={location.pathname === '/medications' ? activeLinkStyle : linkStyle}
            >
              💊 Medications
            </Link>
            <Link 
              to="/events" 
              style={location.pathname === '/events' ? activeLinkStyle : linkStyle}
            >
              📋 Events
            </Link>
            <Link 
              to="/dosages" 
              style={location.pathname === '/dosages' ? activeLinkStyle : linkStyle}
            >
              📅 Schedule
            </Link>
            <Link 
              to="/analytics" 
              style={location.pathname === '/analytics' ? activeLinkStyle : linkStyle}
            >
              📊 Analytics
            </Link>
          </>
        ) : (
          <>
            <Link 
              to="/login" 
              style={location.pathname === '/login' ? activeLinkStyle : linkStyle}
            >
              🔐 Login
            </Link>
            <Link 
              to="/register" 
              style={location.pathname === '/register' ? activeLinkStyle : linkStyle}
            >
              📝 Register
            </Link>
          </>
        )}
      </div>
      
      <div style={{ display: 'flex', alignItems: 'center' }}>
        {isAuthenticated && user && (
          <>
            <span style={{ marginRight: '15px', fontSize: '14px', color: '#666' }}>
              👤 {user.firstName} {user.lastName} ({user.role})
            </span>
            <button onClick={handleLogout} style={logoutButtonStyle}>
              🚪 Logout
            </button>
          </>
        )}
        <Link 
          to="/test" 
          style={{...linkStyle, backgroundColor: '#6c757d', marginLeft: '10px'}}
        >
          🧪 Test
        </Link>
      </div>
    </nav>
  );
};