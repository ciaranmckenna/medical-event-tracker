package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.entity.MedicalEvent;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import com.ciaranmckenna.medical_event_tracker.exception.InvalidMedicalDataException;
import com.ciaranmckenna.medical_event_tracker.repository.MedicalEventRepository;
import com.ciaranmckenna.medical_event_tracker.service.MedicalEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalEventServiceImplTest {

    @Mock
    private MedicalEventRepository medicalEventRepository;

    @InjectMocks
    private MedicalEventServiceImpl medicalEventService;

    private MedicalEvent testEvent;
    private UUID patientId;
    private UUID medicationId;

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
    }

    @Test
    void createMedicalEvent_Success() {
        // Given
        when(medicalEventRepository.save(any(MedicalEvent.class))).thenReturn(testEvent);

        // When
        MedicalEvent result = medicalEventService.createMedicalEvent(testEvent);

        // Then
        assertThat(result).isEqualTo(testEvent);
        verify(medicalEventRepository).save(testEvent);
    }

    @Test
    void createMedicalEvent_NullEvent_ThrowsException() {
        // When/Then
        assertThatThrownBy(() -> medicalEventService.createMedicalEvent(null))
                .isInstanceOf(InvalidMedicalDataException.class)
                .hasMessage("Medical event cannot be null");
        
        verify(medicalEventRepository, never()).save(any());
    }

    @Test
    void getMedicalEventById_Found_ReturnsEvent() {
        // Given
        UUID eventId = testEvent.getId();
        when(medicalEventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));

        // When
        Optional<MedicalEvent> result = medicalEventService.getMedicalEventById(eventId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testEvent);
        verify(medicalEventRepository).findById(eventId);
    }

    @Test
    void getMedicalEventById_NotFound_ReturnsEmpty() {
        // Given
        UUID eventId = UUID.randomUUID();
        when(medicalEventRepository.findById(eventId)).thenReturn(Optional.empty());

        // When
        Optional<MedicalEvent> result = medicalEventService.getMedicalEventById(eventId);

        // Then
        assertThat(result).isEmpty();
        verify(medicalEventRepository).findById(eventId);
    }

    @Test
    void getMedicalEventsByPatientId_ReturnsEvents() {
        // Given
        List<MedicalEvent> events = Arrays.asList(testEvent);
        when(medicalEventRepository.findByPatientIdOrderByEventTimeDesc(patientId)).thenReturn(events);

        // When
        List<MedicalEvent> result = medicalEventService.getMedicalEventsByPatientId(patientId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testEvent);
        verify(medicalEventRepository).findByPatientIdOrderByEventTimeDesc(patientId);
    }

    @Test
    void updateMedicalEvent_Success() {
        // Given
        testEvent.setTitle("Updated Title");
        when(medicalEventRepository.existsById(testEvent.getId())).thenReturn(true);
        when(medicalEventRepository.save(testEvent)).thenReturn(testEvent);

        // When
        MedicalEvent result = medicalEventService.updateMedicalEvent(testEvent);

        // Then
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        verify(medicalEventRepository).existsById(testEvent.getId());
        verify(medicalEventRepository).save(testEvent);
    }

    @Test
    void updateMedicalEvent_NotFound_ThrowsException() {
        // Given
        when(medicalEventRepository.existsById(testEvent.getId())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> medicalEventService.updateMedicalEvent(testEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Medical event not found with id: " + testEvent.getId());
        
        verify(medicalEventRepository).existsById(testEvent.getId());
        verify(medicalEventRepository, never()).save(any());
    }

    @Test
    void deleteMedicalEvent_Success() {
        // Given
        UUID eventId = testEvent.getId();
        when(medicalEventRepository.existsById(eventId)).thenReturn(true);

        // When
        medicalEventService.deleteMedicalEvent(eventId);

        // Then
        verify(medicalEventRepository).existsById(eventId);
        verify(medicalEventRepository).deleteById(eventId);
    }

    @Test
    void deleteMedicalEvent_NotFound_ThrowsException() {
        // Given
        UUID eventId = UUID.randomUUID();
        when(medicalEventRepository.existsById(eventId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> medicalEventService.deleteMedicalEvent(eventId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Medical event not found with id: " + eventId);
        
        verify(medicalEventRepository).existsById(eventId);
        verify(medicalEventRepository, never()).deleteById(any());
    }

    @Test
    void getMedicalEventsByPatientIdAndDateRange_ReturnsFilteredEvents() {
        // Given
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        List<MedicalEvent> events = Arrays.asList(testEvent);
        
        when(medicalEventRepository.findByPatientIdAndEventTimeBetween(patientId, startTime, endTime))
                .thenReturn(events);

        // When
        List<MedicalEvent> result = medicalEventService.getMedicalEventsByPatientIdAndDateRange(
                patientId, startTime, endTime);

        // Then
        assertThat(result).hasSize(1);
        verify(medicalEventRepository).findByPatientIdAndEventTimeBetween(patientId, startTime, endTime);
    }

    @Test
    void getMedicalEventsByPatientIdAndCategory_ReturnsFilteredEvents() {
        // Given
        List<MedicalEvent> events = Arrays.asList(testEvent);
        when(medicalEventRepository.findByPatientIdAndCategory(patientId, MedicalEventCategory.SYMPTOM))
                .thenReturn(events);

        // When
        List<MedicalEvent> result = medicalEventService.getMedicalEventsByPatientIdAndCategory(
                patientId, MedicalEventCategory.SYMPTOM);

        // Then
        assertThat(result).hasSize(1);
        verify(medicalEventRepository).findByPatientIdAndCategory(patientId, MedicalEventCategory.SYMPTOM);
    }
}