package com.ciaranmckenna.medical_event_tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "patient_medications", indexes = {
    @Index(name = "idx_patient_medication_patient", columnList = "patient_id"),
    @Index(name = "idx_patient_medication_active", columnList = "active"),
    @Index(name = "idx_patient_medication_start_date", columnList = "start_date")
})
public class PatientMedication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_patient_medication_patient"))
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false, foreignKey = @ForeignKey(name = "fk_patient_medication_medication"))
    private Medication medication;

    @NotNull(message = "Dosage is required")
    @DecimalMin(value = "0.001", message = "Dosage must be greater than 0")
    @DecimalMax(value = "10000.0", message = "Dosage must be less than 10000")
    @Digits(integer = 5, fraction = 3, message = "Dosage must have at most 5 digits before and 3 digits after decimal point")
    @Column(name = "dosage", nullable = false, precision = 8, scale = 3)
    private BigDecimal dosage;

    @NotBlank(message = "Dosage unit is required")
    @Size(max = 20, message = "Dosage unit cannot exceed 20 characters")
    @Column(name = "dosage_unit", nullable = false, length = 20)
    private String dosageUnit;

    @NotNull(message = "Frequency is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private Frequency frequency;

    @Column(name = "morning_time")
    private LocalTime morningTime;

    @Column(name = "evening_time")
    private LocalTime eveningTime;

    @Column(name = "additional_times")
    private String additionalTimes; // JSON array of times for complex schedules

    @NotNull(message = "Start date is required")
    @PastOrPresent(message = "Start date cannot be in the future")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Future(message = "End date must be in the future")
    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Size(max = 500, message = "Instructions cannot exceed 500 characters")
    @Column(name = "instructions", length = 500)
    private String instructions;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    public enum Frequency {
        ONCE_DAILY, TWICE_DAILY, THREE_TIMES_DAILY, FOUR_TIMES_DAILY, 
        EVERY_OTHER_DAY, WEEKLY, AS_NEEDED, CUSTOM
    }

    // Default constructor for JPA
    public PatientMedication() {
    }

    // Constructor for creation
    public PatientMedication(Patient patient, Medication medication, BigDecimal dosage, 
                           String dosageUnit, Frequency frequency, LocalDateTime startDate) {
        this.patient = patient;
        this.medication = medication;
        this.dosage = dosage;
        this.dosageUnit = dosageUnit;
        this.frequency = frequency;
        this.startDate = startDate;
        this.active = true;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public Medication getMedication() {
        return medication;
    }

    public BigDecimal getDosage() {
        return dosage;
    }

    public String getDosageUnit() {
        return dosageUnit;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public LocalTime getMorningTime() {
        return morningTime;
    }

    public LocalTime getEveningTime() {
        return eveningTime;
    }

    public String getAdditionalTimes() {
        return additionalTimes;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    // Setters
    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public void setMedication(Medication medication) {
        this.medication = medication;
    }

    public void setDosage(BigDecimal dosage) {
        this.dosage = dosage;
    }

    public void setDosageUnit(String dosageUnit) {
        this.dosageUnit = dosageUnit;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public void setMorningTime(LocalTime morningTime) {
        this.morningTime = morningTime;
    }

    public void setEveningTime(LocalTime eveningTime) {
        this.eveningTime = eveningTime;
    }

    public void setAdditionalTimes(String additionalTimes) {
        this.additionalTimes = additionalTimes;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Business methods
    public boolean isCurrentlyActive() {
        if (!active) return false;
        
        LocalDateTime now = LocalDateTime.now();
        if (startDate.isAfter(now)) return false;
        if (endDate != null && endDate.isBefore(now)) return false;
        
        return true;
    }

    public String getDosageDescription() {
        return dosage + " " + dosageUnit + " " + frequency.name().toLowerCase().replace("_", " ");
    }

    public void discontinue() {
        this.active = false;
        this.endDate = LocalDateTime.now();
    }

    public void softDelete() {
        this.active = false;
    }

    // equals and hashCode using ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatientMedication that = (PatientMedication) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PatientMedication{" +
                "id=" + id +
                ", patient=" + (patient != null ? patient.getFullName() : null) +
                ", medication=" + (medication != null ? medication.getName() : null) +
                ", dosage=" + dosage + " " + dosageUnit +
                ", frequency=" + frequency +
                ", active=" + active +
                '}';
    }
}