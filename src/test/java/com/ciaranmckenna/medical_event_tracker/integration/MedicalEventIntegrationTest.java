package com.ciaranmckenna.medical_event_tracker.integration;

import com.ciaranmckenna.medical_event_tracker.dto.CreateMedicalEventRequest;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import com.ciaranmckenna.medical_event_tracker.entity.User;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class MedicalEventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create and save test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
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
    }

    @Test
    void createMedicalEvent_Success() throws Exception {
        // Given
        CreateMedicalEventRequest request = new CreateMedicalEventRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now().minusHours(1),
                "Test Headache",
                "Patient reported mild headache after lunch",
                MedicalEventSeverity.MILD,
                MedicalEventCategory.SYMPTOM
        );

        // When/Then
        mockMvc.perform(post("/api/medical-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(request.patientId().toString()))
                .andExpect(jsonPath("$.title").value("Test Headache"))
                .andExpect(jsonPath("$.severity").value("MILD"))
                .andExpect(jsonPath("$.category").value("SYMPTOM"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void getMedicalEventsByPatientId_ReturnsEvents() throws Exception {
        // Given
        UUID patientId = UUID.randomUUID();

        // Create an event first
        CreateMedicalEventRequest request = new CreateMedicalEventRequest(
                patientId,
                null,
                LocalDateTime.now().minusHours(2),
                "Morning Fever",
                "Patient had elevated temperature",
                MedicalEventSeverity.MODERATE,
                MedicalEventCategory.SYMPTOM
        );

        // Create the event
        mockMvc.perform(post("/api/medical-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // When/Then - Get events for patient
        mockMvc.perform(get("/api/medical-events/patient/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patientId").value(patientId.toString()))
                .andExpect(jsonPath("$[0].title").value("Morning Fever"));
    }

    @Test
    void createMedicalEvent_InvalidData_ReturnsBadRequest() throws Exception {
        // Given - invalid request with null title
        CreateMedicalEventRequest invalidRequest = new CreateMedicalEventRequest(
                UUID.randomUUID(),
                null,
                LocalDateTime.now().minusHours(1),
                null, // Invalid: null title
                "Description",
                MedicalEventSeverity.MILD,
                MedicalEventCategory.SYMPTOM
        );

        // When/Then
        mockMvc.perform(post("/api/medical-events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMedicalEventById_NotFound_ReturnsNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When/Then
        mockMvc.perform(get("/api/medical-events/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }
}