import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './hooks/useAuth'
import { ProtectedRoute } from './components/auth/ProtectedRoute'
import { Navigation } from './components/layout/Navigation'
import { TestConnection } from './components/TestConnection'
import { LoginPage } from './pages/auth/LoginPage'
import { RegisterPage } from './pages/auth/RegisterPage'
import { DashboardPage } from './pages/dashboard/DashboardPage'
import { PatientsPage } from './pages/patients/PatientsPage'
import { MedicationsPage } from './pages/medications/MedicationsPage'
import { EventsPage } from './pages/events/EventsPage'
import { DosagesPage } from './pages/dosages/DosagesPage'
import { AnalyticsPage } from './pages/analytics/AnalyticsPage'
import './App.css'

function App() {
  return (
    <AuthProvider>
      <div className="App">
        <header style={{ padding: '20px', textAlign: 'center', borderBottom: '2px solid #007bff' }}>
          <h1>🏥 Medical Events Tracker</h1>
          <p>Patient Care Management System</p>
        </header>
        
        <Navigation />
        
        <main>
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route 
              path="/dashboard" 
              element={
                <ProtectedRoute>
                  <DashboardPage />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/patients" 
              element={
                <ProtectedRoute requiredRoles={['PRIMARY_USER', 'ADMIN']}>
                  <PatientsPage />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/medications" 
              element={
                <ProtectedRoute requiredRoles={['PRIMARY_USER', 'SECONDARY_USER', 'ADMIN']}>
                  <MedicationsPage />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/events" 
              element={
                <ProtectedRoute requiredRoles={['PRIMARY_USER', 'SECONDARY_USER', 'ADMIN']}>
                  <EventsPage />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/dosages" 
              element={
                <ProtectedRoute requiredRoles={['PRIMARY_USER', 'SECONDARY_USER', 'ADMIN']}>
                  <DosagesPage />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/analytics" 
              element={
                <ProtectedRoute requiredRoles={['PRIMARY_USER', 'SECONDARY_USER', 'ADMIN']}>
                  <AnalyticsPage />
                </ProtectedRoute>
              } 
            />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/test" element={<TestConnection />} />
          </Routes>
        </main>
      </div>
    </AuthProvider>
  )
}

export default App
