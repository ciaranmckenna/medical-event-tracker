# CLAUDE.md - Medical Events Tracker Project Guidelines

## Project Overview

**Project Name:** Medical Events Tracker MVP  
**Purpose:** Track medical events and medication responses to identify optimal dosage patterns  
**Target Users:** Parents, medical staff, and carers  
**Tech Stack:** Java 21, Spring Boot 3.x, Maven, Spring Web, Spring Data JPA, spring-boot-devtools, H2, mysql-connector-j, spring-boot-starter-test

## Development Philosophy

### Core Principles
- **TDD First:** Write tests before implementation to avoid unnecessary bugs
- **Edge Case Consideration:** Always analyse potential edge cases during design
- **Small Commits:** Prefer frequent, meaningful commits over large code dumps
- **Maintainable Solutions:** Choose clean, maintainable approaches over minimal changes
- **SOLID Principles:** Single Responsibility, Open-Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **Additional Principles:** DRY (Don't Repeat Yourself), KISS (Keep It Simple, Stupid), YAGNI (You Aren't Gonna Need It)

### OWASP Security Guidelines
- Implement Spring Security for authentication/authorisation
- Use BCrypt for password encoding
- Validate all inputs using Bean Validation
- Implement proper CORS configuration
- Sanitise all user inputs, especially medical data

## Code Standards and Conventions

### Code Standards and Conventions

### Naming Conventions
- **Classes:** PascalCase (e.g., `MedicationController`, `PatientService`)
- **Methods/Variables:** camelCase (e.g., `findPatientById`, `calculateDosageImpact`)
- **Constants:** ALL_CAPS (e.g., `MAX_MEDICATION_TYPES`, `DEFAULT_PAGE_SIZE`)
- **Packages:** lowercase with dots (e.g., `com.medicaltracker.patient.entity`)

### No Lombok Policy & Record Usage
- **Avoid Lombok completely** - write explicit getters, setters, constructors for all classes
- **Use Java Records for immutable data structures:**
  - DTOs (Data Transfer Objects)
  - API request/response models
  - Value objects (Address, Money, DateRange)
  - Configuration properties
  - Query result containers
- **Use Traditional Classes for:**
  - JPA Entities (require mutable state for ORM)
  - Services and Controllers (stateful behaviour)
  - Complex business objects with behaviour
- **Benefits:** Improved debugging, no magic, explicit code, reduced dependencies
- **For Entities:** Implement proper `equals()` and `hashCode()` methods using entity ID
- **For Classes:** Provide both default constructor (for JPA) and parameterised constructors

### Project Structure
```
src/main/java/com/medicaltracker/
├── config/           # Configuration classes
├── controller/       # REST controllers (@RestController)
├── service/          # Service interfaces
├── service/impl/     # Service implementations (@Service)
├── repository/       # Data access layer (@Repository, JpaRepository)
├── entity/          # Entity models (@Entity, explicit getters/setters)
├── dto/             # Data transfer objects (record types)
├── exception/       # Custom exceptions and GlobalExceptionHandler
└── validation/      # Custom validators
```

### Spring Boot Best Practices
- Use constructor injection with final fields for all dependencies
- All REST operations through Controllers only
- Database operations only through ServiceImpl classes using Repositories
- Data transfer between layers using DTOs (record types with validation)
- Entity classes only for database query results
- Avoid Lombok - write explicit getters, setters, constructors, equals, hashCode
- Implement `@ControllerAdvice` for global exception handling using `GlobalExceptionHandler`
- Use `@ConfigurationProperties` for type-safe configuration
- Leverage Spring Boot starters appropriately
- Implement proper validation with `@Valid` annotations and Bean Validation

### Medical Domain Considerations
- **Data Sensitivity:** All medical data must be handled with GDPR compliance in mind
- **Audit Trail:** Implement audit logging for all medical record changes
- **Data Validation:** Strict validation for medical measurements (weight, height, dosage)
- **Temporal Data:** Precise timestamp handling for medication timing and incident correlation
- **Immutable Medical Records:** Use records for medical events, dosage information, and timestamps
- **Mutable Patient State:** Use traditional entities for patient information that changes over time

## Development Workflow

### Test-Driven Development (TDD)
1. **Red:** Write a failing test that defines desired functionality
2. **Green:** Write minimal code to make the test pass
3. **Refactor:** Clean up code while keeping tests green

### Testing Strategy
- **Unit Tests:** JUnit 5 with Mockito for service layer testing
- **Integration Tests:** `@SpringBootTest` for full application context
- **Repository Tests:** `@DataJpaTest` for database layer
- **Web Layer Tests:** `@WebMvcTest` with MockMvc
- **Edge Cases:** Always test boundary conditions, null values, and invalid inputs

### Branch Strategy & Workflow
**IMPORTANT:** Always create a new feature branch for each development task or MVP stage component.

#### Branch Types
- **main:** Production-ready code only
- **develop:** Integration branch for completed features  
- **feature/MET-XXX:** New features (from tickets/stages)
- **bugfix/MET-XXX:** Bug fixes
- **hotfix/XXX:** Critical production fixes

#### Workflow for Each Development Task
1. **Start from develop branch:**
   ```bash
   git checkout develop
   git pull origin develop
   ```

2. **Create feature branch with descriptive name:**
   ```bash
   # For MVP stages
   git checkout -b feature/MET-XXX-patient-management
   git checkout -b feature/MET-XXX-medication-tracking
   
   # For specific features  
   git checkout -b feature/MET-XXX-jwt-authentication
   git checkout -b feature/MET-XXX-user-registration
   ```

3. **Develop feature with TDD approach**

4. **Commit regularly with meaningful messages**

5. **Push feature branch and create PR to develop:**
   ```bash
   git push -u origin feature/MET-XXX-description
   ```

6. **After PR approval and merge, create new branch for next task:**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/MET-XXX-next-feature
   ```

#### Branch Naming Convention
- Use ticket numbers when available: `feature/MET-123-description`
- For MVP stages: `feature/MET-XXX-stage-2-patient-mgmt`  
- For bug fixes: `bugfix/MET-XXX-auth-validation`
- Keep names descriptive but concise

### Git Commit Guidelines
- Use present tense, imperative mood ("Add medication tracking" not "Added medication tracking")
- Keep first line under 50 characters
- Reference issue numbers when applicable: "MET-XXX Add patient registration"
- Structure: `MET-XXX Brief description`
- Include detailed bullets for complex changes:
  ```
  MET-123 Add patient management system
  
  - Implement PatientController with CRUD operations
  - Add Patient entity with proper validation
  - Create PatientService with business logic
  - Include comprehensive integration tests
  ```

## Domain-Specific Guidelines

### Patient Data Management
- Implement soft deletion for patient records
- Use UUIDs for patient identifiers to avoid enumeration attacks
- Encrypt sensitive personal information at rest
- Implement data retention policies

### Medication Tracking
- Support multiple concurrent medications
- Track active/inactive status with timestamps
- Implement dosage calculation validation
- Support AM/PM scheduling with timezone awareness

### Incident Recording
- Immutable incident records once created
- Comprehensive timestamp recording (creation, occurrence)
- Support for incident categorisation and severity levels
- Implement search functionality across multiple criteria

### Data Analysis Features
- Correlation analysis between medication timing and incidents
- Graphical overlay capabilities for trend analysis
- Statistical significance calculations for dosage effectiveness
- Export functionality for medical professional review

## Performance and Scalability

### Caching Strategy
- Use Spring Cache abstraction for frequently accessed patient data
- Implement Redis for session management
- Cache medication lookup tables

### Database Design
- Implement proper indexing for timestamp-based queries
- Use database migrations with Flyway
- Optimise queries for medication-incident correlation analysis

### Async Processing
- Use `@Async` for non-critical operations (notifications, analytics)
- Implement proper error handling for async operations

## Security Requirements

### Authentication & Authorisation
- Role-based access control (Primary User, Secondary User, Admin)
- JWT tokens for API authentication
- Session timeout for sensitive medical data access

### Data Protection
- Encrypt patient data at rest and in transit
- Implement field-level encryption for sensitive medical information
- Regular security audits and vulnerability assessments

### Compliance
- GDPR compliance for EU users
- HIPAA considerations for medical data handling
- Implement data export and deletion capabilities

## Monitoring and Logging

### Logging Strategy
- Use SLF4J with Logback
- Log levels: ERROR (system failures), WARN (business rule violations), INFO (user actions), DEBUG (development)
- Implement structured logging for medical events
- Never log sensitive patient data in plain text

### Monitoring
- Spring Boot Actuator for application health
- Custom metrics for medication tracking accuracy
- Database performance monitoring
- User activity tracking (anonymised)

## Documentation Standards

### Code Documentation
- Javadoc for all public methods, especially medical calculation logic
- Inline comments for complex business rules
- README files for each major module

### API Documentation
- Use Springdoc OpenAPI for REST API documentation
- Include example requests/responses for medical data endpoints
- Document all validation rules and error responses

## System Architecture Reference

### Comprehensive Architecture Documentation
For detailed system architecture, request flow, and integration patterns, see:

📖 **[System Architecture Documentation](docs/architecture.md)**

This documentation provides:
- **Complete end-to-end flow diagrams** showing user queries from frontend to backend
- **Security architecture** with JWT, CORS, and validation layers
- **Technology stack integration** between React 19 + Spring Boot 3.x
- **Database design patterns** and entity relationships
- **Error handling strategies** and validation approaches
- **Performance considerations** and deployment patterns

**Key Integration Points:**
- Frontend: React 19 + TypeScript + Vite + React Query + Zustand
- Backend: Spring Boot 3.x + Spring Security + JPA + JWT
- Database: H2 (dev) / MySQL (prod) with proper indexing
- Security: Multi-layer validation and authentication

Always reference this architecture documentation when:
- Implementing new features that span frontend/backend
- Adding new API endpoints or modifying existing ones  
- Working on authentication, authorization, or security features
- Planning database schema changes or new entities
- Debugging cross-layer integration issues

## Communication Guidelines

### Solution Proposals
- Always provide 2-3 alternative approaches
- Compare minimal changes vs. comprehensive refactors
- Explain maintenance implications of each approach
- Comment on code changes to clarify modifications

### Code Reviews
- Focus on medical domain accuracy
- Verify security implications
- Check for proper error handling
- Ensure test coverage for edge cases

### Language
- Use UK English throughout codebase and documentation
- Medical terminology should be precise and professionally appropriate

## Getting Started Checklist

### Initial Setup
- [ ] Configure Spring Boot project with appropriate starters
- [ ] Set up database with Flyway migrations
- [ ] Implement basic security configuration
- [ ] Create domain models for Patient, Medication, Incident
- [ ] Set up testing framework with sample test cases
- [ ] Configure logging and monitoring
- [ ] Implement basic CRUD operations with tests

### MVP Milestone Targets
- [ ] User authentication and profile management
- [ ] Patient registration and management
- [ ] Medication tracking with scheduling
- [ ] Incident recording and categorisation
- [ ] Basic correlation analysis between medication and incidents
- [ ] Search and filtering capabilities
- [ ] Data export functionality

## Questions for Clarification

When implementing features, always consider:
1. How does this affect patient data privacy?
2. What are the edge cases for medical measurements?
3. How will this scale with multiple patients per user?
4. What validation is needed for medical accuracy?
5. How should this integrate with existing healthcare systems?
6. What audit trail is required for regulatory compliance?

---

---

# Frontend Development Guidelines (Stage 6)

## Core Principles
- **Medical-First Design:** Prioritise patient safety and healthcare workflows
- **Accessibility:** WCAG 2.1 AA compliance mandatory
- **Type Safety:** Comprehensive TypeScript for medical data integrity
- **Performance:** Sub-second load times for emergency scenarios
- **Testing:** Component tests before implementation
- **Offline Support:** PWA capabilities for unreliable networks

## Tech Stack
- **Framework:** React 18+ with TypeScript
- **Build Tool:** Vite 5+ with hot reload
- **State:** React Query (server) + Zustand (client)
- **Routing:** React Router v6 with role-based protection
- **Forms:** React Hook Form + Zod validation
- **Charts:** Recharts for correlation analysis
- **Testing:** Vitest, React Testing Library, Playwright

## Project Structure
```
frontend/src/
├── components/
│   ├── ui/           # Design system components
│   ├── forms/        # Medical form components
│   ├── charts/       # Data visualization
│   └── layout/       # Navigation and layout
├── pages/            # Route components
├── hooks/            # Custom React hooks
├── services/         # API integration
├── stores/           # Zustand state
├── types/            # TypeScript definitions
└── utils/            # Helper functions
│   │   │   └── DataTable/
│   │   ├── medical/            # Medical-specific components
│   │   │   ├── PatientCard/
│   │   │   ├── MedicationForm/
│   │   │   ├── EventTimeline/
│   │   │   └── CorrelationChart/
│   │   ├── forms/             # Composite form components
│   │   └── layout/            # Navigation and page layouts
│   ├── pages/                 # Route-based components
│   ├── hooks/                 # Custom React hooks
│   │   ├── useApi.ts
│   │   ├── useAuth.ts
│   │   ├── useMedicalData.ts
│   │   └── useOfflineSync.ts
│   ├── services/              # API and business logic
│   │   ├── api/
│   │   ├── auth/
│   │   └── validation/
│   ├── stores/                # State management
│   │   ├── authStore.ts
│   │   ├── patientStore.ts
│   │   └── uiStore.ts
│   ├── types/                 # TypeScript definitions
│   │   ├── api.ts
│   │   ├── medical.ts
│   │   └── ui.ts
│   └── utils/                 # Helper functions
├── docs/                      # Frontend documentation
└── tests/                     # Test configurations
```

## Component Guidelines
- **Design System:** Atomic components with medical-specific variants (emergency, critical)
- **Form Validation:** React Hook Form + Zod with medical data constraints
- **Accessibility:** WCAG 2.1 AA compliance, ARIA labels, keyboard navigation
- **Medical Components:** Specialized forms for medications, events, patient data

## State Management
- **Server State:** React Query for API data with optimistic updates
- **Client State:** Zustand for auth, UI state, offline data
- **Caching:** 5-minute stale time for medical data, immediate updates for critical changes

## Testing Requirements
- **Unit Tests:** React Testing Library for all medical components
- **Integration Tests:** API integration with mock medical data
- **E2E Tests:** Playwright for critical workflows (medication tracking, event logging)
- **Accessibility Tests:** Automated a11y checks in test suite

## UI/UX Standards
- **Color System:** Medical-grade palette with severity indicators (mild/moderate/severe/critical)
- **Typography:** Clear hierarchy optimized for medical professionals
- **Data Visualization:** Recharts with accessibility features for correlation analysis
- **Emergency Patterns:** Quick access for urgent medical scenarios

## Performance
- **Code Splitting:** Route-based lazy loading
- **Optimization:** Virtual scrolling for large datasets, memoization for expensive calculations
- **PWA:** Service workers for offline medication/event entry
- **Bundle Size:** Monitor with bundle analyzer, target <500KB initial load

## Security & Deployment
- **Input Validation:** Zod schemas with DOMPurify for medical data sanitization
- **CSP Headers:** Strict content security policy for medical applications
- **Token Management:** Secure httpOnly cookies in production, session storage in development
- **Build Pipeline:** Type checking, linting, testing, coverage reports in CI/CD
- **PWA Configuration:** Offline support with service workers for critical medical data entry
  });

  return (
    <div ref={scrollElementRef} className="medical-data-table">
      {virtualizer.getVirtualItems().map((virtualRow) => (
        <MedicalDataRow
          key={virtualRow.key}
          data={data[virtualRow.index]}
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: `${virtualRow.size}px`,
            transform: `translateY(${virtualRow.start}px)`
          }}
        />
      ))}
    </div>
  );
});
```

### Offline Support and PWA
```typescript
// Service Worker for Offline Functionality
export const useOfflineSync = () => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [pendingSync, setPendingSync] = useState<MedicalData[]>([]);

  useEffect(() => {
    const handleOnline = () => {
      setIsOnline(true);
      syncPendingData();
    };

    const handleOffline = () => {
      setIsOnline(false);
    };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  const syncPendingData = async () => {
    for (const data of pendingSync) {
      try {
        await apiClient.sync(data);
        setPendingSync(prev => prev.filter(item => item.id !== data.id));
      } catch (error) {
        console.error('Sync failed for:', data.id, error);
      }
    }
  };

  const saveOfflineData = (data: MedicalData) => {
    if (!isOnline) {
      setPendingSync(prev => [...prev, data]);
      localStorage.setItem('pendingMedicalData', JSON.stringify([...pendingSync, data]));
    }
  };

  return { isOnline, pendingSync, saveOfflineData };
};
```

## Security Guidelines for Frontend

### Input Sanitization and Validation
```typescript
// Medical Data Validation Schemas
export const medicationSchema = z.object({
  name: z.string()
    .min(1, 'Medication name is required')
    .max(100, 'Medication name too long')
    .regex(/^[a-zA-Z0-9\s\-\.]+$/, 'Invalid characters in medication name'),
    
  dosage: z.number()
    .positive('Dosage must be positive')
    .max(10000, 'Dosage exceeds maximum safe limit')
    .refine((val) => val % 0.1 === 0, 'Dosage must be in increments of 0.1'),
    
  frequency: z.enum(['ONCE_DAILY', 'TWICE_DAILY', 'THREE_TIMES_DAILY', 'AS_NEEDED']),
  
  startDate: z.date()
    .min(new Date('2020-01-01'), 'Start date too far in the past')
    .max(new Date(), 'Start date cannot be in the future'),
    
  notes: z.string()
    .max(500, 'Notes too long')
    .transform((val) => sanitizeHtml(val)) // Sanitize HTML input
});

// XSS Protection Utility
export const sanitizeHtml = (input: string): string => {
  return DOMPurify.sanitize(input, {
    ALLOWED_TAGS: [], // No HTML tags allowed in medical data
    ALLOWED_ATTR: [],
    RETURN_DOM_FRAGMENT: false
  });
};
```

### Secure API Communication
```typescript
// API Client with Security Headers
export const createApiClient = (token: string) => {
  const client = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json',
      'X-Requested-With': 'XMLHttpRequest',
      'Authorization': `Bearer ${token}`
    }
  });

  // Request interceptor for CSRF protection
  client.interceptors.request.use((config) => {
    const csrfToken = getCsrfToken();
    if (csrfToken) {
      config.headers['X-CSRF-Token'] = csrfToken;
    }
    return config;
  });

  // Response interceptor for error handling
  client.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        // Clear auth and redirect to login
        authStore.getState().logout();
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
  );

  return client;
};
```

## Deployment and Build Guidelines

### Vite Configuration for Medical Applications
```typescript
// vite.config.ts
export default defineConfig({
  plugins: [
    react(),
    vitePWA({
      registerType: 'autoUpdate',
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg}'],
        runtimeCaching: [
          {
            urlPattern: /^https:\/\/api\.medical-tracker\.com\/.*$/,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'api-cache',
              networkTimeoutSeconds: 3,
              cacheableResponse: {
                statuses: [0, 200]
              }
            }
          }
        ]
      }
    })
  ],
  
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          medical: ['recharts', 'react-hook-form'],
          utils: ['zod', 'date-fns']
        }
      }
    },
    
    // Security headers for production
    assetsDir: 'assets',
    sourcemap: false, // Disable in production for security
    
    // Optimize for medical applications
    target: 'es2020',
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true, // Remove console logs in production
        drop_debugger: true
      }
    }
  },
  
  server: {
    https: true, // Required for PWA features
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
});
```

### CI/CD Pipeline for Frontend
```yaml
# .github/workflows/frontend-ci.yml
name: Frontend CI/CD

on:
  push:
    branches: [main, develop]
    paths: ['frontend/**']
  pull_request:
    branches: [main, develop]
    paths: ['frontend/**']

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '18'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      
      - name: Install dependencies
        run: npm ci
        working-directory: frontend
      
      - name: Type checking
        run: npm run type-check
        working-directory: frontend
      
      - name: Linting
        run: npm run lint
        working-directory: frontend
      
      - name: Unit tests
        run: npm run test:coverage
        working-directory: frontend
      
      - name: E2E tests
        run: |
          npm run build
          npm run preview &
          npm run test:e2e
        working-directory: frontend
      
      - name: Accessibility tests
        run: npm run test:a11y
        working-directory: frontend
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          file: frontend/coverage/lcov.info

  build:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '18'
          cache: 'npm'
      
      - name: Build production
        run: |
          npm ci
          npm run build
        working-directory: frontend
        env:
          VITE_API_BASE_URL: ${{ secrets.PRODUCTION_API_URL }}
      
      - name: Deploy to staging
        run: npm run deploy:staging
        working-directory: frontend
```

---

**Remember:** Frontend development for medical applications requires the highest standards of accessibility, security, and reliability. Always prioritise user safety and data integrity over convenience or aesthetics. When in doubt, choose the more robust, well-tested approach over the quick solution.