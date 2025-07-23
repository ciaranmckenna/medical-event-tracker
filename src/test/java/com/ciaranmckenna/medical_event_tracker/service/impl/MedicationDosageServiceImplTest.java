package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.entity.DosageSchedule;
import com.ciaranmckenna.medical_event_tracker.entity.MedicationDosage;
import com.ciaranmckenna.medical_event_tracker.exception.InvalidMedicalDataException;
import com.ciaranmckenna.medical_event_tracker.repository.MedicationDosageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationDosageServiceImplTest {

    @Mock
    private MedicationDosageRepository medicationDosageRepository;

    @InjectMocks
    private MedicationDosageServiceImpl medicationDosageService;

    private MedicationDosage testDosage;
    private UUID patientId;
    private UUID medicationId;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        medicationId = UUID.randomUUID();
        
        testDosage = new MedicationDosage();
        testDosage.setId(UUID.randomUUID());
        testDosage.setPatientId(patientId);
        testDosage.setMedicationId(medicationId);
        testDosage.setAdministrationTime(LocalDateTime.now().minusHours(1));
        testDosage.setDosageAmount(new BigDecimal("10.5"));
        testDosage.setDosageUnit("mg");
        testDosage.setSchedule(DosageSchedule.AM);
        testDosage.setAdministered(false);
        testDosage.setNotes("Test dosage");
    }

    @Test
    void createMedicationDosage_Success() {
        // Given
        when(medicationDosageRepository.save(any(MedicationDosage.class))).thenReturn(testDosage);

        // When
        MedicationDosage result = medicationDosageService.createMedicationDosage(testDosage);

        // Then
        assertThat(result).isEqualTo(testDosage);
        verify(medicationDosageRepository).save(testDosage);
    }

    @Test
    void createMedicationDosage_NullDosage_ThrowsException() {
        // When/Then
        assertThatThrownBy(() -> medicationDosageService.createMedicationDosage(null))
                .isInstanceOf(InvalidMedicalDataException.class)
                .hasMessage("Medication dosage cannot be null");
        
        verify(medicationDosageRepository, never()).save(any());
    }

    @Test
    void getMedicationDosageById_Found_ReturnsDosage() {
        // Given
        UUID dosageId = testDosage.getId();
        when(medicationDosageRepository.findById(dosageId)).thenReturn(Optional.of(testDosage));

        // When
        Optional<MedicationDosage> result = medicationDosageService.getMedicationDosageById(dosageId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testDosage);
        verify(medicationDosageRepository).findById(dosageId);
    }

    @Test
    void getMedicationDosageById_NotFound_ReturnsEmpty() {
        // Given
        UUID dosageId = UUID.randomUUID();
        when(medicationDosageRepository.findById(dosageId)).thenReturn(Optional.empty());

        // When
        Optional<MedicationDosage> result = medicationDosageService.getMedicationDosageById(dosageId);

        // Then
        assertThat(result).isEmpty();
        verify(medicationDosageRepository).findById(dosageId);
    }

    @Test
    void getMedicationDosagesByPatientId_ReturnsDosages() {
        // Given
        List<MedicationDosage> dosages = Arrays.asList(testDosage);
        when(medicationDosageRepository.findByPatientIdOrderByAdministrationTimeDesc(patientId))
                .thenReturn(dosages);

        // When
        List<MedicationDosage> result = medicationDosageService.getMedicationDosagesByPatientId(patientId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testDosage);
        verify(medicationDosageRepository).findByPatientIdOrderByAdministrationTimeDesc(patientId);
    }

    @Test
    void updateMedicationDosage_Success() {
        // Given
        testDosage.setNotes("Updated notes");
        when(medicationDosageRepository.existsById(testDosage.getId())).thenReturn(true);
        when(medicationDosageRepository.save(testDosage)).thenReturn(testDosage);

        // When
        MedicationDosage result = medicationDosageService.updateMedicationDosage(testDosage);

        // Then
        assertThat(result.getNotes()).isEqualTo("Updated notes");
        verify(medicationDosageRepository).existsById(testDosage.getId());
        verify(medicationDosageRepository).save(testDosage);
    }

    @Test
    void updateMedicationDosage_NotFound_ThrowsException() {
        // Given
        when(medicationDosageRepository.existsById(testDosage.getId())).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> medicationDosageService.updateMedicationDosage(testDosage))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Medication dosage not found with id: " + testDosage.getId());
        
        verify(medicationDosageRepository).existsById(testDosage.getId());
        verify(medicationDosageRepository, never()).save(any());
    }

    @Test
    void deleteMedicationDosage_Success() {
        // Given
        UUID dosageId = testDosage.getId();
        when(medicationDosageRepository.existsById(dosageId)).thenReturn(true);

        // When
        medicationDosageService.deleteMedicationDosage(dosageId);

        // Then
        verify(medicationDosageRepository).existsById(dosageId);
        verify(medicationDosageRepository).deleteById(dosageId);
    }

    @Test
    void deleteMedicationDosage_NotFound_ThrowsException() {
        // Given
        UUID dosageId = UUID.randomUUID();
        when(medicationDosageRepository.existsById(dosageId)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> medicationDosageService.deleteMedicationDosage(dosageId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Medication dosage not found with id: " + dosageId);
        
        verify(medicationDosageRepository).existsById(dosageId);
        verify(medicationDosageRepository, never()).deleteById(any());
    }

    @Test
    void markDosageAsAdministered_Success() {
        // Given
        UUID dosageId = testDosage.getId();
        when(medicationDosageRepository.findById(dosageId)).thenReturn(Optional.of(testDosage));
        when(medicationDosageRepository.save(testDosage)).thenReturn(testDosage);

        // When
        MedicationDosage result = medicationDosageService.markDosageAsAdministered(dosageId);

        // Then
        assertThat(result.isAdministered()).isTrue();
        verify(medicationDosageRepository).findById(dosageId);
        verify(medicationDosageRepository).save(testDosage);
    }

    @Test
    void markDosageAsAdministered_NotFound_ThrowsException() {
        // Given
        UUID dosageId = UUID.randomUUID();
        when(medicationDosageRepository.findById(dosageId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> medicationDosageService.markDosageAsAdministered(dosageId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Medication dosage not found with id: " + dosageId);
        
        verify(medicationDosageRepository).findById(dosageId);
        verify(medicationDosageRepository, never()).save(any());
    }

    @Test
    void getMissedDosages_ReturnsMissedDosages() {
        // Given
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(2);
        List<MedicationDosage> missedDosages = Arrays.asList(testDosage);
        when(medicationDosageRepository.findMissedDosagesByPatientId(patientId, cutoffTime))
                .thenReturn(missedDosages);

        // When
        List<MedicationDosage> result = medicationDosageService.getMissedDosages(patientId, cutoffTime);

        // Then
        assertThat(result).hasSize(1);
        verify(medicationDosageRepository).findMissedDosagesByPatientId(patientId, cutoffTime);
    }

    @Test
    void getUpcomingDosages_ReturnsUpcomingDosages() {
        // Given
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime futureTime = currentTime.plusHours(24);
        List<MedicationDosage> upcomingDosages = Arrays.asList(testDosage);
        when(medicationDosageRepository.findUpcomingDosagesByPatientId(patientId, currentTime, futureTime))
                .thenReturn(upcomingDosages);

        // When
        List<MedicationDosage> result = medicationDosageService.getUpcomingDosages(
                patientId, currentTime, futureTime);

        // Then
        assertThat(result).hasSize(1);
        verify(medicationDosageRepository).findUpcomingDosagesByPatientId(patientId, currentTime, futureTime);
    }
}