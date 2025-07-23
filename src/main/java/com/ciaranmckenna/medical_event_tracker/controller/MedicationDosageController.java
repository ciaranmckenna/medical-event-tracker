package com.ciaranmckenna.medical_event_tracker.controller;

import com.ciaranmckenna.medical_event_tracker.dto.CreateMedicationDosageRequest;
import com.ciaranmckenna.medical_event_tracker.dto.MedicationDosageResponse;
import com.ciaranmckenna.medical_event_tracker.dto.UpdateMedicationDosageRequest;
import com.ciaranmckenna.medical_event_tracker.entity.DosageSchedule;
import com.ciaranmckenna.medical_event_tracker.entity.MedicationDosage;
import com.ciaranmckenna.medical_event_tracker.exception.MedicationDosageNotFoundException;
import com.ciaranmckenna.medical_event_tracker.service.MedicationDosageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing medication dosages.
 * Provides CRUD operations and tracking for medication dosages.
 */
@RestController
@RequestMapping("/api/medication-dosages")
@PreAuthorize("hasRole('PRIMARY_USER') or hasRole('SECONDARY_USER') or hasRole('ADMIN')")
public class MedicationDosageController {

    private final MedicationDosageService medicationDosageService;

    public MedicationDosageController(MedicationDosageService medicationDosageService) {
        this.medicationDosageService = medicationDosageService;
    }

    /**
     * Create a new medication dosage record.
     */
    @PostMapping
    @PreAuthorize("hasRole('PRIMARY_USER') or hasRole('ADMIN')")
    public ResponseEntity<MedicationDosageResponse> createMedicationDosage(
            @Valid @RequestBody CreateMedicationDosageRequest request) {
        
        MedicationDosage dosage = mapToEntity(request);
        MedicationDosage createdDosage = medicationDosageService.createMedicationDosage(dosage);
        MedicationDosageResponse response = mapToResponse(createdDosage);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get a medication dosage by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MedicationDosageResponse> getMedicationDosageById(@PathVariable UUID id) {
        Optional<MedicationDosage> dosageOpt = medicationDosageService.getMedicationDosageById(id);
        
        if (dosageOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        MedicationDosageResponse response = mapToResponse(dosageOpt.get());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all medication dosages for a specific patient.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicationDosageResponse>> getMedicationDosagesByPatientId(
            @PathVariable UUID patientId) {
        
        List<MedicationDosage> dosages = medicationDosageService.getMedicationDosagesByPatientId(patientId);
        List<MedicationDosageResponse> responses = dosages.stream()
                .map(this::mapToResponse)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Update an existing medication dosage.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRIMARY_USER') or hasRole('ADMIN')")
    public ResponseEntity<MedicationDosageResponse> updateMedicationDosage(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMedicationDosageRequest request) {
        
        try {
            MedicationDosage dosage = mapToEntity(request);
            MedicationDosage updatedDosage = medicationDosageService.updateMedicationDosage(dosage);
            MedicationDosageResponse response = mapToResponse(updatedDosage);
            
            return ResponseEntity.ok(response);
        } catch (MedicationDosageNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Delete a medication dosage.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRIMARY_USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMedicationDosage(@PathVariable UUID id) {
        try {
            medicationDosageService.deleteMedicationDosage(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MedicationDosageNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Mark a dosage as administered.
     */
    @PatchMapping("/{id}/administer")
    @PreAuthorize("hasRole('PRIMARY_USER') or hasRole('ADMIN')")
    public ResponseEntity<MedicationDosageResponse> markDosageAsAdministered(@PathVariable UUID id) {
        try {
            MedicationDosage updatedDosage = medicationDosageService.markDosageAsAdministered(id);
            MedicationDosageResponse response = mapToResponse(updatedDosage);
            
            return ResponseEntity.ok(response);
        } catch (MedicationDosageNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get medication dosages for a patient and medication.
     */
    @GetMapping("/patient/{patientId}/medication/{medicationId}")
    public ResponseEntity<List<MedicationDosageResponse>> getMedicationDosagesByPatientAndMedication(
            @PathVariable UUID patientId,
            @PathVariable UUID medicationId) {
        
        List<MedicationDosage> dosages = medicationDosageService
                .getMedicationDosagesByPatientIdAndMedicationId(patientId, medicationId);
        List<MedicationDosageResponse> responses = dosages.stream()
                .map(this::mapToResponse)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get medication dosages for a patient by schedule.
     */
    @GetMapping("/patient/{patientId}/schedule/{schedule}")
    public ResponseEntity<List<MedicationDosageResponse>> getMedicationDosagesBySchedule(
            @PathVariable UUID patientId,
            @PathVariable DosageSchedule schedule) {
        
        List<MedicationDosage> dosages = medicationDosageService
                .getMedicationDosagesByPatientIdAndSchedule(patientId, schedule);
        List<MedicationDosageResponse> responses = dosages.stream()
                .map(this::mapToResponse)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get missed dosages for a patient.
     */
    @GetMapping("/patient/{patientId}/missed")
    public ResponseEntity<List<MedicationDosageResponse>> getMissedDosages(
            @PathVariable UUID patientId,
            @RequestParam(required = false) LocalDateTime cutoffTime) {
        
        LocalDateTime effectiveCutoffTime = cutoffTime != null ? cutoffTime : LocalDateTime.now();
        List<MedicationDosage> dosages = medicationDosageService.getMissedDosages(
                patientId, effectiveCutoffTime);
        List<MedicationDosageResponse> responses = dosages.stream()
                .map(this::mapToResponse)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get upcoming dosages for a patient.
     */
    @GetMapping("/patient/{patientId}/upcoming")
    public ResponseEntity<List<MedicationDosageResponse>> getUpcomingDosages(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "24") int hoursAhead) {
        
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime futureTime = currentTime.plusHours(hoursAhead);
        
        List<MedicationDosage> dosages = medicationDosageService.getUpcomingDosages(
                patientId, currentTime, futureTime);
        List<MedicationDosageResponse> responses = dosages.stream()
                .map(this::mapToResponse)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get recent dosages for a patient.
     */
    @GetMapping("/patient/{patientId}/recent")
    public ResponseEntity<List<MedicationDosageResponse>> getRecentDosages(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "7") int daysBack) {
        
        List<MedicationDosage> dosages = medicationDosageService.getRecentDosages(
                patientId, daysBack);
        List<MedicationDosageResponse> responses = dosages.stream()
                .map(this::mapToResponse)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    private MedicationDosage mapToEntity(CreateMedicationDosageRequest request) {
        MedicationDosage dosage = new MedicationDosage();
        dosage.setPatientId(request.patientId());
        dosage.setMedicationId(request.medicationId());
        dosage.setAdministrationTime(request.administrationTime());
        dosage.setDosageAmount(request.dosageAmount());
        dosage.setDosageUnit(request.dosageUnit());
        dosage.setSchedule(request.schedule());
        dosage.setAdministered(request.administered());
        dosage.setNotes(request.notes());
        return dosage;
    }

    private MedicationDosage mapToEntity(UpdateMedicationDosageRequest request) {
        MedicationDosage dosage = new MedicationDosage();
        dosage.setId(request.id());
        dosage.setPatientId(request.patientId());
        dosage.setMedicationId(request.medicationId());
        dosage.setAdministrationTime(request.administrationTime());
        dosage.setDosageAmount(request.dosageAmount());
        dosage.setDosageUnit(request.dosageUnit());
        dosage.setSchedule(request.schedule());
        dosage.setAdministered(request.administered());
        dosage.setNotes(request.notes());
        return dosage;
    }

    private MedicationDosageResponse mapToResponse(MedicationDosage dosage) {
        return new MedicationDosageResponse(
                dosage.getId(),
                dosage.getPatientId(),
                dosage.getMedicationId(),
                dosage.getAdministrationTime(),
                dosage.getDosageAmount(),
                dosage.getDosageUnit(),
                dosage.getSchedule(),
                dosage.isAdministered(),
                dosage.getNotes(),
                dosage.getCreatedAt(),
                dosage.getUpdatedAt()
        );
    }
}