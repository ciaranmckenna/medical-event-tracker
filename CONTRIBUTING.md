# Contributing to Medical Events Tracker

Thank you for your interest in contributing to the Medical Events Tracker! This guide will help you get started with development and ensure consistency across contributions.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Code Standards](#code-standards)
- [Branch Strategy](#branch-strategy)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Testing Requirements](#testing-requirements)
- [Code Review Process](#code-review-process)

## 🤝 Code of Conduct

This project follows a professional code of conduct focused on healthcare software development:

- **Patient Safety First**: All contributions must consider the medical nature of this application
- **Security Focused**: Security implications must be considered for all changes
- **Professional Standards**: Code must meet healthcare software quality standards
- **Respectful Collaboration**: Maintain professional communication in all interactions

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Git**
- **IDE**: IntelliJ IDEA or Eclipse recommended
- **Postman** (for API testing)

### First-Time Setup

1. **Fork and Clone**
   ```bash
   git clone https://github.com/yourusername/medical-event-tracker.git
   cd medical-event-tracker
   ```

2. **Set up upstream remote**
   ```bash
   git remote add upstream https://github.com/ciaranmckenna/medical-event-tracker.git
   ```

3. **Install dependencies**
   ```bash
   mvn clean install
   ```

4. **Verify setup**
   ```bash
   mvn test
   mvn spring-boot:run
   ```

## 💻 Development Setup

### Environment Configuration

#### Development Profile (Default)
- Uses H2 in-memory database
- Debug logging enabled
- Hot reload with spring-boot-devtools

#### Test Profile
- Separate H2 test database
- Reduced logging
- Faster test execution

#### Production Profile (Future)
- MySQL database
- Production-level logging
- Security hardening

### IDE Setup

#### IntelliJ IDEA
1. Import as Maven project
2. Enable annotation processing
3. Set Project SDK to Java 21
4. Install recommended plugins:
   - Spring Boot
   - JPA Buddy (optional)

#### Code Style
- **Indentation**: 4 spaces (no tabs)
- **Line length**: 120 characters max
- **Imports**: Organize imports, remove unused

## 📝 Code Standards

### Architecture Principles

Follow the **Clean Architecture** pattern:
```
Controller → Service → Repository → Entity
```

- **Controllers**: Handle HTTP requests/responses only
- **Services**: Business logic and transaction management  
- **Repositories**: Data access layer
- **Entities**: Database models with JPA annotations

### Java Standards

#### No Lombok Policy
Write explicit code for maintainability:
```java
// ✅ DO: Explicit getters/setters
public String getName() {
    return name;
}

public void setName(String name) {
    this.name = name;
}

// ❌ DON'T: Lombok annotations
@Getter @Setter
private String name;
```

#### Use Records for DTOs
```java
// ✅ DO: Records for immutable data
public record RegisterRequest(
    @NotBlank String username,
    @Email String email,
    @Size(min = 8) String password
) {}

// ❌ DON'T: Traditional classes for simple DTOs
public class RegisterRequest {
    private String username;
    private String email;
    // ... getters/setters
}
```

#### Entity Design
```java
// ✅ DO: Proper JPA entities
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    // Explicit constructors
    public User() {}
    
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
    
    // Proper equals/hashCode using ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
}
```

### Validation & Security

#### Input Validation
Always validate inputs at the Controller level:
```java
public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    // Service layer assumes input is already validated
}
```

#### Security Considerations
- Never log sensitive data (passwords, tokens)
- Always use parameterized queries
- Validate all user inputs
- Follow OWASP guidelines

### Medical Domain Standards

#### Data Sensitivity
- All medical data must be handled with GDPR compliance
- Implement audit trails for data changes
- Use precise timestamps for medical events
- Never expose sensitive data in error messages

#### Naming Conventions
Use medical terminology appropriately:
```java
// ✅ DO: Clear medical terminology
@Entity
public class MedicalEvent {
    private LocalDateTime symptomOnsetTime;
    private MedicationDosage currentDosage;
}

// ❌ DON'T: Generic names for medical concepts
@Entity  
public class Event {
    private LocalDateTime time;
    private String data;
}
```

## 🌿 Branch Strategy

### Branch Types

- **main**: Production-ready code only
- **develop**: Integration branch for features
- **feature/MET-XXX**: New features (from JIRA tickets)
- **bugfix/MET-XXX**: Bug fixes
- **hotfix/XXX**: Critical production fixes

### Branch Naming Convention

```bash
# Features
feature/MET-123-patient-registration
feature/MET-456-medication-tracking

# Bug fixes  
bugfix/MET-789-login-validation
bugfix/MET-012-jwt-expiration

# Hotfixes
hotfix/critical-security-patch
hotfix/database-connection-issue
```

### Workflow

1. **Start from develop**
   ```bash
   git checkout develop
   git pull upstream develop
   ```

2. **Create feature branch**
   ```bash
   git checkout -b feature/MET-123-description
   ```

3. **Make changes and test**
   ```bash
   # Make your changes
   mvn test
   mvn clean install
   ```

4. **Keep branch updated**
   ```bash
   git checkout develop
   git pull upstream develop
   git checkout feature/MET-123-description
   git merge develop
   ```

## 💬 Commit Guidelines

### Commit Message Format

```
MET-XXX Brief description in present tense

- Detailed bullet point of what was changed
- Another change description  
- Reference to any breaking changes

Co-Authored-By: Name <email@example.com> (if applicable)
```

### Examples

```bash
# ✅ Good commit messages
MET-123 Add patient registration endpoint

- Implement PatientController with CRUD operations
- Add validation for patient demographic data
- Include integration tests for all endpoints

MET-456 Fix JWT token expiration handling

- Update JwtService to handle token refresh
- Add proper error responses for expired tokens
- Fix security filter to reject malformed tokens
```

```bash
# ❌ Bad commit messages
"fixed bug"
"updated code"
"MET-123"
"Added stuff for patients"
```

### Commit Types

- **feat**: New features
- **fix**: Bug fixes
- **refactor**: Code restructuring
- **test**: Adding/updating tests
- **docs**: Documentation updates
- **security**: Security-related changes

## 🔄 Pull Request Process

### Before Creating PR

1. **Ensure all tests pass**
   ```bash
   mvn clean test
   ```

2. **Run full build**
   ```bash
   mvn clean install
   ```

3. **Check for security issues**
   ```bash
   mvn org.owasp:dependency-check-maven:check
   ```

4. **Update branch with latest develop**
   ```bash
   git checkout develop
   git pull upstream develop
   git checkout your-branch
   git merge develop
   ```

### PR Template

Use this template for all pull requests:

```markdown
## Description
Brief description of changes and why they were made.

## Type of Change
- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass  
- [ ] Manual testing completed
- [ ] Security considerations reviewed

## Medical/Healthcare Considerations
- [ ] Patient data privacy maintained
- [ ] Medical terminology used correctly
- [ ] Audit trail requirements met
- [ ] No sensitive data exposed

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Code commented appropriately
- [ ] Documentation updated if needed
- [ ] No new security vulnerabilities introduced
```

### PR Review Process

1. **Automated Checks**: GitHub Actions must pass
2. **Code Review**: At least one approved review required  
3. **Medical Review**: Healthcare-related changes need domain expert review
4. **Security Review**: Security-sensitive changes need additional review

## 🧪 Testing Requirements

### Test Coverage Standards

- **Minimum**: 80% line coverage
- **Services**: 90% coverage required
- **Controllers**: Integration tests for all endpoints
- **Repositories**: Test custom queries

### Test Types Required

#### Unit Tests
```java
// ✅ Test business logic
@Test
void registerUser_Success() {
    // Given
    RegisterRequest request = new RegisterRequest(...);
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    
    // When
    AuthResponse response = userService.registerUser(request);
    
    // Then  
    assertThat(response.username()).isEqualTo("testuser");
    verify(userRepository).save(any(User.class));
}
```

#### Integration Tests
```java
// ✅ Test full application context
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {
    
    @Test
    void registerUser_ReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());
    }
}
```

#### Medical Domain Tests
Always test edge cases for medical data:
```java
@Test
void recordMedicalEvent_InvalidTimestamp_ThrowsException() {
    // Medical events cannot be in the future
    LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
    
    assertThatThrownBy(() -> 
        medicalEventService.recordEvent(event.withTimestamp(futureTime))
    ).isInstanceOf(InvalidMedicalDataException.class);
}
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceImplTest

# Run tests with coverage
mvn test jacoco:report

# Run integration tests only
mvn test -Dgroups=integration

# Run security tests
mvn org.owasp:dependency-check-maven:check
```

## 👀 Code Review Process

### What Reviewers Look For

#### Code Quality
- Follows coding standards
- Proper error handling
- No code duplication
- Clear naming conventions

#### Medical Domain
- Correct medical terminology
- Patient data privacy
- Audit trail compliance
- Timestamp accuracy

#### Security
- Input validation
- No hardcoded secrets
- Proper authorization
- Secure data handling

#### Testing
- Adequate test coverage
- Edge cases covered
- Integration tests included
- Medical domain tests

### Review Timeline
- **Initial Review**: Within 2 business days
- **Follow-up**: Within 1 business day
- **Final Approval**: Same day for minor changes

## 🚨 Security Guidelines

### Secure Development Practices

- Never commit secrets or API keys
- Always validate inputs at boundaries
- Use parameterized queries
- Implement proper authentication
- Log security events appropriately

### Medical Data Handling

- Encrypt sensitive data at rest
- Use HTTPS for all communications
- Implement proper access controls
- Maintain audit logs
- Follow GDPR requirements

## 📞 Getting Help

### Communication Channels

- **GitHub Issues**: Technical questions and bug reports
- **GitHub Discussions**: General questions and ideas
- **Pull Request Comments**: Code-specific discussions

### Documentation

- **README.md**: Project overview and setup
- **CLAUDE.md**: Detailed project guidelines
- **ROADMAP.md**: Development timeline
- **API Documentation**: Coming in Stage 4

---

**Remember**: This is medical software that impacts patient care. Quality, security, and accuracy are paramount. When in doubt, choose the more robust, well-tested approach over the quick solution.

*Thank you for contributing to healthcare technology!* 🏥