package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.dto.PatientCreateRequest;
import com.ciaranmckenna.medical_event_tracker.dto.PatientResponse;
import com.ciaranmckenna.medical_event_tracker.dto.PatientUpdateRequest;
import com.ciaranmckenna.medical_event_tracker.entity.Patient;
import com.ciaranmckenna.medical_event_tracker.entity.User;
import com.ciaranmckenna.medical_event_tracker.exception.DuplicatePatientException;
import com.ciaranmckenna.medical_event_tracker.exception.PatientNotFoundException;
import com.ciaranmckenna.medical_event_tracker.repository.PatientRepository;
import com.ciaranmckenna.medical_event_tracker.repository.PatientMedicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMedicationRepository patientMedicationRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    private User testUser;
    private Patient testPatient;
    private PatientCreateRequest createRequest;
    private PatientUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testPatient = new Patient(
            "John",
            "Doe",
            LocalDate.of(1990, 1, 1),
            Patient.Gender.MALE,
            testUser
        );
        // Note: ID is set by JPA, not manually in tests
        testPatient.setWeightKg(new BigDecimal("75.5"));
        testPatient.setHeightCm(new BigDecimal("180.0"));

        createRequest = new PatientCreateRequest(
            "Jane",
            "Smith",
            LocalDate.of(1985, 6, 15),
            Patient.Gender.FEMALE,
            new BigDecimal("65.0"),
            new BigDecimal("165.0"),
            "Test patient notes"
        );

        updateRequest = new PatientUpdateRequest(
            "Jane",
            "Smith-Updated",
            LocalDate.of(1985, 6, 15),
            Patient.Gender.FEMALE,
            new BigDecimal("66.0"),
            new BigDecimal("165.0"),
            "Updated notes"
        );
    }

    @Test
    void createPatient_Success() {
        // Given
        when(patientRepository.existsByUserAndFirstNameAndLastNameAndDateOfBirthAndActiveTrue(
            any(User.class), anyString(), anyString(), any(LocalDate.class))).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // When
        PatientResponse response = patientService.createPatient(createRequest, testUser);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.firstName()).isEqualTo(testPatient.getFirstName());
        assertThat(response.lastName()).isEqualTo(testPatient.getLastName());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void createPatient_DuplicateThrowsException() {
        // Given
        when(patientRepository.existsByUserAndFirstNameAndLastNameAndDateOfBirthAndActiveTrue(
            any(User.class), anyString(), anyString(), any(LocalDate.class))).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> patientService.createPatient(createRequest, testUser))
            .isInstanceOf(DuplicatePatientException.class)
            .hasMessageContaining("Jane Smith");
    }

    @Test
    void getActivePatients_Success() {
        // Given
        List<Patient> patients = Arrays.asList(testPatient);
        when(patientRepository.findByUserAndActiveTrue(testUser)).thenReturn(patients);
        when(patientMedicationRepository.countByPatientAndActiveTrue(testPatient)).thenReturn(2L);

        // When
        List<PatientResponse> responses = patientService.getActivePatients(testUser);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).activeMedicationCount()).isEqualTo(2);
    }

    @Test
    void getPatientById_Success() {
        // Given
        when(patientRepository.findByIdAndUser(testPatient.getId(), testUser))
            .thenReturn(Optional.of(testPatient));
        when(patientMedicationRepository.countByPatientAndActiveTrue(testPatient)).thenReturn(1L);

        // When
        PatientResponse response = patientService.getPatientById(testPatient.getId(), testUser);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(testPatient.getId());
        assertThat(response.activeMedicationCount()).isEqualTo(1);
    }

    @Test
    void getPatientById_NotFoundThrowsException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(patientRepository.findByIdAndUser(nonExistentId, testUser))
            .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> patientService.getPatientById(nonExistentId, testUser))
            .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void updatePatient_Success() {
        // Given
        when(patientRepository.findByIdAndUserAndActiveTrue(testPatient.getId(), testUser))
            .thenReturn(Optional.of(testPatient));
        when(patientRepository.existsByUserAndFirstNameAndLastNameAndDateOfBirthAndActiveTrue(
            any(User.class), anyString(), anyString(), any(LocalDate.class))).thenReturn(false);
        when(patientRepository.save(testPatient)).thenReturn(testPatient);
        when(patientMedicationRepository.countByPatientAndActiveTrue(testPatient)).thenReturn(0L);

        // When
        PatientResponse response = patientService.updatePatient(testPatient.getId(), updateRequest, testUser);

        // Then
        assertThat(response).isNotNull();
        verify(patientRepository).save(testPatient);
    }

    @Test
    void updatePatient_NotFoundThrowsException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(patientRepository.findByIdAndUserAndActiveTrue(nonExistentId, testUser))
            .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> patientService.updatePatient(nonExistentId, updateRequest, testUser))
            .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void deletePatient_Success() {
        // Given
        when(patientRepository.findByIdAndUserAndActiveTrue(testPatient.getId(), testUser))
            .thenReturn(Optional.of(testPatient));

        // When
        patientService.deletePatient(testPatient.getId(), testUser);

        // Then
        assertThat(testPatient.isActive()).isFalse();
        verify(patientRepository).save(testPatient);
    }

    @Test
    void deletePatient_NotFoundThrowsException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(patientRepository.findByIdAndUserAndActiveTrue(nonExistentId, testUser))
            .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> patientService.deletePatient(nonExistentId, testUser))
            .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void searchPatientsByName_Success() {
        // Given
        List<Patient> patients = Arrays.asList(testPatient);
        when(patientRepository.findByUserAndNameContaining(testUser, "John")).thenReturn(patients);
        when(patientMedicationRepository.countByPatientAndActiveTrue(testPatient)).thenReturn(1L);

        // When
        List<PatientResponse> responses = patientService.searchPatientsByName("John", testUser);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).firstName()).isEqualTo("John");
    }

    @Test
    void searchPatientsByName_EmptySearchReturnsActivePatients() {
        // Given
        List<Patient> patients = Arrays.asList(testPatient);
        when(patientRepository.findByUserAndActiveTrue(testUser)).thenReturn(patients);
        when(patientMedicationRepository.countByPatientAndActiveTrue(testPatient)).thenReturn(0L);

        // When
        List<PatientResponse> responses = patientService.searchPatientsByName("", testUser);

        // Then
        assertThat(responses).hasSize(1);
        verify(patientRepository).findByUserAndActiveTrue(testUser);
    }

    @Test
    void findPatientsByAgeRange_Success() {
        // Given
        List<Patient> patients = Arrays.asList(testPatient);
        when(patientRepository.findByUserAndDateOfBirthBetween(
            eq(testUser), any(LocalDate.class), any(LocalDate.class))).thenReturn(patients);
        when(patientMedicationRepository.countByPatientAndActiveTrue(testPatient)).thenReturn(0L);

        // When
        List<PatientResponse> responses = patientService.findPatientsByAgeRange(20, 40, testUser);

        // Then
        assertThat(responses).hasSize(1);
    }

    @Test
    void findPatientsByAgeRange_InvalidRangeThrowsException() {
        // When/Then
        assertThatThrownBy(() -> patientService.findPatientsByAgeRange(40, 20, testUser))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid age range");
    }

    @Test
    void getActivePatientCount_Success() {
        // Given
        when(patientRepository.countByUserAndActiveTrue(testUser)).thenReturn(5L);

        // When
        long count = patientService.getActivePatientCount(testUser);

        // Then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    void patientExists_True() {
        // Given
        when(patientRepository.existsByUserAndFirstNameAndLastNameAndDateOfBirthAndActiveTrue(
            testUser, "John", "Doe", LocalDate.of(1990, 1, 1))).thenReturn(true);

        // When
        boolean exists = patientService.patientExists("John", "Doe", LocalDate.of(1990, 1, 1), testUser);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void patientExists_False() {
        // Given
        when(patientRepository.existsByUserAndFirstNameAndLastNameAndDateOfBirthAndActiveTrue(
            testUser, "Jane", "Smith", LocalDate.of(1995, 1, 1))).thenReturn(false);

        // When
        boolean exists = patientService.patientExists("Jane", "Smith", LocalDate.of(1995, 1, 1), testUser);

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void getPatientsWithActiveMedications_Success() {
        // Given
        List<Patient> patients = Arrays.asList(testPatient);
        when(patientRepository.findByUserWithActiveMedications(testUser)).thenReturn(patients);
        when(patientMedicationRepository.countByPatientAndActiveTrue(testPatient)).thenReturn(3L);

        // When
        List<PatientResponse> responses = patientService.getPatientsWithActiveMedications(testUser);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).activeMedicationCount()).isEqualTo(3);
    }
}