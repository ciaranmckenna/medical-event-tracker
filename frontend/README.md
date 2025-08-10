# Medical Event Tracker - Frontend 🏥

A modern React 19 application designed for medical professionals, carers, and parents to track patient medical events, medications, and analyze correlations between treatments and outcomes.

## 🎯 Overview

This frontend application provides an intuitive, accessible interface for managing patient medical data with advanced analytics capabilities. Built with medical environments in mind, it features WCAG 2.1 AA compliance, offline support, and specialized medical UI components.

## 🛠 Tech Stack

- **Framework**: React 19 with TypeScript
- **Build Tool**: Vite 7+ with hot module replacement
- **State Management**: 
  - React Query (@tanstack/react-query) for server state
  - Zustand for client-side state management
- **Routing**: React Router v7 with role-based route protection
- **Forms**: React Hook Form + Zod validation
- **Data Visualization**: Recharts for medical correlation analysis
- **Styling**: CSS Modules with responsive design
- **Testing**: Vitest, React Testing Library, Playwright (E2E)
- **PWA**: Service workers for offline medical data entry

## 🚀 Quick Start

### Prerequisites
- Node.js 18+ 
- npm or yarn
- Backend API running on http://localhost:8080

### Installation & Setup

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Access the application
open http://localhost:5173
```

### Environment Configuration

Create a `.env.local` file:

```env
# API Configuration
VITE_API_BASE_URL=http://localhost:8080
VITE_API_TIMEOUT=10000

# Feature Flags
VITE_ENABLE_ANALYTICS=true
VITE_ENABLE_OFFLINE_MODE=true
VITE_ENABLE_PWA=true

# Development
VITE_MOCK_API=false
VITE_DEBUG_MODE=true
```

## 📋 Available Scripts

### Development
```bash
npm run dev          # Start development server
npm run dev:https    # Start with HTTPS for PWA testing
```

### Building
```bash
npm run build        # Production build
npm run build:pwa    # Build with PWA optimizations
npm run preview      # Preview production build
```

### Testing
```bash
npm run test         # Unit tests with Vitest
npm run test:ui      # Tests with UI interface
npm run test:e2e     # End-to-end tests with Playwright
npm run test:coverage # Coverage report
```

### Code Quality
```bash
npm run lint         # ESLint analysis
npm run lint:fix     # Auto-fix linting issues
npm run type-check   # TypeScript checking
npm run format       # Prettier formatting
```

### Analysis
```bash
npm run analyze      # Bundle size analysis
```

## 🏗 Project Structure

```
src/
├── components/              # Reusable UI components
│   ├── ui/                 # Base design system
│   │   ├── Card.tsx
│   │   ├── FilterGroup.tsx
│   │   ├── StatCard.tsx
│   │   └── Toast.tsx
│   ├── forms/              # Medical form components
│   │   ├── MedicalEventForm.tsx
│   │   ├── MedicationForm.tsx
│   │   └── PatientForm.tsx
│   ├── charts/             # Data visualization
│   │   ├── CorrelationChart.tsx
│   │   └── SimpleChart.tsx
│   ├── medical/            # Medical-specific components
│   │   ├── PatientCard.tsx
│   │   ├── MedicationCard.tsx
│   │   └── MedicalEventCard.tsx
│   ├── layout/             # Navigation and layout
│   │   └── Navigation.tsx
│   └── auth/               # Authentication components
│       └── ProtectedRoute.tsx
├── pages/                  # Route-based pages
│   ├── auth/              # Login, Register
│   ├── dashboard/         # Main dashboard
│   ├── patients/          # Patient management
│   ├── medications/       # Medication tracking
│   ├── events/           # Medical events
│   ├── dosages/          # Dosage tracking
│   └── analytics/        # Data analysis
├── services/              # API and business logic
│   ├── api/              # API service layers
│   ├── auth/             # Authentication services
│   ├── validation/       # Zod validation schemas
│   └── mockData/         # Development mock data
├── hooks/                # Custom React hooks
│   └── useAuth.tsx
├── types/                # TypeScript definitions
│   ├── api.ts
│   ├── medical.ts
│   └── ui.ts
└── test/                 # Test configurations
    └── setup.ts
```

## 🏥 Key Features

### Medical Dashboard
- **Real-time Overview**: Patient vitals, medication schedules, recent events
- **Quick Actions**: Emergency contact, medication alerts, event logging
- **Role-based Views**: Different interfaces for primary users, secondary users, and admins

### Patient Management
- **Comprehensive Profiles**: Demographics, medical history, current medications
- **Multi-patient Support**: Manage multiple patients per user account
- **Secure Data Handling**: GDPR-compliant data storage and processing

### Medication Tracking
- **Schedule Management**: AM/PM dosage tracking with notifications
- **Effectiveness Analysis**: Track medication impact on symptoms
- **Correlation Insights**: Visual correlation between medications and events

### Medical Event Recording
- **Timestamp Precision**: Accurate event timing for correlation analysis
- **Severity Categorization**: Mild, Moderate, Severe, Critical classifications
- **Photo Attachments**: Visual documentation of symptoms or conditions

### Advanced Analytics
- **Correlation Analysis**: Interactive charts showing medication-event relationships
- **Timeline Visualization**: Patient medical history timeline
- **Impact Assessment**: Statistical analysis of treatment effectiveness
- **Exportable Reports**: PDF/CSV exports for healthcare providers

### Accessibility & PWA
- **WCAG 2.1 AA Compliant**: Screen reader support, keyboard navigation
- **Offline Capabilities**: Service workers for offline data entry
- **Responsive Design**: Mobile-first approach for on-the-go access
- **High Contrast Mode**: Medical environment optimized display

## 🔐 Security Features

- **Input Sanitization**: DOMPurify integration for XSS protection
- **Token Management**: Secure JWT handling with automatic refresh
- **Role-based Access**: Different permission levels for different user types
- **Data Validation**: Comprehensive Zod schemas for medical data integrity

## 🧪 Testing Strategy

### Unit Tests
- React Testing Library for component testing
- Vitest for fast test execution
- Mock API responses for isolation testing

### Integration Tests
- Full user workflows (login, patient creation, event logging)
- API integration testing with mock backend

### End-to-End Tests
- Playwright for critical medical workflows
- Accessibility testing automation
- Cross-browser compatibility testing

## 🚀 Deployment

### Production Build
```bash
npm run build
```

### PWA Configuration
The application is configured as a Progressive Web App with:
- Service worker for offline functionality
- App manifest for home screen installation
- Background sync for medical data

### Performance Optimization
- Code splitting by routes
- Lazy loading of non-critical components
- Virtual scrolling for large datasets
- Bundle analysis and optimization

## 🔧 Development Guidelines

### Component Development
- Use TypeScript for all components
- Implement proper error boundaries
- Follow accessibility best practices
- Include comprehensive prop validation

### State Management
- React Query for server state (caching, synchronization)
- Zustand for client state (UI, user preferences)
- Form state managed by React Hook Form

### Styling Guidelines
- CSS Modules for component-scoped styles
- Responsive design with mobile-first approach
- Medical-grade color palette with severity indicators
- Consistent spacing and typography scale

### API Integration
- Axios with interceptors for authentication
- Error handling with user-friendly messages
- Request/response logging in development
- Retry logic for failed requests

## 📞 Support & Contributing

For development questions or issues:
- Check the main project README for setup instructions
- Review CLAUDE.md for comprehensive development guidelines
- Create issues in the main repository
- Follow the established code review process

---

**⚠️ Medical Disclaimer**: This application is for informational and organizational purposes only. It is not intended to replace professional medical advice, diagnosis, or treatment. Always consult qualified healthcare providers for medical decisions.