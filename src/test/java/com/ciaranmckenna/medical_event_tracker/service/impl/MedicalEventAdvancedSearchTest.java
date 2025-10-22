package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.dto.MedicalEventSearchRequest;
import com.ciaranmckenna.medical_event_tracker.dto.PagedMedicalEventResponse;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEvent;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import com.ciaranmckenna.medical_event_tracker.exception.InvalidMedicalDataException;
import com.ciaranmckenna.medical_event_tracker.repository.MedicalEventRepository;
import com.ciaranmckenna.medical_event_tracker.repository.MedicalEventSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalEventAdvancedSearchTest {

    @Mock
    private MedicalEventRepository medicalEventRepository;

    @InjectMocks
    private MedicalEventServiceImpl medicalEventService;

    private UUID patientId;
    private UUID medicationId;
    private MedicalEvent testEvent1;
    private MedicalEvent testEvent2;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        medicationId = UUID.randomUUID();

        testEvent1 = new MedicalEvent();
        testEvent1.setId(UUID.randomUUID());
        testEvent1.setPatientId(patientId);
        testEvent1.setMedicationId(medicationId);
        testEvent1.setEventTime(LocalDateTime.now().minusHours(2));
        testEvent1.setTitle("Severe Headache");
        testEvent1.setDescription("Patient experienced severe headache after medication");
        testEvent1.setSeverity(MedicalEventSeverity.SEVERE);
        testEvent1.setCategory(MedicalEventCategory.SYMPTOM);
        testEvent1.setCreatedAt(LocalDateTime.now().minusHours(2));
        testEvent1.setUpdatedAt(LocalDateTime.now().minusHours(2));

        testEvent2 = new MedicalEvent();
        testEvent2.setId(UUID.randomUUID());
        testEvent2.setPatientId(patientId);
        testEvent2.setEventTime(LocalDateTime.now().minusHours(1));
        testEvent2.setTitle("Medication Taken");
        testEvent2.setDescription("Morning dose of medication administered");
        testEvent2.setSeverity(MedicalEventSeverity.MILD);
        testEvent2.setCategory(MedicalEventCategory.MEDICATION);
        testEvent2.setCreatedAt(LocalDateTime.now().minusHours(1));
        testEvent2.setUpdatedAt(LocalDateTime.now().minusHours(1));
    }

    @Test
    void searchMedicalEvents_Success_WithAllFilters() {
        // Given
        MedicalEventSearchRequest searchRequest = new MedicalEventSearchRequest(
                patientId,
                "headache",
                List.of(MedicalEventCategory.SYMPTOM),
                List.of(MedicalEventSeverity.SEVERE),
                List.of(medicationId),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                0,
                10,
                "eventTime",
                "DESC"
        );

        List<MedicalEvent> events = Arrays.asList(testEvent1);
        Page<MedicalEvent> eventPage = new PageImpl<>(events, PageRequest.of(0, 10), 1);
        
        when(medicalEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(eventPage);

        // When
        PagedMedicalEventResponse response = medicalEventService.searchMedicalEvents(searchRequest);

        // Then
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).title()).isEqualTo("Severe Headache");
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.first()).isTrue();
        assertThat(response.last()).isTrue();
        
        verify(medicalEventRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchMedicalEvents_Success_WithTextSearchOnly() {
        // Given
        MedicalEventSearchRequest searchRequest = new MedicalEventSearchRequest(
                patientId,
                "medication",
                null,
                null,
                null,
                null,
                null,
                0,
                20,
                "eventTime",
                "DESC"
        );

        List<MedicalEvent> events = Arrays.asList(testEvent1, testEvent2);
        Page<MedicalEvent> eventPage = new PageImpl<>(events, PageRequest.of(0, 20), 2);
        
        when(medicalEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(eventPage);

        // When
        PagedMedicalEventResponse response = medicalEventService.searchMedicalEvents(searchRequest);

        // Then
        assertThat(response.content()).hasSize(2);
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.hasPrevious()).isFalse();
        
        verify(medicalEventRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchMedicalEvents_Success_WithPagination() {
        // Given
        MedicalEventSearchRequest searchRequest = new MedicalEventSearchRequest(
                patientId,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                1,
                "eventTime",
                "DESC"
        );

        List<MedicalEvent> events = Arrays.asList(testEvent2);
        Page<MedicalEvent> eventPage = new PageImpl<>(events, PageRequest.of(1, 1), 2);
        
        when(medicalEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(eventPage);

        // When
        PagedMedicalEventResponse response = medicalEventService.searchMedicalEvents(searchRequest);

        // Then
        assertThat(response.content()).hasSize(1);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.first()).isFalse();
        assertThat(response.last()).isTrue();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.hasPrevious()).isTrue();
        
        verify(medicalEventRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchMedicalEvents_ThrowsException_WhenRequestIsNull() {
        // When & Then
        assertThatThrownBy(() -> medicalEventService.searchMedicalEvents(null))
                .isInstanceOf(InvalidMedicalDataException.class)
                .hasMessage("Search request cannot be null");
    }

    @Test
    void searchMedicalEvents_WorksWithNullPatientId() {
        // Given - patientId is now optional, should search across all patients
        MedicalEventSearchRequest searchRequest = new MedicalEventSearchRequest(
                null, "test", null, null, null, null, null, 0, 20, "eventTime", "DESC"
        );

        Page<MedicalEvent> mockPage = new PageImpl<>(List.of(testEvent1, testEvent2));
        when(medicalEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockPage);

        // When
        PagedMedicalEventResponse response = medicalEventService.searchMedicalEvents(searchRequest);

        // Then - should successfully return results across all patients
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(2);
        verify(medicalEventRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchMedicalEvents_ThrowsException_WhenDateRangeInvalid() {
        // Given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);
        
        MedicalEventSearchRequest searchRequest = new MedicalEventSearchRequest(
                patientId, null, null, null, null, startDate, endDate, 0, 20, "eventTime", "DESC"
        );

        // When & Then
        assertThatThrownBy(() -> medicalEventService.searchMedicalEvents(searchRequest))
                .isInstanceOf(InvalidMedicalDataException.class)
                .hasMessage("Invalid date range: start date must be before or equal to end date");
    }

    @Test
    void getMedicalEventsByPatientIdPaginated_Success() {
        // Given
        List<MedicalEvent> events = Arrays.asList(testEvent1, testEvent2);
        Page<MedicalEvent> eventPage = new PageImpl<>(events, PageRequest.of(0, 20), 2);
        
        when(medicalEventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(eventPage);

        // When
        PagedMedicalEventResponse response = medicalEventService.getMedicalEventsByPatientIdPaginated(
                patientId, 0, 20, "eventTime", "DESC");

        // Then
        assertThat(response.content()).hasSize(2);
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.sortBy()).isEqualTo("eventTime");
        assertThat(response.sortDirection()).isEqualTo("DESC");
        
        verify(medicalEventRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getMedicalEventsByPatientIdPaginated_ThrowsException_WhenPatientIdIsNull() {
        // When & Then
        assertThatThrownBy(() -> medicalEventService.getMedicalEventsByPatientIdPaginated(
                null, 0, 20, "eventTime", "DESC"))
                .isInstanceOf(InvalidMedicalDataException.class)
                .hasMessage("Patient ID cannot be null");
    }
}