package com.ciaranmckenna.medical_event_tracker.repository;

import com.ciaranmckenna.medical_event_tracker.entity.MedicalEvent;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
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
class MedicalEventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MedicalEventRepository medicalEventRepository;

    @Test
    void findByPatientId_ReturnsEventsForPatient() {
        // Given
        UUID patientId = UUID.randomUUID();
        UUID otherPatientId = UUID.randomUUID();
        
        MedicalEvent event1 = createMedicalEvent(patientId, "Fever", MedicalEventSeverity.MODERATE);
        MedicalEvent event2 = createMedicalEvent(patientId, "Headache", MedicalEventSeverity.MILD);
        MedicalEvent event3 = createMedicalEvent(otherPatientId, "Other", MedicalEventSeverity.MILD);
        
        entityManager.persistAndFlush(event1);
        entityManager.persistAndFlush(event2);
        entityManager.persistAndFlush(event3);

        // When
        List<MedicalEvent> events = medicalEventRepository.findByPatientId(patientId);

        // Then
        assertThat(events).hasSize(2);
        assertThat(events).extracting(MedicalEvent::getTitle)
                .containsExactlyInAnyOrder("Fever", "Headache");
    }

    @Test
    void findByPatientIdAndEventTimeBetween_ReturnsEventsInDateRange() {
        // Given
        UUID patientId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now().minusDays(1);
        
        MedicalEvent recentEvent = createMedicalEvent(patientId, "Recent", MedicalEventSeverity.MILD);
        recentEvent.setEventTime(LocalDateTime.now().minusDays(3));
        
        MedicalEvent oldEvent = createMedicalEvent(patientId, "Old", MedicalEventSeverity.MILD);
        oldEvent.setEventTime(LocalDateTime.now().minusDays(10));
        
        entityManager.persistAndFlush(recentEvent);
        entityManager.persistAndFlush(oldEvent);

        // When
        List<MedicalEvent> events = medicalEventRepository
                .findByPatientIdAndEventTimeBetween(patientId, startTime, endTime);

        // Then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTitle()).isEqualTo("Recent");
    }

    @Test
    void findByPatientIdAndCategory_ReturnsEventsWithSpecificCategory() {
        // Given
        UUID patientId = UUID.randomUUID();
        
        MedicalEvent symptomEvent = createMedicalEvent(patientId, "Symptom", MedicalEventSeverity.MILD);
        symptomEvent.setCategory(MedicalEventCategory.SYMPTOM);
        
        MedicalEvent medicationEvent = createMedicalEvent(patientId, "Medication", MedicalEventSeverity.MILD);
        medicationEvent.setCategory(MedicalEventCategory.MEDICATION);
        
        entityManager.persistAndFlush(symptomEvent);
        entityManager.persistAndFlush(medicationEvent);

        // When
        List<MedicalEvent> events = medicalEventRepository
                .findByPatientIdAndCategory(patientId, MedicalEventCategory.SYMPTOM);

        // Then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTitle()).isEqualTo("Symptom");
    }

    @Test
    void findByPatientIdAndSeverity_ReturnsEventsWithSpecificSeverity() {
        // Given
        UUID patientId = UUID.randomUUID();
        
        MedicalEvent severeEvent = createMedicalEvent(patientId, "Severe", MedicalEventSeverity.SEVERE);
        MedicalEvent mildEvent = createMedicalEvent(patientId, "Mild", MedicalEventSeverity.MILD);
        
        entityManager.persistAndFlush(severeEvent);
        entityManager.persistAndFlush(mildEvent);

        // When
        List<MedicalEvent> events = medicalEventRepository
                .findByPatientIdAndSeverity(patientId, MedicalEventSeverity.SEVERE);

        // Then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTitle()).isEqualTo("Severe");
    }

    @Test
    void findByPatientIdAndMedicationId_ReturnsEventsLinkedToMedication() {
        // Given
        UUID patientId = UUID.randomUUID();
        UUID medicationId = UUID.randomUUID();
        UUID otherMedicationId = UUID.randomUUID();
        
        MedicalEvent linkedEvent = createMedicalEvent(patientId, "Linked", MedicalEventSeverity.MILD);
        linkedEvent.setMedicationId(medicationId);
        
        MedicalEvent unlinkedEvent = createMedicalEvent(patientId, "Unlinked", MedicalEventSeverity.MILD);
        unlinkedEvent.setMedicationId(otherMedicationId);
        
        entityManager.persistAndFlush(linkedEvent);
        entityManager.persistAndFlush(unlinkedEvent);

        // When
        List<MedicalEvent> events = medicalEventRepository
                .findByPatientIdAndMedicationId(patientId, medicationId);

        // Then
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTitle()).isEqualTo("Linked");
    }

    @Test
    void findByPatientIdOrderByEventTimeDesc_ReturnsEventsInDescendingOrder() {
        // Given
        UUID patientId = UUID.randomUUID();
        
        MedicalEvent earlierEvent = createMedicalEvent(patientId, "Earlier", MedicalEventSeverity.MILD);
        earlierEvent.setEventTime(LocalDateTime.now().minusDays(2));
        
        MedicalEvent laterEvent = createMedicalEvent(patientId, "Later", MedicalEventSeverity.MILD);
        laterEvent.setEventTime(LocalDateTime.now().minusDays(1));
        
        entityManager.persistAndFlush(earlierEvent);
        entityManager.persistAndFlush(laterEvent);

        // When
        List<MedicalEvent> events = medicalEventRepository
                .findByPatientIdOrderByEventTimeDesc(patientId);

        // Then
        assertThat(events).hasSize(2);
        assertThat(events.get(0).getTitle()).isEqualTo("Later");
        assertThat(events.get(1).getTitle()).isEqualTo("Earlier");
    }

    @Test
    void countByPatientIdAndCategory_ReturnsCorrectCount() {
        // Given
        UUID patientId = UUID.randomUUID();
        
        MedicalEvent symptom1 = createMedicalEvent(patientId, "Symptom1", MedicalEventSeverity.MILD);
        symptom1.setCategory(MedicalEventCategory.SYMPTOM);
        
        MedicalEvent symptom2 = createMedicalEvent(patientId, "Symptom2", MedicalEventSeverity.MILD);
        symptom2.setCategory(MedicalEventCategory.SYMPTOM);
        
        MedicalEvent medication = createMedicalEvent(patientId, "Medication", MedicalEventSeverity.MILD);
        medication.setCategory(MedicalEventCategory.MEDICATION);
        
        entityManager.persistAndFlush(symptom1);
        entityManager.persistAndFlush(symptom2);
        entityManager.persistAndFlush(medication);

        // When
        long count = medicalEventRepository
                .countByPatientIdAndCategory(patientId, MedicalEventCategory.SYMPTOM);

        // Then
        assertThat(count).isEqualTo(2);
    }

    /**
     * Helper method to create a test MedicalEvent with all required fields.
     * Sets realistic default medical values for weight, height, and dosage.
     */
    private MedicalEvent createMedicalEvent(UUID patientId, String title, MedicalEventSeverity severity) {
        MedicalEvent event = new MedicalEvent();
        event.setPatientId(patientId);
        event.setEventTime(LocalDateTime.now().minusHours(1));
        event.setTitle(title);
        event.setDescription("Test description for " + title);
        event.setSeverity(severity);
        event.setCategory(MedicalEventCategory.SYMPTOM);
        // Set required medical data fields with realistic test values
        event.setWeightKg(new BigDecimal("70.50")); // Default adult weight
        event.setHeightCm(new BigDecimal("175.00")); // Default adult height
        event.setDosageGiven(new BigDecimal("5.00")); // Default medication dosage
        return event;
    }
}