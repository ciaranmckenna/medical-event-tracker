package com.ciaranmckenna.medical_event_tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a medication dosage administration for a patient.
 * Tracks when medications are given, in what amounts, and following which schedule.
 */
@Entity
@Table(name = "medication_dosages")
public class MedicationDosage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @NotNull(message = "Medication ID is required")
    @Column(name = "medication_id", nullable = false)
    private UUID medicationId;

    @NotNull(message = "Administration time is required")
    @PastOrPresent(message = "Administration time cannot be in the future")
    @Column(name = "administration_time", nullable = false)
    private LocalDateTime administrationTime;

    @NotNull(message = "Dosage amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Dosage amount must be greater than 0")
    @Digits(integer = 8, fraction = 3, message = "Dosage amount must have at most 8 integer digits and 3 decimal places")
    @Column(name = "dosage_amount", nullable = false, precision = 11, scale = 3)
    private BigDecimal dosageAmount;

    @NotBlank(message = "Dosage unit is required")
    @Size(max = 20, message = "Dosage unit cannot exceed 20 characters")
    @Column(name = "dosage_unit", nullable = false, length = 20)
    private String dosageUnit;

    @NotNull(message = "Schedule is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule", nullable = false)
    private DosageSchedule schedule;

    @Column(name = "administered", nullable = false)
    private boolean administered = false;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Default constructor required by JPA.
     */
    public MedicationDosage() {
    }

    /**
     * Constructor for creating a new medication dosage record.
     *
     * @param patientId          the UUID of the patient
     * @param medicationId       the UUID of the medication
     * @param administrationTime the time when the medication was/should be administered
     * @param dosageAmount       the amount of medication
     * @param dosageUnit         the unit of measurement (mg, ml, etc.)
     * @param schedule           the dosage schedule (AM, PM, etc.)
     */
    public MedicationDosage(UUID patientId, UUID medicationId, LocalDateTime administrationTime,
                           BigDecimal dosageAmount, String dosageUnit, DosageSchedule schedule) {
        this.patientId = patientId;
        this.medicationId = medicationId;
        this.administrationTime = administrationTime;
        this.dosageAmount = dosageAmount;
        this.dosageUnit = dosageUnit;
        this.schedule = schedule;
        this.administered = false;
    }

    /**
     * Constructor for creating a medication dosage record with administration status.
     *
     * @param patientId          the UUID of the patient
     * @param medicationId       the UUID of the medication
     * @param administrationTime the time when the medication was/should be administered
     * @param dosageAmount       the amount of medication
     * @param dosageUnit         the unit of measurement (mg, ml, etc.)
     * @param schedule           the dosage schedule (AM, PM, etc.)
     * @param administered       whether the medication has been administered
     * @param notes              additional notes about the dosage
     */
    public MedicationDosage(UUID patientId, UUID medicationId, LocalDateTime administrationTime,
                           BigDecimal dosageAmount, String dosageUnit, DosageSchedule schedule,
                           boolean administered, String notes) {
        this.patientId = patientId;
        this.medicationId = medicationId;
        this.administrationTime = administrationTime;
        this.dosageAmount = dosageAmount;
        this.dosageUnit = dosageUnit;
        this.schedule = schedule;
        this.administered = administered;
        this.notes = notes;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public UUID getMedicationId() {
        return medicationId;
    }

    public LocalDateTime getAdministrationTime() {
        return administrationTime;
    }

    public BigDecimal getDosageAmount() {
        return dosageAmount;
    }

    public String getDosageUnit() {
        return dosageUnit;
    }

    public DosageSchedule getSchedule() {
        return schedule;
    }

    public boolean isAdministered() {
        return administered;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public void setMedicationId(UUID medicationId) {
        this.medicationId = medicationId;
    }

    public void setAdministrationTime(LocalDateTime administrationTime) {
        this.administrationTime = administrationTime;
    }

    public void setDosageAmount(BigDecimal dosageAmount) {
        this.dosageAmount = dosageAmount;
    }

    public void setDosageUnit(String dosageUnit) {
        this.dosageUnit = dosageUnit;
    }

    public void setSchedule(DosageSchedule schedule) {
        this.schedule = schedule;
    }

    public void setAdministered(boolean administered) {
        this.administered = administered;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicationDosage that = (MedicationDosage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MedicationDosage{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", medicationId=" + medicationId +
                ", administrationTime=" + administrationTime +
                ", dosageAmount=" + dosageAmount +
                ", dosageUnit='" + dosageUnit + '\'' +
                ", schedule=" + schedule +
                ", administered=" + administered +
                ", createdAt=" + createdAt +
                '}';
    }
}