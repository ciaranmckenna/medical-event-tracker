package com.ciaranmckenna.medical_event_tracker.controller;

import com.ciaranmckenna.medical_event_tracker.dto.CreateMedicalEventRequest;
import com.ciaranmckenna.medical_event_tracker.dto.MedicalEventResponse;
import com.ciaranmckenna.medical_event_tracker.dto.UpdateMedicalEventRequest;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEvent;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import com.ciaranmckenna.medical_event_tracker.exception.MedicalEventNotFoundException;
import com.ciaranmckenna.medical_event_tracker.service.MedicalEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MedicalEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MedicalEventService medicalEventService;

    private MedicalEvent testEvent;
    private UUID patientId;
    private UUID medicationId;
    private CreateMedicalEventRequest createRequest;
    private UpdateMedicalEventRequest updateRequest;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        medicationId = UUID.randomUUID();
        
        testEvent = new MedicalEvent();
        testEvent.setId(UUID.randomUUID());
        testEvent.setPatientId(patientId);
        testEvent.setMedicationId(medicationId);
        testEvent.setEventTime(LocalDateTime.now().minusHours(2));
        testEvent.setTitle("Test Fever");
        testEvent.setDescription("Patient experienced elevated temperature");
        testEvent.setSeverity(MedicalEventSeverity.MODERATE);
        testEvent.setCategory(MedicalEventCategory.SYMPTOM);
        testEvent.setCreatedAt(LocalDateTime.now().minusHours(1));
        testEvent.setUpdatedAt(LocalDateTime.now().minusMinutes(30));

        createRequest = new CreateMedicalEventRequest(
                patientId,
                medicationId,
                testEvent.getEventTime(),
                testEvent.getTitle(),
                testEvent.getDescription(),
                testEvent.getSeverity(),
                testEvent.getCategory()
        );

        updateRequest = new UpdateMedicalEventRequest(
                testEvent.getId(),
                patientId,
                medicationId,
                testEvent.getEventTime(),
                "Updated Title",
                "Updated description",
                MedicalEventSeverity.SEVERE,
                MedicalEventCategory.EMERGENCY
        );
    }

    @Test
    @WithMockUser(roles = "PRIMARY_USER")
    void createMedicalEvent_Success() throws Exception {
        // Given
        when(medicalEventService.createMedicalEvent(any(MedicalEvent.class))).thenReturn(testEvent);

        // When/Then
        mockMvc.perform(post("/api/medical-events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testEvent.getId().toString()))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.title").value("Test Fever"))
                .andExpect(jsonPath("$.severity").value("MODERATE"))
                .andExpect(jsonPath("$.category").value("SYMPTOM"));

        verify(medicalEventService).createMedicalEvent(any(MedicalEvent.class));
    }

    @Test
    @WithMockUser(roles = "PRIMARY_USER")
    void createMedicalEvent_InvalidInput_ReturnsBadRequest() throws Exception {
        // Given - invalid request with null title
        CreateMedicalEventRequest invalidRequest = new CreateMedicalEventRequest(
                patientId, medicationId, LocalDateTime.now(), null, "desc", 
                MedicalEventSeverity.MILD, MedicalEventCategory.SYMPTOM
        );

        // When/Then
        mockMvc.perform(post("/api/medical-events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(medicalEventService, never()).createMedicalEvent(any());
    }

    @Test
    @WithMockUser(roles = "PRIMARY_USER")
    void getMedicalEventById_Found_ReturnsEvent() throws Exception {
        // Given
        UUID eventId = testEvent.getId();
        when(medicalEventService.getMedicalEventById(eventId)).thenReturn(Optional.of(testEvent));

        // When/Then
        mockMvc.perform(get("/api/medical-events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.title").value("Test Fever"));

        verify(medicalEventService).getMedicalEventById(eventId);
    }

    @Test
    @WithMockUser(roles = "PRIMARY_USER")
    void getMedicalEventById_NotFound_ReturnsNotFound() throws Exception {
        // Given
        UUID eventId = UUID.randomUUID();
        when(medicalEventService.getMedicalEventById(eventId)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/medical-events/{id}", eventId))
                .andExpect(status().isNotFound());

        verify(medicalEventService).getMedicalEventById(eventId);
    }

    @Test
    @WithMockUser(roles = "PRIMARY_USER")
    void getMedicalEventsByPatientId_ReturnsEvents() throws Exception {
        // Given
        List<MedicalEvent> events = Arrays.asList(testEvent);
        when(medicalEventService.getMedicalEventsByPatientId(patientId)).thenReturn(events);

        // When/Then
        mockMvc.perform(get("/api/medical-events/patient/{patientId}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testEvent.getId().toString()))
                .andExpect(jsonPath("$[0].title").value("Test Fever"));

        verify(medicalEventService).getMedicalEventsByPatientId(patientId);
    }

    @Test
    @WithMockUser(roles = "PRIMARY_USER")
    void updateMedicalEvent_Success() throws Exception {
        // Given
        MedicalEvent updatedEvent = new MedicalEvent();
        updatedEvent.setId(testEvent.getId());
        updatedEvent.setPatientId(patientId);
        updatedEvent.setTitle("Updated Title");
        updatedEvent.setSeverity(MedicalEventSeverity.SEVERE);
        updatedEvent.setCategory(MedicalEventCategory.EMERGENCY);
        
        when(medicalEventService.updateMedicalEvent(any(MedicalEvent.class))).thenReturn(updatedEvent);

        // When/Then
        mockMvc.perform(put("/api/medical-events/{id}", testEvent.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEvent.getId().toString()))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.severity").value("SEVERE"))
                .andExpect(jsonPath("$.category").value("EMERGENCY"));

        verify(medicalEventService).updateMedicalEvent(any(MedicalEvent.class));
    }

    @Test
    @WithMockUser(roles = "PRIMARY_USER")
    void updateMedicalEvent_NotFound_ReturnsNotFound() throws Exception {
        // Given
        when(medicalEventService.updateMedicalEvent(any(MedicalEvent.class)))
                .thenThrow(new MedicalEventNotFoundException(testEvent.getId()));

        // When/Then
        mockMvc.perform(put("/api/medical-events/{id}", testEvent.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(medicalEventService).updateMedicalEvent(any(MedicalEvent.class));
    }

    @Test
    @WithMockUser(roles = "PRIMARY_USER")
    void deleteMedicalEvent_Success() throws Exception {
        // Given
        UUID eventId = testEvent.getId();
        doNothing().when(medicalEventService).deleteMedicalEvent(eventId);

        // When/Then
        mockMvc.perform(delete("/api/medical-events/{id}", eventId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(medicalEventService).deleteMedicalEvent(eventId);
    }

    @Test
    @WithMockUser(roles = "PRIMARY_USER")
    void deleteMedicalEvent_NotFound_ReturnsNotFound() throws Exception {
        // Given
        UUID eventId = UUID.randomUUID();
        doThrow(new MedicalEventNotFoundException(eventId))
                .when(medicalEventService).deleteMedicalEvent(eventId);

        // When/Then
        mockMvc.perform(delete("/api/medical-events/{id}", eventId)
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(medicalEventService).deleteMedicalEvent(eventId);
    }

    @Test
    void getMedicalEventById_Unauthorized_ReturnsForbidden() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/medical-events/{id}", UUID.randomUUID()))
                .andExpect(status().isForbidden());

        verify(medicalEventService, never()).getMedicalEventById(any());
    }
}