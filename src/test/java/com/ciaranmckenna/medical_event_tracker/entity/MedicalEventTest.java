package com.ciaranmckenna.medical_event_tracker.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MedicalEventTest {

    @Test
    void medicalEvent_Creation_Success() {
        // Given
        UUID patientId = UUID.randomUUID();
        UUID medicationId = UUID.randomUUID();
        LocalDateTime eventTime = LocalDateTime.now().minusHours(2);
        String title = "Fever symptoms observed";
        String description = "Patient showed elevated temperature and fatigue";
        MedicalEventSeverity severity = MedicalEventSeverity.MODERATE;
        MedicalEventCategory category = MedicalEventCategory.SYMPTOM;

        // When
        MedicalEvent medicalEvent = new MedicalEvent();
        medicalEvent.setPatientId(patientId);
        medicalEvent.setMedicationId(medicationId);
        medicalEvent.setEventTime(eventTime);
        medicalEvent.setTitle(title);
        medicalEvent.setDescription(description);
        medicalEvent.setSeverity(severity);
        medicalEvent.setCategory(category);

        // Then
        assertThat(medicalEvent.getPatientId()).isEqualTo(patientId);
        assertThat(medicalEvent.getMedicationId()).isEqualTo(medicationId);
        assertThat(medicalEvent.getEventTime()).isEqualTo(eventTime);
        assertThat(medicalEvent.getTitle()).isEqualTo(title);
        assertThat(medicalEvent.getDescription()).isEqualTo(description);
        assertThat(medicalEvent.getSeverity()).isEqualTo(severity);
        assertThat(medicalEvent.getCategory()).isEqualTo(category);
        assertThat(medicalEvent.getId()).isNull(); // Not persisted yet
        assertThat(medicalEvent.getCreatedAt()).isNull(); // Set by JPA
        assertThat(medicalEvent.getUpdatedAt()).isNull(); // Set by JPA
    }

    @Test
    void medicalEvent_Equality_BasedOnId() {
        // Given
        UUID id = UUID.randomUUID();
        MedicalEvent event1 = new MedicalEvent();
        MedicalEvent event2 = new MedicalEvent();
        
        // When both have same ID
        event1.setId(id);
        event2.setId(id);

        // Then
        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    }

    @Test
    void medicalEvent_Inequality_DifferentIds() {
        // Given
        MedicalEvent event1 = new MedicalEvent();
        MedicalEvent event2 = new MedicalEvent();
        event1.setId(UUID.randomUUID());
        event2.setId(UUID.randomUUID());

        // Then
        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    void medicalEvent_ToString_ContainsKeyFields() {
        // Given
        MedicalEvent event = new MedicalEvent();
        event.setTitle("Test Event");
        event.setSeverity(MedicalEventSeverity.MILD);
        event.setCategory(MedicalEventCategory.SYMPTOM);

        // When
        String toString = event.toString();

        // Then
        assertThat(toString).contains("Test Event");
        assertThat(toString).contains("MILD");
        assertThat(toString).contains("SYMPTOM");
    }
}