import { Routes, Route, Navigate } from 'react-router-dom'
import { Navigation } from './components/layout/Navigation'
import { TestConnection } from './components/TestConnection'
import { LoginPage } from './pages/auth/LoginPage'
import { RegisterPage } from './pages/auth/RegisterPage'
import { DashboardPage } from './pages/dashboard/DashboardPage'
import { PatientsPage } from './pages/patients/PatientsPage'
import { MedicationsPage } from './pages/medications/MedicationsPage'
import './App.css'

function App() {
  return (
    <div className="App">
      <header style={{ padding: '20px', textAlign: 'center', borderBottom: '2px solid #007bff' }}>
        <h1>🏥 Medical Events Tracker</h1>
        <p>Patient Care Management System</p>
      </header>
      
      <Navigation />
      
      <main>
        <Routes>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/patients" element={<PatientsPage />} />
          <Route path="/medications" element={<MedicationsPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/test" element={<TestConnection />} />
        </Routes>
      </main>
    </div>
  )
}

export default App
