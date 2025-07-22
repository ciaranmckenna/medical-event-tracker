package com.ciaranmckenna.medical_event_tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "patients", indexes = {
    @Index(name = "idx_patient_user_id", columnList = "user_id"),
    @Index(name = "idx_patient_active", columnList = "active")
})
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @DecimalMin(value = "0.1", message = "Weight must be greater than 0.1 kg")
    @DecimalMax(value = "1000.0", message = "Weight must be less than 1000 kg")
    @Digits(integer = 4, fraction = 2, message = "Weight must have at most 4 digits before and 2 digits after decimal point")
    @Column(name = "weight_kg", precision = 6, scale = 2)
    private BigDecimal weightKg;

    @DecimalMin(value = "0.1", message = "Height must be greater than 0.1 cm")
    @DecimalMax(value = "300.0", message = "Height must be less than 300 cm")
    @Digits(integer = 3, fraction = 2, message = "Height must have at most 3 digits before and 2 digits after decimal point")
    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_patient_user"))
    private User user;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PatientMedication> medications = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }

    // Default constructor for JPA
    public Patient() {
    }

    // Constructor for creation
    public Patient(String firstName, String lastName, LocalDate dateOfBirth, Gender gender, User user) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.user = user;
        this.active = true;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isActive() {
        return active;
    }

    public User getUser() {
        return user;
    }

    public Set<PatientMedication> getMedications() {
        return medications;
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
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public void setHeightCm(BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setMedications(Set<PatientMedication> medications) {
        this.medications = medications;
    }

    // Business methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getAgeInYears() {
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    public BigDecimal getBmiIfAvailable() {
        if (weightKg != null && heightCm != null && heightCm.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal heightM = heightCm.divide(new BigDecimal("100"));
            return weightKg.divide(heightM.multiply(heightM), 2, java.math.RoundingMode.HALF_UP);
        }
        return null;
    }

    public void softDelete() {
        this.active = false;
    }

    // equals and hashCode using ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(id, patient.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender=" + gender +
                ", active=" + active +
                '}';
    }
}