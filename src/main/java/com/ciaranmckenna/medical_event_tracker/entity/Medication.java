package com.ciaranmckenna.medical_event_tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "medications", indexes = {
    @Index(name = "idx_medication_active", columnList = "active"),
    @Index(name = "idx_medication_name", columnList = "name")
})
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotBlank(message = "Medication name is required")
    @Size(min = 1, max = 100, message = "Medication name must be between 1 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 100, message = "Generic name cannot exceed 100 characters")
    @Column(name = "generic_name", length = 100)
    private String genericName;

    @NotNull(message = "Medication type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MedicationType type;

    @DecimalMin(value = "0.001", message = "Strength must be greater than 0")
    @DecimalMax(value = "10000.0", message = "Strength must be less than 10000")
    @Digits(integer = 5, fraction = 3, message = "Strength must have at most 5 digits before and 3 digits after decimal point")
    @Column(name = "strength", precision = 8, scale = 3)
    private BigDecimal strength;

    @Size(max = 20, message = "Unit cannot exceed 20 characters")
    @Column(name = "unit", length = 20)
    private String unit;

    @Size(max = 100, message = "Manufacturer cannot exceed 100 characters")
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "medication", cascade = CascadeType.ALL)
    private Set<PatientMedication> patientMedications = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    public enum MedicationType {
        TABLET, CAPSULE, LIQUID, INJECTION, TOPICAL, INHALER, PATCH, SUPPOSITORY, OTHER
    }

    // Default constructor for JPA
    public Medication() {
    }

    // Constructor for creation
    public Medication(String name, MedicationType type) {
        this.name = name;
        this.type = type;
        this.active = true;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGenericName() {
        return genericName;
    }

    public MedicationType getType() {
        return type;
    }

    public BigDecimal getStrength() {
        return strength;
    }

    public String getUnit() {
        return unit;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public Set<PatientMedication> getPatientMedications() {
        return patientMedications;
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
    public void setName(String name) {
        this.name = name;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public void setType(MedicationType type) {
        this.type = type;
    }

    public void setStrength(BigDecimal strength) {
        this.strength = strength;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setPatientMedications(Set<PatientMedication> patientMedications) {
        this.patientMedications = patientMedications;
    }

    // Business methods
    public String getFullName() {
        StringBuilder fullName = new StringBuilder(name);
        if (strength != null && unit != null) {
            fullName.append(" ").append(strength).append(unit);
        }
        return fullName.toString();
    }

    public void softDelete() {
        this.active = false;
    }

    // equals and hashCode using ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medication medication = (Medication) o;
        return Objects.equals(id, medication.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Medication{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", strength=" + strength +
                ", unit='" + unit + '\'' +
                ", active=" + active +
                '}';
    }
}