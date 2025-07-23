package com.ciaranmckenna.medical_event_tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

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
     */
    public MedicalEvent(UUID patientId, LocalDateTime eventTime, String title, 
                       String description, MedicalEventSeverity severity, 
                       MedicalEventCategory category) {
        this.patientId = patientId;
        this.eventTime = eventTime;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.category = category;
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
     */
    public MedicalEvent(UUID patientId, UUID medicationId, LocalDateTime eventTime, 
                       String title, String description, MedicalEventSeverity severity, 
                       MedicalEventCategory category) {
        this.patientId = patientId;
        this.medicationId = medicationId;
        this.eventTime = eventTime;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.category = category;
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