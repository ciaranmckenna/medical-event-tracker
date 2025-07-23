package com.ciaranmckenna.medical_event_tracker.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MedicationDosageTest {

    @Test
    void medicationDosage_Creation_Success() {
        // Given
        UUID patientId = UUID.randomUUID();
        UUID medicationId = UUID.randomUUID();
        LocalDateTime administrationTime = LocalDateTime.now().minusHours(1);
        BigDecimal dosageAmount = new BigDecimal("10.5");
        String dosageUnit = "mg";
        DosageSchedule schedule = DosageSchedule.AM;
        String notes = "Taken with breakfast";

        // When
        MedicationDosage dosage = new MedicationDosage();
        dosage.setPatientId(patientId);
        dosage.setMedicationId(medicationId);
        dosage.setAdministrationTime(administrationTime);
        dosage.setDosageAmount(dosageAmount);
        dosage.setDosageUnit(dosageUnit);
        dosage.setSchedule(schedule);
        dosage.setNotes(notes);
        dosage.setAdministered(true);

        // Then
        assertThat(dosage.getPatientId()).isEqualTo(patientId);
        assertThat(dosage.getMedicationId()).isEqualTo(medicationId);
        assertThat(dosage.getAdministrationTime()).isEqualTo(administrationTime);
        assertThat(dosage.getDosageAmount()).isEqualByComparingTo(dosageAmount);
        assertThat(dosage.getDosageUnit()).isEqualTo(dosageUnit);
        assertThat(dosage.getSchedule()).isEqualTo(schedule);
        assertThat(dosage.getNotes()).isEqualTo(notes);
        assertThat(dosage.isAdministered()).isTrue();
        assertThat(dosage.getId()).isNull(); // Not persisted yet
        assertThat(dosage.getCreatedAt()).isNull(); // Set by JPA
        assertThat(dosage.getUpdatedAt()).isNull(); // Set by JPA
    }

    @Test
    void medicationDosage_Constructor_Success() {
        // Given
        UUID patientId = UUID.randomUUID();
        UUID medicationId = UUID.randomUUID();
        LocalDateTime administrationTime = LocalDateTime.now().minusHours(2);
        BigDecimal dosageAmount = new BigDecimal("5.0");
        String dosageUnit = "ml";
        DosageSchedule schedule = DosageSchedule.PM;

        // When
        MedicationDosage dosage = new MedicationDosage(
            patientId, medicationId, administrationTime, 
            dosageAmount, dosageUnit, schedule
        );

        // Then
        assertThat(dosage.getPatientId()).isEqualTo(patientId);
        assertThat(dosage.getMedicationId()).isEqualTo(medicationId);
        assertThat(dosage.getAdministrationTime()).isEqualTo(administrationTime);
        assertThat(dosage.getDosageAmount()).isEqualByComparingTo(dosageAmount);
        assertThat(dosage.getDosageUnit()).isEqualTo(dosageUnit);
        assertThat(dosage.getSchedule()).isEqualTo(schedule);
        assertThat(dosage.isAdministered()).isFalse(); // Default
    }

    @Test
    void medicationDosage_Equality_BasedOnId() {
        // Given
        UUID id = UUID.randomUUID();
        MedicationDosage dosage1 = new MedicationDosage();
        MedicationDosage dosage2 = new MedicationDosage();
        
        // When both have same ID
        dosage1.setId(id);
        dosage2.setId(id);

        // Then
        assertThat(dosage1).isEqualTo(dosage2);
        assertThat(dosage1.hashCode()).isEqualTo(dosage2.hashCode());
    }

    @Test
    void medicationDosage_Inequality_DifferentIds() {
        // Given
        MedicationDosage dosage1 = new MedicationDosage();
        MedicationDosage dosage2 = new MedicationDosage();
        dosage1.setId(UUID.randomUUID());
        dosage2.setId(UUID.randomUUID());

        // Then
        assertThat(dosage1).isNotEqualTo(dosage2);
    }

    @Test
    void medicationDosage_ToString_ContainsKeyFields() {
        // Given
        MedicationDosage dosage = new MedicationDosage();
        dosage.setDosageAmount(new BigDecimal("7.5"));
        dosage.setDosageUnit("mg");
        dosage.setSchedule(DosageSchedule.AM);

        // When
        String toString = dosage.toString();

        // Then
        assertThat(toString).contains("7.5");
        assertThat(toString).contains("mg");
        assertThat(toString).contains("AM");
    }
}