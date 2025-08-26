package com.ciaranmckenna.medical_event_tracker.controller;

import com.ciaranmckenna.medical_event_tracker.dto.CreateMedicalEventRequest;
import com.ciaranmckenna.medical_event_tracker.dto.MedicalEventResponse;
import com.ciaranmckenna.medical_event_tracker.dto.MedicalEventSearchRequest;
import com.ciaranmckenna.medical_event_tracker.dto.PagedMedicalEventResponse;
import com.ciaranmckenna.medical_event_tracker.dto.UpdateMedicalEventRequest;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEvent;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import com.ciaranmckenna.medical_event_tracker.exception.MedicalEventNotFoundException;
import com.ciaranmckenna.medical_event_tracker.service.MedicalEventService;
import com.ciaranmckenna.medical_event_tracker.service.MapperService;
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
 * REST controller for managing medical events.
 * Provides CRUD operations and advanced querying for medical events.
 */
@RestController
@RequestMapping("/api/medical-events")
@PreAuthorize("hasRole('PRIMARY_USER') or hasRole('SECONDARY_USER') or hasRole('ADMIN')")
public class MedicalEventController {

    private final MedicalEventService medicalEventService;
    private final MapperService mapperService;

    public MedicalEventController(MedicalEventService medicalEventService, MapperService mapperService) {
        this.medicalEventService = medicalEventService;
        this.mapperService = mapperService;
    }

    /**
     * Create a new medical event.
     */
    @PostMapping
    @PreAuthorize("hasRole('PRIMARY_USER') or hasRole('ADMIN')")
    public ResponseEntity<MedicalEventResponse> createMedicalEvent(
            @Valid @RequestBody CreateMedicalEventRequest request) {
        
        MedicalEvent medicalEvent = mapperService.mapToEntity(request);
        MedicalEvent createdEvent = medicalEventService.createMedicalEvent(medicalEvent);
        MedicalEventResponse response = mapperService.mapToResponse(createdEvent);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get a medical event by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MedicalEventResponse> getMedicalEventById(@PathVariable UUID id) {
        Optional<MedicalEvent> eventOpt = medicalEventService.getMedicalEventById(id);
        
        if (eventOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        MedicalEventResponse response = mapperService.mapToResponse(eventOpt.get());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all medical events for a specific patient.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalEventResponse>> getMedicalEventsByPatientId(
            @PathVariable UUID patientId) {
        
        List<MedicalEvent> events = medicalEventService.getMedicalEventsByPatientId(patientId);
        List<MedicalEventResponse> responses = events.stream()
                .map(mapperService::mapToResponse)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Update an existing medical event.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRIMARY_USER') or hasRole('ADMIN')")
    public ResponseEntity<MedicalEventResponse> updateMedicalEvent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMedicalEventRequest request) {
        
        try {
            MedicalEvent medicalEvent = mapperService.mapToEntity(request);
            MedicalEvent updatedEvent = medicalEventService.updateMedicalEvent(medicalEvent);
            MedicalEventResponse response = mapperService.mapToResponse(updatedEvent);
            
            return ResponseEntity.ok(response);
        } catch (MedicalEventNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Delete a medical event.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRIMARY_USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMedicalEvent(@PathVariable UUID id) {
        try {
            medicalEventService.deleteMedicalEvent(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (MedicalEventNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get medical events for a patient within a date range.
     */
    @GetMapping("/patient/{patientId}/date-range")
    public ResponseEntity<List<MedicalEventResponse>> getMedicalEventsByDateRange(
            @PathVariable UUID patientId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        
        List<MedicalEvent> events = medicalEventService.getMedicalEventsByPatientIdAndDateRange(
                patientId, startTime, endTime);
        List<MedicalEventResponse> responses = events.stream()
                .map(mapperService::mapToResponse)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get medical events for a patient by category.
     */
    @GetMapping("/patient/{patientId}/category/{category}")
    public ResponseEntity<List<MedicalEventResponse>> getMedicalEventsByCategory(
            @PathVariable UUID patientId,
            @PathVariable MedicalEventCategory category) {
        
        List<MedicalEvent> events = medicalEventService.getMedicalEventsByPatientIdAndCategory(
                patientId, category);
        List<MedicalEventResponse> responses = events.stream()
                .map(mapperService::mapToResponse)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get medical events for a patient by severity.
     */
    @GetMapping("/patient/{patientId}/severity/{severity}")
    public ResponseEntity<List<MedicalEventResponse>> getMedicalEventsBySeverity(
            @PathVariable UUID patientId,
            @PathVariable MedicalEventSeverity severity) {
        
        List<MedicalEvent> events = medicalEventService.getMedicalEventsByPatientIdAndSeverity(
                patientId, severity);
        List<MedicalEventResponse> responses = events.stream()
                .map(mapperService::mapToResponse)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Search medical events for a patient by text.
     */
    @GetMapping("/patient/{patientId}/search")
    public ResponseEntity<List<MedicalEventResponse>> searchMedicalEvents(
            @PathVariable UUID patientId,
            @RequestParam String searchText) {
        
        List<MedicalEvent> events = medicalEventService.searchMedicalEventsByPatientId(
                patientId, searchText);
        List<MedicalEventResponse> responses = events.stream()
                .map(mapperService::mapToResponse)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get recent medical events for a patient.
     */
    @GetMapping("/patient/{patientId}/recent")
    public ResponseEntity<List<MedicalEventResponse>> getRecentMedicalEvents(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "7") int daysBack) {
        
        List<MedicalEvent> events = medicalEventService.getRecentMedicalEventsByPatientId(
                patientId, daysBack);
        List<MedicalEventResponse> responses = events.stream()
                .map(mapperService::mapToResponse)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Advanced search for medical events with filtering, sorting, and pagination.
     * Supports complex queries for comprehensive medical event analysis.
     */
    @PostMapping("/search")
    public ResponseEntity<PagedMedicalEventResponse> searchMedicalEvents(
            @Valid @RequestBody MedicalEventSearchRequest searchRequest) {
        
        PagedMedicalEventResponse response = medicalEventService.searchMedicalEvents(searchRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Get medical events for a patient with pagination and sorting.
     * Provides simple paginated access to all patient events.
     */
    @GetMapping("/patient/{patientId}/paginated")
    public ResponseEntity<PagedMedicalEventResponse> getMedicalEventsPaginated(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "eventTime") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        PagedMedicalEventResponse response = medicalEventService.getMedicalEventsByPatientIdPaginated(
                patientId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    /**
     * Get medical events statistics for a patient.
     * Provides aggregated data for medical event analysis and reporting.
     */
    @GetMapping("/patient/{patientId}/statistics")
    public ResponseEntity<MedicalEventStatistics> getMedicalEventStatistics(
            @PathVariable UUID patientId) {
        
        // Get basic counts by category
        long totalEvents = medicalEventService.countMedicalEventsByPatientId(patientId);
        long symptomEvents = medicalEventService.countMedicalEventsByPatientIdAndCategory(patientId, MedicalEventCategory.SYMPTOM);
        long medicationEvents = medicalEventService.countMedicalEventsByPatientIdAndCategory(patientId, MedicalEventCategory.MEDICATION);
        long emergencyEvents = medicalEventService.countMedicalEventsByPatientIdAndCategory(patientId, MedicalEventCategory.EMERGENCY);
        
        // Get recent events for trend analysis
        List<MedicalEvent> recentEvents = medicalEventService.getRecentMedicalEventsByPatientId(patientId, 30);
        
        MedicalEventStatistics statistics = new MedicalEventStatistics(
                patientId,
                totalEvents,
                symptomEvents,
                medicationEvents,
                emergencyEvents,
                recentEvents.size(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.ok(statistics);
    }

    /**
     * DTO for medical event statistics response.
     */
    public record MedicalEventStatistics(
            UUID patientId,
            long totalEvents,
            long symptomEvents,
            long medicationEvents,
            long emergencyEvents,
            long recentEvents,
            LocalDateTime generatedAt
    ) {}

}