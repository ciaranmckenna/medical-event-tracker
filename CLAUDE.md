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

### Git Commit Guidelines
- Use present tense, imperative mood ("Add medication tracking" not "Added medication tracking")
- Keep first line under 50 characters
- Reference issue numbers when applicable
- Structure: `type: brief description`
  - `feat:` new features
  - `fix:` bug fixes
  - `test:` adding tests
  - `refactor:` code restructuring
  - `docs:` documentation updates

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

**Remember:** This is medical software that impacts patient care. Accuracy, security, and reliability are paramount. When in doubt, choose the more robust, well-tested approach over the quick solution.