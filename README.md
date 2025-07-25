# Medical Events Tracker 🏥

A Spring Boot application for tracking medical events and medication responses to identify optimal dosage patterns for patients. Designed for parents, medical staff, and carers to monitor and analyze medical incidents in correlation with medication schedules.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Development](#development)
- [Security](#security)
- [Project Structure](#project-structure)
- [Current Status](#current-status)
- [Contributing](#contributing)

## 🎯 Overview

The Medical Events Tracker is a secure, role-based application that enables healthcare providers and carers to:

- **Track Medical Events**: Record incidents with precise timestamps and categorization
- **Monitor Medications**: Track active medications and dosage schedules (AM/PM)
- **Analyze Correlations**: Identify patterns between medication timing and medical events
- **Manage Multiple Patients**: Support for managing data across multiple patients
- **Role-Based Access**: Primary users (full access) and secondary users (read-only)

**Target Users**: Parents with medical conditions, medical staff, carers, and healthcare coordinators

## ✨ Features

### 🔐 **Authentication & Security**
- JWT-based authentication with secure token management
- Role-based access control (Primary User, Secondary User, Admin)
- BCrypt password encryption
- CORS configuration for frontend integration
- Spring Security integration

### 👤 **User Management**
- User registration and login
- Profile management (update personal information)
- Username and email availability checking
- Soft delete functionality

### 🏗️ **Architecture & Quality**
- Clean architecture (Entity → Repository → Service → Controller)
- Comprehensive input validation using Bean Validation
- RESTful API design with proper HTTP status codes
- Transaction management and data integrity
- Comprehensive test coverage (unit + integration tests)

## 🛠 Tech Stack

### Backend
- **Framework**: Spring Boot 3.5.3
- **Java Version**: 21
- **Database**: H2 (development), MySQL (production)
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA with Hibernate
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Build Tool**: Maven
- **Documentation**: Springdoc OpenAPI (planned)

### Frontend (Stage 6)
- **Framework**: React 18+ with TypeScript
- **Build Tool**: Vite 5+ with hot module replacement
- **State Management**: React Query (server state) + Zustand (client state)
- **Routing**: React Router v6 with role-based route protection
- **UI Components**: Custom design system with medical-specific components
- **Charts & Visualization**: Recharts for correlation analysis and medical timelines
- **Forms**: React Hook Form + Zod validation for medical data integrity
- **Styling**: CSS Modules / Styled Components with responsive design
- **Testing**: Vitest, React Testing Library, Playwright (E2E)
- **Accessibility**: WCAG 2.1 AA compliance for medical applications
- **PWA**: Service workers for offline medical data entry capabilities

## 🚀 Getting Started

### Prerequisites

#### Backend Development
- **Java 21** or higher
- **Maven 3.8+**
- **Git**
- **Postman** (optional, for API testing)

#### Frontend Development (Stage 6)
- **Node.js 18+** with npm/yarn
- **Modern browser** with developer tools
- **VS Code** (recommended with React/TypeScript extensions)
- **Git** for version control

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/ciaranmckenna/medical-event-tracker.git
   cd medical-event-tracker
   ```

2. **Install dependencies**
   ```bash
   mvn clean install
   ```

### Running the Application

1. **Start the application**
   ```bash
   mvn spring-boot:run
   ```

2. **Access the application**
   - **API Base URL**: `http://localhost:8080`
   - **H2 Console**: `http://localhost:8080/h2-console`
     - JDBC URL: `jdbc:h2:mem:medicaltracker`
     - Username: `sa`
     - Password: (leave empty)
   - **Health Check**: `http://localhost:8080/actuator/health`

### Frontend Development (Stage 6)

The frontend is a modern React application designed for medical professionals and carers to manage patient data with advanced analytics and visualization capabilities.

#### Quick Start

1. **Navigate to frontend directory**
   ```bash
   cd frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   # or
   yarn install
   ```

3. **Start development server**
   ```bash
   npm run dev
   # or
   yarn dev
   ```

4. **Access the application**
   - **Frontend URL**: `http://localhost:5173`
   - **API Integration**: Automatically connects to backend at `http://localhost:8080`

#### Available Scripts

```bash
# Development
npm run dev          # Start development server with hot reload
npm run dev:https    # Start with HTTPS for PWA testing

# Building
npm run build        # Production build with optimizations
npm run preview      # Preview production build locally

# Testing
npm run test         # Run unit tests with Vitest
npm run test:ui      # Run tests with UI interface
npm run test:e2e     # Run end-to-end tests with Playwright
npm run test:coverage # Generate test coverage report

# Code Quality
npm run lint         # ESLint code analysis
npm run lint:fix     # Auto-fix linting issues
npm run type-check   # TypeScript type checking
npm run format       # Prettier code formatting

# PWA & Deployment
npm run build:pwa    # Build with PWA optimizations
npm run analyze      # Bundle analyzer for optimization
```

#### Environment Configuration

Create a `.env.local` file in the frontend directory:

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

#### Project Structure

```
frontend/
├── src/
│   ├── components/          # Reusable UI components
│   │   ├── ui/             # Base design system components
│   │   ├── forms/          # Medical form components
│   │   ├── charts/         # Data visualization components
│   │   └── layout/         # Layout and navigation
│   ├── pages/              # Route-based page components
│   │   ├── auth/           # Authentication pages
│   │   ├── dashboard/      # Dashboard and analytics
│   │   ├── patients/       # Patient management
│   │   └── medications/    # Medication tracking
│   ├── hooks/              # Custom React hooks
│   ├── services/           # API and business logic
│   ├── stores/             # Zustand state stores
│   ├── types/              # TypeScript type definitions
│   ├── utils/              # Utility functions
│   ├── styles/             # Global styles and themes
│   └── __tests__/          # Test files
├── public/                 # Static assets
├── docs/                   # Frontend documentation
└── playwright/             # E2E test configurations
```

#### Key Features

- **🏥 Medical Dashboard**: Real-time patient overview with key metrics
- **📊 Analytics & Visualization**: Interactive charts for medication correlation analysis
- **📱 Responsive Design**: Mobile-first approach for on-the-go access
- **🔒 Role-Based Access**: Different interfaces for primary users, secondary users, and admins
- **♿ Accessibility**: WCAG 2.1 AA compliant for medical environments
- **🔄 Offline Support**: PWA capabilities for offline data entry
- **🎨 Medical UI Components**: Specialized components for medical data input and display

## 📚 API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/auth/register` | Register a new user | ❌ |
| `POST` | `/api/auth/login` | Authenticate user | ❌ |
| `GET` | `/api/auth/profile` | Get user profile | ✅ |
| `PUT` | `/api/auth/profile` | Update user profile | ✅ |
| `DELETE` | `/api/auth/profile` | Soft delete user account | ✅ |
| `GET` | `/api/auth/check-username/{username}` | Check username availability | ❌ |
| `GET` | `/api/auth/check-email/{email}` | Check email availability | ❌ |

### Example API Usage

**Register a new user:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com", 
    "password": "securepassword123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "johndoe",
    "password": "securepassword123"
  }'
```

**Access protected endpoint:**
```bash
curl -X GET http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## 🧪 Testing

### Run all tests
```bash
mvn test
```

### Run specific test class
```bash
mvn test -Dtest=UserServiceImplTest
```

### Generate test coverage report
```bash
mvn jacoco:report
```

### Test Types
- **Unit Tests**: Service layer logic testing
- **Integration Tests**: Full application context testing
- **Repository Tests**: Database layer testing (planned)

## 💻 Development

### Running in Development Mode
```bash
# Start with development profile (H2 database)
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Environment Profiles
- **default**: Development with H2 database
- **test**: Test environment for automated testing  
- **prod**: Production with MySQL (planned)

## 🔒 Security

### Implemented Security Measures
- **JWT Authentication**: Secure token-based auth with configurable expiration
- **Password Encryption**: BCrypt hashing with proper salt
- **Input Validation**: Bean Validation on all inputs
- **SQL Injection Prevention**: JPA/Hibernate parameterized queries
- **CORS Configuration**: Controlled cross-origin resource sharing
- **Role-Based Access**: Fine-grained permission system

### Security Headers
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: SAMEORIGIN`
- `X-XSS-Protection: 1; mode=block`

## 🏗 Project Structure

```
src/main/java/com/ciaranmckenna/medical_event_tracker/
├── config/              # Configuration classes
│   ├── SecurityConfig.java
│   └── JwtAuthenticationFilter.java
├── controller/          # REST controllers
│   └── AuthController.java
├── service/             # Service interfaces
│   ├── UserService.java
│   └── JwtService.java
├── service/impl/        # Service implementations
│   ├── UserServiceImpl.java
│   └── JwtServiceImpl.java
├── repository/          # Data access layer
│   └── UserRepository.java
├── entity/              # JPA entities
│   └── User.java
├── dto/                 # Data transfer objects
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── AuthResponse.java
│   └── UserProfileResponse.java
├── exception/           # Exception handling (planned)
└── validation/          # Custom validators (planned)
```

## 🚧 Current Status

**✅ Completed Stages**: 
- **Stage 1**: Core authentication and user management system
- **Stage 2**: Patient management system with CRUD operations
- **Stage 3**: Medical event tracking and medication dosage recording
- **Stage 4**: Advanced search and filtering capabilities with pagination
- **Stage 5**: Data visualization and analytics with correlation analysis

**🔄 In Progress**: 
- **Stage 6**: Modern React frontend with TypeScript and medical-grade UI components

**📋 Backend API**: Fully functional REST API with comprehensive analytics endpoints  
**🎨 Frontend**: Ready for Stage 6 implementation with detailed guidelines and architecture

See [MVP-roadmap.md](MVP-roadmap.md) for detailed development plan and [CLAUDE.md](CLAUDE.md) for comprehensive development guidelines.

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for:
- Development setup and coding standards  
- Branch strategy and commit message format
- Pull request process
- Code review guidelines

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For questions or support:
- **GitHub Issues**: [Create an issue](https://github.com/ciaranmckenna/medical-event-tracker/issues)
- **Email**: Contact the development team
- **Documentation**: See `CLAUDE.md` for detailed development guidelines

---

**⚠️ Important Medical Disclaimer**: This software is for informational and organizational purposes only. It is not intended to replace professional medical advice, diagnosis, or treatment. Always consult qualified healthcare providers for medical decisions.

---

*🤖 Generated with [Claude Code](https://claude.ai/code)*