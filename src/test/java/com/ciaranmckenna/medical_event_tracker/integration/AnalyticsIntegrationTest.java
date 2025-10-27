package com.ciaranmckenna.medical_event_tracker.integration;

import com.ciaranmckenna.medical_event_tracker.entity.*;
import com.ciaranmckenna.medical_event_tracker.repository.MedicalEventRepository;
import com.ciaranmckenna.medical_event_tracker.repository.MedicationDosageRepository;
import com.ciaranmckenna.medical_event_tracker.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for analytics endpoints.
 * Tests the complete analytics functionality with real data and database integration.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AnalyticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MedicalEventRepository medicalEventRepository;

    @Autowired
    private MedicationDosageRepository medicationDosageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private UUID patientId;
    private UUID medicationId;

    @BeforeEach
    void setUp() {
        // Create and save test user
        testUser = new User();
        testUser.setUsername("analyticsuser");
        testUser.setEmail("analytics@example.com");
        testUser.setPassword(passwordEncoder.encode("Password123!"));
        testUser.setFirstName("Analytics");
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

        // Create medication dosages
        createTestMedicationDosages();
        
        // Create medical events
        createTestMedicalEvents();
    }

    @Test
    void getDashboardSummary_Success() throws Exception {
        mockMvc.perform(get("/api/analytics/dashboard/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.totalEvents").value(4))
                .andExpect(jsonPath("$.totalDosages").value(3))
                .andExpect(jsonPath("$.eventsByCategory").exists())
                .andExpect(jsonPath("$.eventsBySeverity").exists())
                .andExpect(jsonPath("$.generatedAt").exists());
    }

    @Test
    void getMedicationCorrelationAnalysis_Success() throws Exception {
        mockMvc.perform(get("/api/analytics/correlation/{patientId}/medication/{medicationId}", 
                        patientId, medicationId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.medicationId").value(medicationId.toString()))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.totalDosages").value(3))
                .andExpect(jsonPath("$.correlationPercentage").exists())
                .andExpect(jsonPath("$.correlationStrength").exists())
                .andExpect(jsonPath("$.eventsByCategoryCount").exists())
                .andExpect(jsonPath("$.eventsBySeverityCount").exists())
                .andExpect(jsonPath("$.analysisGeneratedAt").exists());
    }

    @Test
    void getAllMedicationCorrelations_Success() throws Exception {
        mockMvc.perform(get("/api/analytics/correlation/{patientId}/all-medications", patientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patientId").value(patientId.toString()));
    }

    @Test
    void getTimelineAnalysis_Success() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        mockMvc.perform(get("/api/analytics/timeline/{patientId}", patientId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.periodStart").exists())
                .andExpect(jsonPath("$.periodEnd").exists())
                .andExpect(jsonPath("$.dataPoints").isArray())
                .andExpect(jsonPath("$.generatedAt").exists());
    }

    @Test
    void getMedicationImpactAnalysis_Success() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        mockMvc.perform(get("/api/analytics/impact/{patientId}/medication/{medicationId}", 
                        patientId, medicationId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.medicationId").value(medicationId.toString()))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.analysisPeriodStart").exists())
                .andExpect(jsonPath("$.analysisPeriodEnd").exists())
                .andExpect(jsonPath("$.totalDosages").exists())
                .andExpect(jsonPath("$.effectivenessScore").exists())
                .andExpect(jsonPath("$.analysisGeneratedAt").exists());
    }

    @Test
    void getWeeklyTrends_Success() throws Exception {
        mockMvc.perform(get("/api/analytics/weekly-trends/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$['Week 1']").exists())
                .andExpect(jsonPath("$['Week 2']").exists())
                .andExpect(jsonPath("$['Week 1'].patientId").value(patientId.toString()));
    }

    @Test
    void getAnalyticsOverview_Success() throws Exception {
        mockMvc.perform(get("/api/analytics/overview/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.dashboardSummary").exists())
                .andExpect(jsonPath("$.medicationCorrelations").isArray())
                .andExpect(jsonPath("$.generatedAt").exists())
                .andExpect(jsonPath("$.dashboardSummary.patientId").value(patientId.toString()));
    }

    private void createTestMedicationDosages() {
        // Create dosage 1 - 6 hours ago
        MedicationDosage dosage1 = new MedicationDosage();
        dosage1.setPatientId(patientId);
        dosage1.setMedicationId(medicationId);
        dosage1.setAdministrationTime(LocalDateTime.now().minusHours(6));
        dosage1.setDosageAmount(new BigDecimal("500.0"));
        dosage1.setDosageUnit("mg");
        dosage1.setSchedule(DosageSchedule.AM);
        dosage1.setAdministered(true);
        medicationDosageRepository.save(dosage1);

        // Create dosage 2 - 12 hours ago
        MedicationDosage dosage2 = new MedicationDosage();
        dosage2.setPatientId(patientId);
        dosage2.setMedicationId(medicationId);
        dosage2.setAdministrationTime(LocalDateTime.now().minusHours(12));
        dosage2.setDosageAmount(new BigDecimal("500.0"));
        dosage2.setDosageUnit("mg");
        dosage2.setSchedule(DosageSchedule.PM);
        dosage2.setAdministered(true);
        medicationDosageRepository.save(dosage2);

        // Create dosage 3 - 18 hours ago
        MedicationDosage dosage3 = new MedicationDosage();
        dosage3.setPatientId(patientId);
        dosage3.setMedicationId(medicationId);
        dosage3.setAdministrationTime(LocalDateTime.now().minusHours(18));
        dosage3.setDosageAmount(new BigDecimal("500.0"));
        dosage3.setDosageUnit("mg");
        dosage3.setSchedule(DosageSchedule.AM);
        dosage3.setAdministered(true);
        medicationDosageRepository.save(dosage3);
    }

    private void createTestMedicalEvents() {
        // Create symptom event - 4 hours ago (after dosage 1)
        MedicalEvent symptomEvent = new MedicalEvent();
        symptomEvent.setPatientId(patientId);
        symptomEvent.setMedicationId(medicationId);
        symptomEvent.setEventTime(LocalDateTime.now().minusHours(4));
        symptomEvent.setTitle("Mild headache");
        symptomEvent.setDescription("Patient reported mild headache 2 hours after morning medication");
        symptomEvent.setSeverity(MedicalEventSeverity.MILD);
        symptomEvent.setCategory(MedicalEventCategory.SYMPTOM);
        symptomEvent.setWeightKg(new BigDecimal("70.50"));
        symptomEvent.setHeightCm(new BigDecimal("175.00"));
        symptomEvent.setDosageGiven(new BigDecimal("5.00"));
        medicalEventRepository.save(symptomEvent);

        // Create adverse reaction - 10 hours ago (after dosage 2)
        MedicalEvent adverseEvent = new MedicalEvent();
        adverseEvent.setPatientId(patientId);
        adverseEvent.setMedicationId(medicationId);
        adverseEvent.setEventTime(LocalDateTime.now().minusHours(10));
        adverseEvent.setTitle("Nausea");
        adverseEvent.setDescription("Patient experienced nausea 2 hours after evening medication");
        adverseEvent.setSeverity(MedicalEventSeverity.MODERATE);
        adverseEvent.setCategory(MedicalEventCategory.ADVERSE_REACTION);
        adverseEvent.setWeightKg(new BigDecimal("70.00"));
        adverseEvent.setHeightCm(new BigDecimal("175.00"));
        adverseEvent.setDosageGiven(new BigDecimal("7.50"));
        medicalEventRepository.save(adverseEvent);

        // Create observation - 16 hours ago (after dosage 3)
        MedicalEvent observationEvent = new MedicalEvent();
        observationEvent.setPatientId(patientId);
        observationEvent.setEventTime(LocalDateTime.now().minusHours(16));
        observationEvent.setTitle("Improved mood");
        observationEvent.setDescription("Patient showed improved mood and energy levels");
        observationEvent.setSeverity(MedicalEventSeverity.MILD);
        observationEvent.setCategory(MedicalEventCategory.OBSERVATION);
        observationEvent.setWeightKg(new BigDecimal("71.00"));
        observationEvent.setHeightCm(new BigDecimal("175.00"));
        observationEvent.setDosageGiven(new BigDecimal("0.00")); // No medication for observation
        medicalEventRepository.save(observationEvent);

        // Create emergency event - 2 days ago (unrelated to current medication)
        MedicalEvent emergencyEvent = new MedicalEvent();
        emergencyEvent.setPatientId(patientId);
        emergencyEvent.setEventTime(LocalDateTime.now().minusDays(2));
        emergencyEvent.setTitle("Severe reaction");
        emergencyEvent.setDescription("Patient had severe allergic reaction requiring immediate attention");
        emergencyEvent.setSeverity(MedicalEventSeverity.CRITICAL);
        emergencyEvent.setCategory(MedicalEventCategory.EMERGENCY);
        emergencyEvent.setWeightKg(new BigDecimal("69.50"));
        emergencyEvent.setHeightCm(new BigDecimal("175.00"));
        emergencyEvent.setDosageGiven(new BigDecimal("0.00")); // No medication during emergency
        medicalEventRepository.save(emergencyEvent);
    }
}