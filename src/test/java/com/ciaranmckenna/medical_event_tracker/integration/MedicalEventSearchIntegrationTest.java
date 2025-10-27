package com.ciaranmckenna.medical_event_tracker.integration;

import com.ciaranmckenna.medical_event_tracker.dto.MedicalEventSearchRequest;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEvent;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import com.ciaranmckenna.medical_event_tracker.entity.User;
import com.ciaranmckenna.medical_event_tracker.repository.MedicalEventRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class MedicalEventSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MedicalEventRepository medicalEventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private UUID patientId;
    private UUID medicationId;
    private MedicalEvent testEvent1;
    private MedicalEvent testEvent2;

    @BeforeEach
    void setUp() {
        // Create and save test user
        testUser = new User();
        testUser.setUsername("medicaluser");
        testUser.setEmail("medicaluser@example.com");
        testUser.setPassword(passwordEncoder.encode("Password123!"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.Role.PRIMARY_USER);
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);

        // Set up security context
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(testUser, null, 
                java.util.List.of(new SimpleGrantedAuthority("ROLE_" + testUser.getRole().name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create test data
        patientId = UUID.randomUUID();
        medicationId = UUID.randomUUID();

        // Create test event 1 - Severe headache with medication
        testEvent1 = new MedicalEvent();
        testEvent1.setPatientId(patientId);
        testEvent1.setMedicationId(medicationId);
        testEvent1.setEventTime(LocalDateTime.now().minusHours(2));
        testEvent1.setTitle("Severe Headache");
        testEvent1.setDescription("Patient experienced severe headache after medication");
        testEvent1.setSeverity(MedicalEventSeverity.SEVERE);
        testEvent1.setCategory(MedicalEventCategory.SYMPTOM);
        testEvent1.setWeightKg(new BigDecimal("72.00")); // Weight at time of event
        testEvent1.setHeightCm(new BigDecimal("175.00")); // Height
        testEvent1.setDosageGiven(new BigDecimal("10.00")); // Medication dosage (10mg)
        testEvent1 = medicalEventRepository.save(testEvent1);

        // Create test event 2 - Medication administered, no linked medication ID
        testEvent2 = new MedicalEvent();
        testEvent2.setPatientId(patientId);
        testEvent2.setEventTime(LocalDateTime.now().minusHours(1));
        testEvent2.setTitle("Medication Administered");
        testEvent2.setDescription("Morning dose of prescribed medication taken");
        testEvent2.setSeverity(MedicalEventSeverity.MILD);
        testEvent2.setCategory(MedicalEventCategory.MEDICATION);
        testEvent2.setWeightKg(new BigDecimal("71.50")); // Weight at time of event
        testEvent2.setHeightCm(new BigDecimal("175.00")); // Height
        testEvent2.setDosageGiven(new BigDecimal("5.00")); // Medication dosage (5mg)
        testEvent2 = medicalEventRepository.save(testEvent2);
    }

    @Test
    void searchMedicalEvents_Success_WithTextSearch() throws Exception {
        // Given
        MedicalEventSearchRequest searchRequest = new MedicalEventSearchRequest(
                patientId,
                "headache",
                null,
                null,
                null,
                null,
                null,
                0,
                10,
                "eventTime",
                "DESC"
        );

        // When/Then
        mockMvc.perform(post("/api/medical-events/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Severe Headache"))
                .andExpect(jsonPath("$.content[0].severity").value("SEVERE"))
                .andExpect(jsonPath("$.content[0].category").value("SYMPTOM"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.sortBy").value("eventTime"))
                .andExpect(jsonPath("$.sortDirection").value("DESC"));
    }

    @Test
    void searchMedicalEvents_Success_WithCategoryFilter() throws Exception {
        // Given
        MedicalEventSearchRequest searchRequest = new MedicalEventSearchRequest(
                patientId,
                null,
                List.of(MedicalEventCategory.MEDICATION),
                null,
                null,
                null,
                null,
                0,
                10,
                "eventTime",
                "DESC"
        );

        // When/Then
        mockMvc.perform(post("/api/medical-events/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Medication Administered"))
                .andExpect(jsonPath("$.content[0].category").value("MEDICATION"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchMedicalEvents_Success_WithSeverityFilter() throws Exception {
        // Given
        MedicalEventSearchRequest searchRequest = new MedicalEventSearchRequest(
                patientId,
                null,
                null,
                List.of(MedicalEventSeverity.SEVERE),
                null,
                null,
                null,
                0,
                10,
                "eventTime",
                "DESC"
        );

        // When/Then
        mockMvc.perform(post("/api/medical-events/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].severity").value("SEVERE"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchMedicalEvents_Success_WithDateRangeFilter() throws Exception {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusHours(3);
        LocalDateTime endDate = LocalDateTime.now().minusMinutes(30);
        
        MedicalEventSearchRequest searchRequest = new MedicalEventSearchRequest(
                patientId,
                null,
                null,
                null,
                null,
                startDate,
                endDate,
                0,
                10,
                "eventTime",
                "DESC"
        );

        // When/Then
        mockMvc.perform(post("/api/medical-events/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void searchMedicalEvents_Success_WithPagination() throws Exception {
        // Given
        MedicalEventSearchRequest searchRequest = new MedicalEventSearchRequest(
                patientId,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                1,
                "eventTime",
                "DESC"
        );

        // When/Then
        mockMvc.perform(post("/api/medical-events/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").value(org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    @Test
    void getMedicalEventsPaginated_Success() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/medical-events/patient/{patientId}/paginated", patientId)
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "eventTime")
                .param("sortDirection", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").value(org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.sortBy").value("eventTime"))
                .andExpect(jsonPath("$.sortDirection").value("DESC"));
    }

    @Test
    void getMedicalEventStatistics_Success() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/medical-events/patient/{patientId}/statistics", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.totalEvents").value(2))
                .andExpect(jsonPath("$.symptomEvents").value(1))
                .andExpect(jsonPath("$.medicationEvents").value(1))
                .andExpect(jsonPath("$.emergencyEvents").value(0))
                .andExpect(jsonPath("$.recentEvents").value(2))
                .andExpect(jsonPath("$.generatedAt").exists());
    }

    @Test
    void searchMedicalEvents_BadRequest_WithInvalidDateRange() throws Exception {
        // Given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);
        
        MedicalEventSearchRequest searchRequest = new MedicalEventSearchRequest(
                patientId,
                null,
                null,
                null,
                null,
                startDate,
                endDate,
                0,
                10,
                "eventTime",
                "DESC"
        );

        // When/Then
        mockMvc.perform(post("/api/medical-events/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchRequest)))
                .andExpect(status().isBadRequest());
    }
}