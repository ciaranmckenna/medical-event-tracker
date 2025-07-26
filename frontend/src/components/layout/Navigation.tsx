import { Link, useLocation } from 'react-router-dom';

export const Navigation: React.FC = () => {
  const location = useLocation();

  const navStyle = {
    padding: '20px',
    borderBottom: '1px solid #ddd',
    marginBottom: '20px',
    backgroundColor: '#f8f9fa'
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

  return (
    <nav style={navStyle}>
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
        to="/login" 
        style={location.pathname === '/login' ? activeLinkStyle : linkStyle}
      >
        🔐 Login
      </Link>
    </nav>
  );
};