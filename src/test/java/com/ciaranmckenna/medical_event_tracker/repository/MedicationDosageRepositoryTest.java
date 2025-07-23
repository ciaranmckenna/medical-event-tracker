package com.ciaranmckenna.medical_event_tracker.repository;

import com.ciaranmckenna.medical_event_tracker.entity.DosageSchedule;
import com.ciaranmckenna.medical_event_tracker.entity.MedicationDosage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MedicationDosageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MedicationDosageRepository medicationDosageRepository;

    @Test
    void findByPatientId_ReturnsDosagesForPatient() {
        // Given
        UUID patientId = UUID.randomUUID();
        UUID otherPatientId = UUID.randomUUID();
        
        MedicationDosage dosage1 = createMedicationDosage(patientId, new BigDecimal("10.0"), DosageSchedule.AM);
        MedicationDosage dosage2 = createMedicationDosage(patientId, new BigDecimal("15.0"), DosageSchedule.PM);
        MedicationDosage otherDosage = createMedicationDosage(otherPatientId, new BigDecimal("5.0"), DosageSchedule.AM);
        
        entityManager.persistAndFlush(dosage1);
        entityManager.persistAndFlush(dosage2);
        entityManager.persistAndFlush(otherDosage);

        // When
        List<MedicationDosage> dosages = medicationDosageRepository.findByPatientId(patientId);

        // Then
        assertThat(dosages).hasSize(2);
        assertThat(dosages).extracting(MedicationDosage::getDosageAmount)
                .containsExactlyInAnyOrder(new BigDecimal("10.0"), new BigDecimal("15.0"));
    }

    @Test
    void findByPatientIdAndMedicationId_ReturnsDosagesForPatientAndMedication() {
        // Given
        UUID patientId = UUID.randomUUID();
        UUID medicationId = UUID.randomUUID();
        UUID otherMedicationId = UUID.randomUUID();
        
        MedicationDosage targetDosage = createMedicationDosage(patientId, new BigDecimal("10.0"), DosageSchedule.AM);
        targetDosage.setMedicationId(medicationId);
        
        MedicationDosage otherMedDosage = createMedicationDosage(patientId, new BigDecimal("5.0"), DosageSchedule.PM);
        otherMedDosage.setMedicationId(otherMedicationId);
        
        entityManager.persistAndFlush(targetDosage);
        entityManager.persistAndFlush(otherMedDosage);

        // When
        List<MedicationDosage> dosages = medicationDosageRepository
                .findByPatientIdAndMedicationId(patientId, medicationId);

        // Then
        assertThat(dosages).hasSize(1);
        assertThat(dosages.get(0).getDosageAmount()).isEqualByComparingTo(new BigDecimal("10.0"));
    }

    @Test
    void findByPatientIdAndSchedule_ReturnsDosagesForSchedule() {
        // Given
        UUID patientId = UUID.randomUUID();
        
        MedicationDosage amDosage = createMedicationDosage(patientId, new BigDecimal("10.0"), DosageSchedule.AM);
        MedicationDosage pmDosage = createMedicationDosage(patientId, new BigDecimal("15.0"), DosageSchedule.PM);
        
        entityManager.persistAndFlush(amDosage);
        entityManager.persistAndFlush(pmDosage);

        // When
        List<MedicationDosage> dosages = medicationDosageRepository
                .findByPatientIdAndSchedule(patientId, DosageSchedule.AM);

        // Then
        assertThat(dosages).hasSize(1);
        assertThat(dosages.get(0).getSchedule()).isEqualTo(DosageSchedule.AM);
    }

    @Test
    void findByPatientIdAndAdministered_ReturnsDosagesByAdministrationStatus() {
        // Given
        UUID patientId = UUID.randomUUID();
        
        MedicationDosage administeredDosage = createMedicationDosage(patientId, new BigDecimal("10.0"), DosageSchedule.AM);
        administeredDosage.setAdministered(true);
        
        MedicationDosage pendingDosage = createMedicationDosage(patientId, new BigDecimal("15.0"), DosageSchedule.PM);
        pendingDosage.setAdministered(false);
        
        entityManager.persistAndFlush(administeredDosage);
        entityManager.persistAndFlush(pendingDosage);

        // When
        List<MedicationDosage> administeredDosages = medicationDosageRepository
                .findByPatientIdAndAdministered(patientId, true);

        // Then
        assertThat(administeredDosages).hasSize(1);
        assertThat(administeredDosages.get(0).isAdministered()).isTrue();
    }

    @Test
    void findByPatientIdAndAdministrationTimeBetween_ReturnsDosagesInDateRange() {
        // Given
        UUID patientId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now().minusDays(1);
        
        MedicationDosage recentDosage = createMedicationDosage(patientId, new BigDecimal("10.0"), DosageSchedule.AM);
        recentDosage.setAdministrationTime(LocalDateTime.now().minusDays(3));
        
        MedicationDosage oldDosage = createMedicationDosage(patientId, new BigDecimal("15.0"), DosageSchedule.PM);
        oldDosage.setAdministrationTime(LocalDateTime.now().minusDays(10));
        
        entityManager.persistAndFlush(recentDosage);
        entityManager.persistAndFlush(oldDosage);

        // When
        List<MedicationDosage> dosages = medicationDosageRepository
                .findByPatientIdAndAdministrationTimeBetween(patientId, startTime, endTime);

        // Then
        assertThat(dosages).hasSize(1);
        assertThat(dosages.get(0).getDosageAmount()).isEqualByComparingTo(new BigDecimal("10.0"));
    }

    @Test
    void findByPatientIdOrderByAdministrationTimeDesc_ReturnsDosagesInDescendingOrder() {
        // Given
        UUID patientId = UUID.randomUUID();
        
        MedicationDosage earlierDosage = createMedicationDosage(patientId, new BigDecimal("10.0"), DosageSchedule.AM);
        earlierDosage.setAdministrationTime(LocalDateTime.now().minusDays(2));
        
        MedicationDosage laterDosage = createMedicationDosage(patientId, new BigDecimal("15.0"), DosageSchedule.PM);
        laterDosage.setAdministrationTime(LocalDateTime.now().minusDays(1));
        
        entityManager.persistAndFlush(earlierDosage);
        entityManager.persistAndFlush(laterDosage);

        // When
        List<MedicationDosage> dosages = medicationDosageRepository
                .findByPatientIdOrderByAdministrationTimeDesc(patientId);

        // Then
        assertThat(dosages).hasSize(2);
        assertThat(dosages.get(0).getDosageAmount()).isEqualByComparingTo(new BigDecimal("15.0")); // Later dosage first
        assertThat(dosages.get(1).getDosageAmount()).isEqualByComparingTo(new BigDecimal("10.0")); // Earlier dosage second
    }

    @Test
    void countByPatientIdAndSchedule_ReturnsCorrectCount() {
        // Given
        UUID patientId = UUID.randomUUID();
        
        MedicationDosage amDosage1 = createMedicationDosage(patientId, new BigDecimal("10.0"), DosageSchedule.AM);
        MedicationDosage amDosage2 = createMedicationDosage(patientId, new BigDecimal("12.0"), DosageSchedule.AM);
        MedicationDosage pmDosage = createMedicationDosage(patientId, new BigDecimal("15.0"), DosageSchedule.PM);
        
        entityManager.persistAndFlush(amDosage1);
        entityManager.persistAndFlush(amDosage2);
        entityManager.persistAndFlush(pmDosage);

        // When
        long count = medicationDosageRepository.countByPatientIdAndSchedule(patientId, DosageSchedule.AM);

        // Then
        assertThat(count).isEqualTo(2);
    }

    private MedicationDosage createMedicationDosage(UUID patientId, BigDecimal dosageAmount, DosageSchedule schedule) {
        MedicationDosage dosage = new MedicationDosage();
        dosage.setPatientId(patientId);
        dosage.setMedicationId(UUID.randomUUID());
        dosage.setAdministrationTime(LocalDateTime.now().minusHours(1));
        dosage.setDosageAmount(dosageAmount);
        dosage.setDosageUnit("mg");
        dosage.setSchedule(schedule);
        dosage.setAdministered(false);
        return dosage;
    }
}