package com.ciaranmckenna.medical_event_tracker.controller;

import com.ciaranmckenna.medical_event_tracker.dto.PatientCreateRequest;
import com.ciaranmckenna.medical_event_tracker.dto.PatientResponse;
import com.ciaranmckenna.medical_event_tracker.dto.PatientUpdateRequest;
import com.ciaranmckenna.medical_event_tracker.dto.PaginatedResponse;
import com.ciaranmckenna.medical_event_tracker.entity.User;
import com.ciaranmckenna.medical_event_tracker.exception.DuplicatePatientException;
import com.ciaranmckenna.medical_event_tracker.exception.PatientNotFoundException;
import com.ciaranmckenna.medical_event_tracker.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(
            @Valid @RequestBody PatientCreateRequest request,
            @AuthenticationPrincipal User user) {
        PatientResponse response = patientService.createPatient(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<PatientResponse>> getActivePatients(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<PatientResponse> patients = patientService.getActivePatients(user);
        PaginatedResponse<PatientResponse> response = PaginatedResponse.of(patients, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PatientResponse>> getAllPatients(
            @AuthenticationPrincipal User user) {
        List<PatientResponse> patients = patientService.getAllPatients(user);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<PatientResponse> getPatientById(
            @PathVariable UUID patientId,
            @AuthenticationPrincipal User user) {
        PatientResponse patient = patientService.getPatientById(patientId, user);
        return ResponseEntity.ok(patient);
    }

    @PutMapping("/{patientId}")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable UUID patientId,
            @Valid @RequestBody PatientUpdateRequest request,
            @AuthenticationPrincipal User user) {
        PatientResponse response = patientService.updatePatient(patientId, request, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> deletePatient(
            @PathVariable UUID patientId,
            @AuthenticationPrincipal User user) {
        patientService.deletePatient(patientId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<PatientResponse>> searchPatients(
            @RequestParam("q") String searchTerm,
            @AuthenticationPrincipal User user) {
        List<PatientResponse> patients = patientService.searchPatientsByName(searchTerm, user);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/age-range")
    public ResponseEntity<List<PatientResponse>> findPatientsByAge(
            @RequestParam("minAge") int minAge,
            @RequestParam("maxAge") int maxAge,
            @AuthenticationPrincipal User user) {
        List<PatientResponse> patients = patientService.findPatientsByAgeRange(minAge, maxAge, user);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/with-medications")
    public ResponseEntity<List<PatientResponse>> getPatientsWithMedications(
            @AuthenticationPrincipal User user) {
        List<PatientResponse> patients = patientService.getPatientsWithActiveMedications(user);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getActivePatientCount(
            @AuthenticationPrincipal User user) {
        long count = patientService.getActivePatientCount(user);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkPatientExists(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String dateOfBirth,
            @AuthenticationPrincipal User user) {
        try {
            java.time.LocalDate dob = java.time.LocalDate.parse(dateOfBirth);
            boolean exists = patientService.patientExists(firstName, lastName, dob, user);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}