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

- **Framework**: Spring Boot 3.5.3
- **Java Version**: 21
- **Database**: H2 (development), MySQL (production)
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA with Hibernate
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Build Tool**: Maven
- **Documentation**: Springdoc OpenAPI (planned)

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Git**
- **Postman** (optional, for API testing)

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

**Stage 1 Complete**: Core authentication and user management system  
**Next**: Patient management system (Stage 2)  

See [ROADMAP.md](ROADMAP.md) for detailed development plan and timeline.

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