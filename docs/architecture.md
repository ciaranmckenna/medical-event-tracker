# Medical Event Tracker - System Architecture

## Overview

This document provides a comprehensive view of the Medical Event Tracker system architecture, detailing the end-to-end flow of user requests from the React frontend to the Spring Boot backend and back.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                             CLIENT-SIDE                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│  Browser                                                                    │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐         │
│  │   React App     │    │   Zustand Store │    │   React Query   │         │
│  │   (Vite)        │◄──►│   (Client State)│◄──►│  (Server State) │         │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘         │
│           │                       │                       │                │
│           ▼                       ▼                       ▼                │
│  ┌─────────────────────────────────────────────────────────────────────────┐ │
│  │                        API Client (Axios)                              │ │
│  └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTPS Request
                                    │ Bearer Token + CSRF
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                             SERVER-SIDE                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│  Spring Boot Application (Port 8080)                                       │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────────┐ │
│  │                      Security Layer                                     │ │
│  │  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐     │ │
│  │  │  CORS Filter    │───►│ JWT Auth Filter │───►│  Security Chain │     │ │
│  │  └─────────────────┘    └─────────────────┘    └─────────────────┘     │ │
│  └─────────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────────┐ │
│  │                      Controller Layer                                   │ │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐         │ │
│  │  │ PatientController│  │ AuthController  │  │MedicalEventCtrl │  ....   │ │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘         │ │
│  └─────────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────────┐ │
│  │                       Service Layer                                     │ │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐         │ │
│  │  │ PatientService  │  │   UserService   │  │MedicalEventSrvc │  ....   │ │
│  │  │     Impl        │  │     Impl        │  │     Impl        │         │ │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘         │ │
│  └─────────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────────┐ │
│  │                     Repository Layer                                    │ │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐         │ │
│  │  │PatientRepository│  │  UserRepository │  │MedicalEventRepo │  ....   │ │
│  │  │  (JpaRepository)│  │  (JpaRepository)│  │  (JpaRepository)│         │ │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘         │ │
│  └─────────────────────────────────────────────────────────────────────────┘ │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────────┐ │
│  │                        Database Layer                                   │ │
│  │                     H2 (Dev) / MySQL (Prod)                             │ │
│  └─────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Technology Stack

### Frontend Technologies
- **React 19** with TypeScript for UI components
- **Vite** for build tooling and development server
- **Axios** for HTTP client with interceptors
- **React Query** for server state management and caching
- **Zustand** for client-side state management
- **React Hook Form + Zod** for form handling and validation
- **React Router v6** for navigation
- **Recharts** for data visualization

### Backend Technologies
- **Spring Boot 3.x** with Java 21
- **Spring Security** with JWT authentication
- **Spring Data JPA** for data access
- **Bean Validation** for request validation
- **H2** (development) / **MySQL** (production) for database
- **Maven** for dependency management

## Detailed Request Flow Example: Creating a Patient

### 1. Frontend Initiation (User Action)

```
User fills out PatientForm.tsx
          │
          ▼
Form validation (Zod schema)
          │
          ▼
React Hook Form submission
          │
          ▼
patientService.createPatient() called
          │
          ▼
apiClient.post('/api/patients', data)
```

### 2. HTTP Request Journey

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              REQUEST PATH                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Frontend (React) - Port 5173                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 1. User Action: Submit PatientForm                                  │   │
│  │    - Form data: {firstName, lastName, dateOfBirth, ...}             │   │
│  │    - Zod validation applied                                         │   │
│  │    - React Hook Form handles submission                             │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 2. API Service Layer                                               │   │
│  │    - patientService.createPatient(data) called                     │   │
│  │    - Service calls apiClient.post('/api/patients', data)           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 3. API Client (Axios)                                              │   │
│  │    - Adds Authorization: Bearer <token>                            │   │
│  │    - Adds X-Requested-With: XMLHttpRequest                         │   │
│  │    - Adds X-CSRF-Token if available                                │   │
│  │    - Sets Content-Type: application/json                           │   │
│  │    - Timeout: 10s                                                  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│                            HTTPS Request                                    │
│  POST http://localhost:8080/api/patients                                   │
│  Headers: {                                                                │
│    'Authorization': 'Bearer eyJ0eXAiOiJKV1Q...',                          │
│    'Content-Type': 'application/json',                                     │
│    'X-Requested-With': 'XMLHttpRequest',                                   │
│    'X-CSRF-Token': '...'                                                   │
│  }                                                                          │
│  Body: {                                                                    │
│    "firstName": "John",                                                     │
│    "lastName": "Doe",                                                       │
│    "dateOfBirth": "1990-01-01",                                            │
│    "gender": "MALE",                                                        │
│    "weightKg": 75.5,                                                        │
│    "heightCm": 180.0                                                        │
│  }                                                                          │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3. Backend Processing

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              BACKEND PROCESSING                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Spring Boot App - Port 8080                                               │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 1. Security Filter Chain                                           │   │
│  │    ┌─────────────────────────────────────────────────────────────┐ │   │
│  │    │ a) CORS Filter                                              │ │   │
│  │    │    - Validates origin (localhost:5173 allowed)              │ │   │
│  │    │    - Checks allowed methods (POST allowed)                  │ │   │
│  │    │    - Adds CORS headers to response                          │ │   │
│  │    └─────────────────────────────────────────────────────────────┘ │   │
│  │                               │                                     │   │
│  │                               ▼                                     │   │
│  │    ┌─────────────────────────────────────────────────────────────┐ │   │
│  │    │ b) JWT Authentication Filter                                │ │   │
│  │    │    - Extracts Bearer token from Authorization header        │ │   │
│  │    │    - Validates JWT signature and expiration                 │ │   │
│  │    │    - Extracts user info from JWT claims                     │ │   │
│  │    │    - Sets Authentication in SecurityContext                 │ │   │
│  │    └─────────────────────────────────────────────────────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 2. Controller Layer - PatientController.java:29                   │   │
│  │    @PostMapping                                                    │   │
│  │    public ResponseEntity<PatientResponse> createPatient(           │   │
│  │        @Valid @RequestBody PatientCreateRequest request,           │   │
│  │        @AuthenticationPrincipal User user)                         │   │
│  │                                                                    │   │
│  │    - @Valid triggers Bean Validation on request DTO               │   │
│  │    - @AuthenticationPrincipal injects authenticated User          │   │
│  │    - Calls patientService.createPatient(request, user)            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 3. Service Layer - PatientServiceImpl.java:34                     │   │
│  │    @Transactional                                                  │   │
│  │    public PatientResponse createPatient(...)                       │   │
│  │                                                                    │   │
│  │    Business Logic:                                                 │   │
│  │    a) Check for duplicate patient (line 36)                       │   │
│  │    b) Create new Patient entity (line 40-46)                      │   │
│  │    c) Set additional properties (line 48-50)                      │   │
│  │    d) Save to database via repository                             │   │
│  │    e) Convert entity to DTO response                              │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 4. Repository Layer - PatientRepository (JpaRepository)           │   │
│  │    - Spring Data JPA auto-generates SQL                           │   │
│  │    - Duplicate check: findByFirstNameAndLastNameAndDateOfBirth    │   │
│  │    - Save operation: save(patient)                                │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 5. Database Layer                                                  │   │
│  │    SQL Generated:                                                  │   │
│  │    SELECT * FROM patients WHERE                                    │   │
│  │      first_name = ? AND last_name = ? AND date_of_birth = ?        │   │
│  │                                                                    │   │
│  │    INSERT INTO patients (id, first_name, last_name, ...)           │   │
│  │    VALUES (?, ?, ?, ...)                                           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 4. Response Journey Back

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              RESPONSE PATH                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Database → Repository → Service → Controller → HTTP Response               │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 1. Database Returns Patient Record                                 │   │
│  │    - Patient entity with generated UUID                            │   │
│  │    - Timestamps: createdAt, updatedAt                              │   │
│  │    - Associated User relationship                                  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 2. Service Layer Transforms Entity to DTO                         │   │
│  │    - Patient entity → PatientResponse record                       │   │
│  │    - Excludes sensitive internal fields                           │   │
│  │    - Adds calculated fields (age, BMI if applicable)              │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 3. Controller Returns HTTP 201 Created                            │   │
│  │    ResponseEntity.status(HttpStatus.CREATED).body(response)        │   │
│  │                                                                    │   │
│  │    HTTP Response:                                                  │   │
│  │    Status: 201 Created                                             │   │
│  │    Headers: {                                                      │   │
│  │      'Content-Type': 'application/json',                          │   │
│  │      'Access-Control-Allow-Origin': 'http://localhost:5173',      │   │
│  │      'Access-Control-Allow-Credentials': 'true'                   │   │
│  │    }                                                               │   │
│  │    Body: {                                                         │   │
│  │      "patientId": "123e4567-e89b-12d3-a456-426614174000",         │   │
│  │      "firstName": "John",                                          │   │
│  │      "lastName": "Doe",                                            │   │
│  │      "age": 34,                                                    │   │
│  │      "gender": "MALE",                                             │   │
│  │      "createdAt": "2024-08-10T15:30:00Z"                          │   │
│  │    }                                                               │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│                            HTTPS Response                                   │
│                     (Back to Frontend)                                      │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 5. Frontend Response Handling

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          FRONTEND RESPONSE HANDLING                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 1. API Client (Axios) Receives Response                           │   │
│  │    - Status 201 triggers success path                             │   │
│  │    - Response interceptor logs in development mode                │   │
│  │    - Returns response.data to calling service                     │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 2. Patient Service Returns Data                                   │   │
│  │    - patientService.createPatient() resolves with PatientResponse │   │
│  │    - Data flows back to React component                           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 3. React Query Updates Cache                                      │   │
│  │    - New patient added to query cache                             │   │
│  │    - Related queries invalidated (patient list, dashboard stats)  │   │
│  │    - UI components automatically re-render                        │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ 4. UI Updates                                                     │   │
│  │    - Form resets (React Hook Form)                                │   │
│  │    - Success toast notification displayed                         │   │
│  │    - Navigation to patient list or dashboard                      │   │
│  │    - PatientCard components re-render with new data               │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Security Architecture

### Authentication Flow
```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            SECURITY FLOW                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1. User Login                                                              │
│     Frontend → POST /api/auth/login → Backend                              │
│     Credentials validated → JWT token generated → Sent to frontend         │
│                                                                             │
│  2. Subsequent Requests                                                     │
│     Frontend includes: Authorization: Bearer <token>                       │
│     JwtAuthenticationFilter validates token                                │
│     User context set in SecurityContext                                    │
│                                                                             │
│  3. CORS Protection                                                         │
│     Origin validation: localhost:5173 (dev), production domain             │
│     Preflight requests handled for complex requests                        │
│                                                                             │
│  4. CSRF Protection                                                         │
│     X-CSRF-Token header validation                                          │
│     Token rotation on sensitive operations                                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Data Validation Flow
```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           VALIDATION LAYERS                                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Frontend Validation (Zod Schema):                                         │
│  - Type safety and format validation                                       │
│  - Medical data constraints (dosage limits, date ranges)                   │
│  - Real-time feedback to users                                             │
│                                                                             │
│  Backend Validation (Bean Validation):                                     │
│  - @Valid annotation on controller methods                                 │
│  - Medical domain-specific validators                                       │
│  - Database constraint validation                                           │
│                                                                             │
│  Business Logic Validation:                                                │
│  - Duplicate patient checks                                                │
│  - User authorization (owns patient/medical event)                         │
│  - Medical data consistency (medication schedules, event timelines)        │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Error Handling Architecture

### Error Flow Patterns
```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              ERROR SCENARIOS                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  Frontend Validation Error:                                                │
│  User Input → Zod Validation → Error Display → No API Call                │
│                                                                             │
│  Authentication Error (401):                                               │
│  API Call → JWT Invalid → 401 Response → Token Cleared → Redirect Login   │
│                                                                             │
│  Business Logic Error (400):                                               │
│  API Call → Duplicate Patient → DuplicatePatientException →                │
│  GlobalExceptionHandler → 400 Response → Form Error Display                │
│                                                                             │
│  Server Error (500):                                                       │
│  API Call → Database Error → 500 Response → Toast Error → Retry Option    │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Global Exception Handler
The backend uses a centralized `GlobalExceptionHandler` that:
- Catches all application exceptions
- Maps to appropriate HTTP status codes
- Provides consistent error response format
- Logs errors for debugging and monitoring
- Protects sensitive information from exposure

## Database Architecture

### Entity Relationships
```
User (1) ──────────── (*) Patient
              │
              │
Patient (1) ─────────── (*) MedicalEvent
              │
              │
Patient (*) ─────────── (*) Medication (via PatientMedication)
              │
              │
Patient (1) ─────────── (*) MedicationDosage
```

### Key Design Decisions
- **UUID Primary Keys**: For security and scalability
- **Soft Deletion**: Patient records marked as inactive, not deleted
- **Audit Timestamps**: All entities track creation and modification times
- **User Isolation**: All data scoped to authenticated user
- **Medical Data Validation**: Database constraints for medical measurements

## Performance Considerations

### Frontend Optimization
- **Code Splitting**: Route-based lazy loading
- **React Query Caching**: 5-minute stale time for medical data
- **Virtual Scrolling**: For large data sets (patient lists, event timelines)
- **Memoization**: Expensive calculations cached with useMemo/useCallback

### Backend Optimization
- **Database Indexing**: On frequently queried fields (user_id, timestamps)
- **JPA Fetch Strategies**: Lazy loading for relationships
- **Transaction Management**: @Transactional for data consistency
- **Connection Pooling**: Configured for production workloads

## Deployment Architecture

### Development Environment
- **Frontend**: Vite dev server (localhost:5173)
- **Backend**: Spring Boot embedded Tomcat (localhost:8080)
- **Database**: H2 in-memory database
- **Proxy**: Vite proxies API requests to backend

### Production Considerations
- **Frontend**: Static files served by CDN/nginx
- **Backend**: Spring Boot WAR deployed to application server
- **Database**: MySQL with proper connection pooling
- **Load Balancing**: Multiple backend instances with session management
- **HTTPS**: TLS termination at load balancer

## File Structure Reference

### Frontend Key Files
```
frontend/src/
├── services/api/apiClient.ts          # Central HTTP client with security
├── hooks/useAuth.tsx                  # Authentication state management
├── pages/patients/PatientsPage.tsx    # Patient management UI
├── components/forms/PatientForm.tsx   # Patient creation form
└── services/validation/               # Zod validation schemas
```

### Backend Key Files
```
src/main/java/.../medical_event_tracker/
├── controller/PatientController.java           # REST endpoints
├── service/impl/PatientServiceImpl.java        # Business logic
├── repository/PatientRepository.java           # Data access
├── config/SecurityConfig.java                  # Security configuration
├── config/JwtAuthenticationFilter.java        # JWT processing
└── exception/GlobalExceptionHandler.java      # Error handling
```

## Development Guidelines

For detailed development guidelines, coding standards, and medical domain considerations, see:
- [CLAUDE.md](../CLAUDE.md) - Comprehensive project guidelines
- [CONTRIBUTING.md](../CONTRIBUTING.md) - Contribution guidelines
- [README.md](../README.md) - Project overview and setup

## Monitoring and Observability

### Logging Strategy
- **Frontend**: Console logging in development, structured logging in production
- **Backend**: SLF4J with Logback, different levels for different scenarios
- **Security Events**: Authentication failures, authorization denials
- **Medical Events**: Audit trail for all medical record changes

### Health Checks
- **Frontend**: Connection tests with backend health endpoint
- **Backend**: Spring Boot Actuator health endpoints
- **Database**: Connection pool monitoring
- **External Services**: JWT validation, external API integrations

This architecture ensures a secure, scalable, and maintainable medical application that follows Spring Boot best practices and modern frontend patterns.