package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.dto.MedicalEventResponse;
import com.ciaranmckenna.medical_event_tracker.dto.MedicalEventSearchRequest;
import com.ciaranmckenna.medical_event_tracker.dto.PagedMedicalEventResponse;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEvent;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import com.ciaranmckenna.medical_event_tracker.exception.InvalidMedicalDataException;
import com.ciaranmckenna.medical_event_tracker.exception.MedicalEventNotFoundException;
import com.ciaranmckenna.medical_event_tracker.repository.MedicalEventRepository;
import com.ciaranmckenna.medical_event_tracker.repository.MedicalEventSpecification;
import com.ciaranmckenna.medical_event_tracker.service.MedicalEventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of MedicalEventService interface.
 * Provides business logic for managing medical events with proper transaction management.
 */
@Service
@Transactional
public class MedicalEventServiceImpl implements MedicalEventService {

    private final MedicalEventRepository medicalEventRepository;

    public MedicalEventServiceImpl(MedicalEventRepository medicalEventRepository) {
        this.medicalEventRepository = medicalEventRepository;
    }

    @Override
    public MedicalEvent createMedicalEvent(MedicalEvent medicalEvent) {
        if (medicalEvent == null) {
            throw new InvalidMedicalDataException("Medical event cannot be null");
        }
        
        return medicalEventRepository.save(medicalEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MedicalEvent> getMedicalEventById(UUID id) {
        return medicalEventRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalEvent> getMedicalEventsByPatientId(UUID patientId) {
        return medicalEventRepository.findByPatientIdOrderByEventTimeDesc(patientId);
    }

    @Override
    public MedicalEvent updateMedicalEvent(MedicalEvent medicalEvent) {
        if (!medicalEventRepository.existsById(medicalEvent.getId())) {
            throw new MedicalEventNotFoundException(medicalEvent.getId());
        }
        
        return medicalEventRepository.save(medicalEvent);
    }

    @Override
    public void deleteMedicalEvent(UUID id) {
        if (!medicalEventRepository.existsById(id)) {
            throw new MedicalEventNotFoundException(id);
        }
        
        medicalEventRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalEvent> getMedicalEventsByPatientIdAndDateRange(UUID patientId, 
                                                                     LocalDateTime startTime, 
                                                                     LocalDateTime endTime) {
        return medicalEventRepository.findByPatientIdAndEventTimeBetween(patientId, startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalEvent> getMedicalEventsByPatientIdAndCategory(UUID patientId, 
                                                                    MedicalEventCategory category) {
        return medicalEventRepository.findByPatientIdAndCategory(patientId, category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalEvent> getMedicalEventsByPatientIdAndSeverity(UUID patientId, 
                                                                    MedicalEventSeverity severity) {
        return medicalEventRepository.findByPatientIdAndSeverity(patientId, severity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalEvent> getMedicalEventsByPatientIdAndMedicationId(UUID patientId, 
                                                                        UUID medicationId) {
        return medicalEventRepository.findByPatientIdAndMedicationId(patientId, medicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalEvent> searchMedicalEventsByPatientId(UUID patientId, String searchText) {
        return medicalEventRepository.findByPatientIdAndTitleOrDescriptionContaining(patientId, searchText);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalEvent> getRecentMedicalEventsByPatientId(UUID patientId, int daysBack) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysBack);
        return medicalEventRepository.findRecentEventsByPatientId(patientId, cutoffDate);
    }

    @Override
    @Transactional(readOnly = true)
    public long countMedicalEventsByPatientIdAndCategory(UUID patientId, MedicalEventCategory category) {
        return medicalEventRepository.countByPatientIdAndCategory(patientId, category);
    }

    @Override
    @Transactional(readOnly = true)
    public long countMedicalEventsByPatientId(UUID patientId) {
        return medicalEventRepository.countByPatientId(patientId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedMedicalEventResponse searchMedicalEvents(MedicalEventSearchRequest searchRequest) {
        if (searchRequest == null || searchRequest.patientId() == null) {
            throw new InvalidMedicalDataException("Search request and patient ID cannot be null");
        }
        
        if (!searchRequest.isValidDateRange()) {
            throw new InvalidMedicalDataException("Invalid date range: start date must be before or equal to end date");
        }
        
        // Create specification for dynamic query building
        Specification<MedicalEvent> specification = MedicalEventSpecification.createSpecification(searchRequest);
        
        // Create pageable with sorting
        Sort sort = createSort(searchRequest.sortBy(), searchRequest.sortDirection());
        Pageable pageable = PageRequest.of(searchRequest.page(), searchRequest.size(), sort);
        
        // Execute search
        Page<MedicalEvent> eventPage = medicalEventRepository.findAll(specification, pageable);
        
        // Convert to response DTOs
        List<MedicalEventResponse> responseList = eventPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();
        
        return PagedMedicalEventResponse.of(
                responseList,
                eventPage.getNumber(),
                eventPage.getSize(),
                eventPage.getTotalElements(),
                searchRequest.sortBy(),
                searchRequest.sortDirection()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedMedicalEventResponse getMedicalEventsByPatientIdPaginated(UUID patientId, 
                                                                          int page, 
                                                                          int size, 
                                                                          String sortBy, 
                                                                          String sortDirection) {
        if (patientId == null) {
            throw new InvalidMedicalDataException("Patient ID cannot be null");
        }
        
        // Create specification for patient filter
        Specification<MedicalEvent> specification = MedicalEventSpecification.hasPatientId(patientId);
        
        // Create pageable with sorting
        Sort sort = createSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Execute query
        Page<MedicalEvent> eventPage = medicalEventRepository.findAll(specification, pageable);
        
        // Convert to response DTOs
        List<MedicalEventResponse> responseList = eventPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();
        
        return PagedMedicalEventResponse.of(
                responseList,
                eventPage.getNumber(),
                eventPage.getSize(),
                eventPage.getTotalElements(),
                sortBy,
                sortDirection
        );
    }

    /**
     * Create a Sort object based on field name and direction.
     * Provides safe sorting with validation for medical event fields.
     */
    private Sort createSort(String sortBy, String sortDirection) {
        // Validate sort field to prevent SQL injection
        String validatedSortBy = validateSortField(sortBy);
        
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
            
        return Sort.by(direction, validatedSortBy);
    }

    /**
     * Validate sort field to ensure it exists and is safe for sorting.
     * Prevents SQL injection and ensures field exists in the entity.
     */
    private String validateSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "eventTime";
        }
        
        // Allowed sort fields for medical events
        return switch (sortBy.toLowerCase()) {
            case "eventtime", "event_time" -> "eventTime";
            case "createdat", "created_at" -> "createdAt";
            case "updatedat", "updated_at" -> "updatedAt";
            case "title" -> "title";
            case "category" -> "category";
            case "severity" -> "severity";
            default -> "eventTime"; // Default safe sorting
        };
    }

    /**
     * Map MedicalEvent entity to MedicalEventResponse DTO.
     */
    private MedicalEventResponse mapToResponse(MedicalEvent event) {
        return new MedicalEventResponse(
                event.getId(),
                event.getPatientId(),
                event.getMedicationId(),
                event.getEventTime(),
                event.getTitle(),
                event.getDescription(),
                event.getSeverity(),
                event.getCategory(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}