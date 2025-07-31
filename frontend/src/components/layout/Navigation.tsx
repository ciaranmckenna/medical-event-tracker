import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import './Navigation.css';

export const Navigation: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuth();
  

  // Handle logout
  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };



  return (
    <nav className="nav-bar" role="navigation" aria-label="Main navigation">
      <div className="nav-container">
        {isAuthenticated ? (
          <>
            <Link 
              to="/dashboard" 
              className={`nav-link ${location.pathname === '/dashboard' ? 'active' : ''}`}
              aria-current={location.pathname === '/dashboard' ? 'page' : undefined}
            >
              📊 Dashboard
            </Link>
            
            <Link 
              to="/patients" 
              className={`nav-link ${location.pathname === '/patients' ? 'active' : ''}`}
              aria-current={location.pathname === '/patients' ? 'page' : undefined}
            >
              👥 Patients
            </Link>
            
            <Link 
              to="/medications" 
              className={`nav-link ${location.pathname === '/medications' ? 'active' : ''}`}
              aria-current={location.pathname === '/medications' ? 'page' : undefined}
            >
              💊 Medications
            </Link>
            
            <Link 
              to="/analytics" 
              className={`nav-link ${location.pathname === '/analytics' ? 'active' : ''}`}
              aria-current={location.pathname === '/analytics' ? 'page' : undefined}
            >
              📊 Analytics
            </Link>
            
            <Link 
              to="/events" 
              className={`nav-link ${location.pathname === '/events' ? 'active' : ''}`}
              aria-current={location.pathname === '/events' ? 'page' : undefined}
            >
              📋 Events
            </Link>
            
            <Link 
              to="/dosages" 
              className={`nav-link ${location.pathname === '/dosages' ? 'active' : ''}`}
              aria-current={location.pathname === '/dosages' ? 'page' : undefined}
            >
              📅 Schedule
            </Link>
          </>
        ) : (
          <>
            <Link 
              to="/login" 
              className={`nav-link ${location.pathname === '/login' ? 'active' : ''}`}
              aria-current={location.pathname === '/login' ? 'page' : undefined}
            >
              🔐 Login
            </Link>
            <Link 
              to="/register" 
              className={`nav-link ${location.pathname === '/register' ? 'active' : ''}`}
              aria-current={location.pathname === '/register' ? 'page' : undefined}
            >
              📝 Register
            </Link>
          </>
        )}
      </div>
      
      <div className="user-container">
        {isAuthenticated && user && (
          <>
            <span className="user-info">
              👤 {user.firstName} {user.lastName} ({user.role})
            </span>
            <button 
              onClick={handleLogout} 
              className="nav-link logout"
              aria-label="Logout from application"
            >
              🚪 Logout
            </button>
          </>
        )}
      </div>
    </nav>
  );
};