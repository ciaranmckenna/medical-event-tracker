package com.ciaranmckenna.medical_event_tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a medical event recorded for a patient.
 * Medical events include symptoms, medication administration, appointments,
 * tests, and other significant medical occurrences.
 */
@Entity
@Table(name = "medical_events")
public class MedicalEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @NotNull(message = "Patient ID is required")
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "medication_id")
    private UUID medicationId;

    @NotNull(message = "Event time is required")
    @PastOrPresent(message = "Event time cannot be in the future")
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Column(name = "description", length = 2000)
    private String description;

    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private MedicalEventSeverity severity;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private MedicalEventCategory category;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.1", message = "Weight must be greater than 0.1 kg")
    @DecimalMax(value = "1000.0", message = "Weight must be less than 1000 kg")
    @Digits(integer = 4, fraction = 2, message = "Weight must have at most 4 digits before and 2 digits after decimal point")
    @Column(name = "weight_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal weightKg;

    @DecimalMin(value = "0.1", message = "Height must be greater than 0.1 cm")
    @DecimalMax(value = "300.0", message = "Height must be less than 300 cm")
    @Digits(integer = 3, fraction = 2, message = "Height must have at most 3 digits before and 2 digits after decimal point")
    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @NotNull(message = "Dosage is required")
    @DecimalMin(value = "0.0", message = "Dosage cannot be negative")
    @DecimalMax(value = "10000.0", message = "Dosage must be less than 10000")
    @Digits(integer = 5, fraction = 2, message = "Dosage must have at most 5 digits before and 2 digits after decimal point")
    @Column(name = "dosage_given", nullable = false, precision = 7, scale = 2)
    private BigDecimal dosageGiven;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Default constructor required by JPA.
     */
    public MedicalEvent() {
    }

    /**
     * Constructor for creating a new medical event.
     *
     * @param patientId    the UUID of the patient
     * @param eventTime    the time when the event occurred
     * @param title        the title/summary of the event
     * @param description  detailed description of the event
     * @param severity     the severity level of the event
     * @param category     the category of the event
     * @param weightKg     the patient's weight in kilograms at the time of event
     * @param heightCm     the patient's height in centimeters at the time of event (optional for patients > 20 years)
     * @param dosageGiven  the medication dosage given (0 if no medication given)
     */
    public MedicalEvent(UUID patientId, LocalDateTime eventTime, String title,
                       String description, MedicalEventSeverity severity,
                       MedicalEventCategory category, BigDecimal weightKg,
                       BigDecimal heightCm, BigDecimal dosageGiven) {
        this.patientId = patientId;
        this.eventTime = eventTime;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.category = category;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.dosageGiven = dosageGiven;
    }

    /**
     * Constructor for creating a medical event with medication association.
     *
     * @param patientId    the UUID of the patient
     * @param medicationId the UUID of the associated medication
     * @param eventTime    the time when the event occurred
     * @param title        the title/summary of the event
     * @param description  detailed description of the event
     * @param severity     the severity level of the event
     * @param category     the category of the event
     * @param weightKg     the patient's weight in kilograms at the time of event
     * @param heightCm     the patient's height in centimeters at the time of event (optional for patients > 20 years)
     * @param dosageGiven  the medication dosage given (0 if no medication given)
     */
    public MedicalEvent(UUID patientId, UUID medicationId, LocalDateTime eventTime,
                       String title, String description, MedicalEventSeverity severity,
                       MedicalEventCategory category, BigDecimal weightKg,
                       BigDecimal heightCm, BigDecimal dosageGiven) {
        this.patientId = patientId;
        this.medicationId = medicationId;
        this.eventTime = eventTime;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.category = category;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.dosageGiven = dosageGiven;
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

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public MedicalEventSeverity getSeverity() {
        return severity;
    }

    public MedicalEventCategory getCategory() {
        return category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public BigDecimal getDosageGiven() {
        return dosageGiven;
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

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSeverity(MedicalEventSeverity severity) {
        this.severity = severity;
    }

    public void setCategory(MedicalEventCategory category) {
        this.category = category;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public void setDosageGiven(BigDecimal dosageGiven) {
        this.dosageGiven = dosageGiven;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalEvent that = (MedicalEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MedicalEvent{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", medicationId=" + medicationId +
                ", eventTime=" + eventTime +
                ", title='" + title + '\'' +
                ", severity=" + severity +
                ", category=" + category +
                ", createdAt=" + createdAt +
                '}';
    }
}