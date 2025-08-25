package com.ciaranmckenna.medical_event_tracker.controller;

import com.ciaranmckenna.medical_event_tracker.dto.CreateMedicationRequest;
import com.ciaranmckenna.medical_event_tracker.dto.MedicationResponse;
import com.ciaranmckenna.medical_event_tracker.dto.PaginatedResponse;
import com.ciaranmckenna.medical_event_tracker.dto.UpdateMedicationRequest;
import com.ciaranmckenna.medical_event_tracker.entity.Medication;
import com.ciaranmckenna.medical_event_tracker.service.MedicationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/medications")
@PreAuthorize("hasAnyRole('PRIMARY_USER', 'SECONDARY_USER', 'ADMIN')")
public class MedicationController {

    private static final Logger logger = LoggerFactory.getLogger(MedicationController.class);

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PRIMARY_USER', 'ADMIN')")
    public ResponseEntity<MedicationResponse> createMedication(@Valid @RequestBody CreateMedicationRequest request) {
        logger.info("Creating new medication: {}", request.name());
        
        try {
            MedicationResponse response = medicationService.createMedication(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create medication: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<MedicationResponse>> getAllMedications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        logger.debug("Retrieving all active medications");
        
        List<MedicationResponse> medications = medicationService.getAllActiveMedications();
        PaginatedResponse<MedicationResponse> response = PaginatedResponse.of(medications, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicationResponse> getMedicationById(@PathVariable UUID id) {
        logger.debug("Retrieving medication by ID: {}", id);
        
        MedicationResponse medication = medicationService.getMedicationById(id);
        return ResponseEntity.ok(medication);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRIMARY_USER', 'ADMIN')")
    public ResponseEntity<MedicationResponse> updateMedication(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMedicationRequest request) {
        logger.info("Updating medication with ID: {}", id);
        
        MedicationResponse response = medicationService.updateMedication(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRIMARY_USER', 'ADMIN')")
    public ResponseEntity<Void> deleteMedication(@PathVariable UUID id) {
        logger.info("Deleting medication with ID: {}", id);
        
        medicationService.deleteMedication(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<MedicationResponse>> searchMedications(
            @RequestParam(required = false) String query) {
        logger.debug("Searching medications with query: {}", query);
        
        List<MedicationResponse> medications = medicationService.searchMedications(query);
        return ResponseEntity.ok(medications);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<MedicationResponse>> getMedicationsByType(
            @PathVariable Medication.MedicationType type) {
        logger.debug("Retrieving medications by type: {}", type);
        
        List<MedicationResponse> medications = medicationService.getMedicationsByType(type);
        return ResponseEntity.ok(medications);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<MedicationResponse>> getMostCommonlyUsedMedications() {
        logger.debug("Retrieving most commonly used medications");
        
        List<MedicationResponse> medications = medicationService.getMostCommonlyUsedMedications();
        return ResponseEntity.ok(medications);
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkMedicationExists(@RequestParam String name) {
        logger.debug("Checking if medication exists: {}", name);
        
        boolean exists = medicationService.medicationExistsByName(name);
        return ResponseEntity.ok(exists);
    }
}