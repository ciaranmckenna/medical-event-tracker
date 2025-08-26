package com.ciaranmckenna.medical_event_tracker.integration;

import com.ciaranmckenna.medical_event_tracker.dto.PatientCreateRequest;
import com.ciaranmckenna.medical_event_tracker.dto.PatientResponse;
import com.ciaranmckenna.medical_event_tracker.dto.PatientUpdateRequest;
import com.ciaranmckenna.medical_event_tracker.entity.Patient;
import com.ciaranmckenna.medical_event_tracker.entity.User;
import com.ciaranmckenna.medical_event_tracker.repository.PatientRepository;
import com.ciaranmckenna.medical_event_tracker.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class PatientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("medicaluser");
        testUser.setEmail("medicaluser@example.com");
        testUser.setPassword("Password123!");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.Role.PRIMARY_USER);
        testUser = userRepository.save(testUser);

        // Set up security context with the actual user
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(testUser, null, 
                java.util.List.of(new SimpleGrantedAuthority("ROLE_" + testUser.getRole().name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create test patient
        testPatient = new Patient(
            "John",
            "Doe",
            LocalDate.of(1990, 1, 1),
            Patient.Gender.MALE,
            testUser
        );
        testPatient.setWeightKg(new BigDecimal("75.5"));
        testPatient.setHeightCm(new BigDecimal("180.0"));
        testPatient.setNotes("Test patient");
        testPatient = patientRepository.save(testPatient);
    }

    @Test
    void createPatient_Success() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
            "Jane",
            "Smith",
            LocalDate.of(1985, 6, 15),
            Patient.Gender.FEMALE,
            new BigDecimal("65.0"),
            new BigDecimal("165.0"),
            "New patient"
        );

        MvcResult result = mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.weightKg").value(65.0))
                .andExpect(jsonPath("$.heightCm").value(165.0))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        PatientResponse response = objectMapper.readValue(responseJson, PatientResponse.class);
        assertThat(response.id()).isNotNull();
    }

    @Test
    void createPatient_InvalidData_ReturnsBadRequest() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
            "", // Invalid empty first name
            "",
            LocalDate.now().plusDays(1), // Invalid future date
            null,
            null,
            null,
            null
        );

        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void createPatient_Duplicate_ReturnsConflict() throws Exception {
        PatientCreateRequest request = new PatientCreateRequest(
            "John", // Same as existing patient
            "Doe",
            LocalDate.of(1990, 1, 1),
            Patient.Gender.MALE,
            new BigDecimal("80.0"),
            new BigDecimal("175.0"),
            "Duplicate patient"
        );

        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_PATIENT"));
    }

    @Test
    void getActivePatients_Success() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalPages").exists());
    }

    @Test
    void getPatientById_Success() throws Exception {
        mockMvc.perform(get("/api/patients/" + testPatient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testPatient.getId().toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.ageInYears").isNumber())
                .andExpect(jsonPath("$.bmi").isNumber());
    }

    @Test
    void getPatientById_NotFound_ReturnsNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        
        mockMvc.perform(get("/api/patients/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PATIENT_NOT_FOUND"));
    }

    @Test
    void updatePatient_Success() throws Exception {
        PatientUpdateRequest request = new PatientUpdateRequest(
            "John",
            "Doe-Updated",
            LocalDate.of(1990, 1, 1),
            Patient.Gender.MALE,
            new BigDecimal("76.0"),
            new BigDecimal("181.0"),
            "Updated notes"
        );

        mockMvc.perform(put("/api/patients/" + testPatient.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Doe-Updated"))
                .andExpect(jsonPath("$.weightKg").value(76.0))
                .andExpect(jsonPath("$.notes").value("Updated notes"));
    }

    @Test
    void deletePatient_Success() throws Exception {
        mockMvc.perform(delete("/api/patients/" + testPatient.getId()))
                .andExpect(status().isNoContent());

        // Verify patient is soft deleted
        Patient deletedPatient = patientRepository.findById(testPatient.getId()).orElseThrow();
        assertThat(deletedPatient.isActive()).isFalse();
    }

    @Test
    void searchPatients_Success() throws Exception {
        mockMvc.perform(get("/api/patients/search")
                .param("q", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void findPatientsByAge_Success() throws Exception {
        mockMvc.perform(get("/api/patients/age-range")
                .param("minAge", "20")
                .param("maxAge", "40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getActivePatientCount_Success() throws Exception {
        mockMvc.perform(get("/api/patients/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    void checkPatientExists_True() throws Exception {
        mockMvc.perform(get("/api/patients/exists")
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("dateOfBirth", "1990-01-01"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkPatientExists_False() throws Exception {
        mockMvc.perform(get("/api/patients/exists")
                .param("firstName", "NonExistent")
                .param("lastName", "Patient")
                .param("dateOfBirth", "2000-01-01"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void getAllEndpoints_Unauthorized_ReturnsForbidden() throws Exception {
        // Clear security context to simulate unauthorized access
        SecurityContextHolder.clearContext();
        
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }
}