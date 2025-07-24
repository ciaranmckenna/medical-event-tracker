package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.DosageSchedule;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for advanced medication dosage searching with filtering and pagination.
 * Supports complex dosage queries for medication adherence analysis.
 */
public record MedicationDosageSearchRequest(
        
        @NotNull(message = "Patient ID is required for dosage search")
        UUID patientId,
        
        List<UUID> medicationIds,
        
        List<DosageSchedule> schedules,
        
        Boolean administered,
        
        LocalDateTime startDate,
        
        LocalDateTime endDate,
        
        @Min(value = 0, message = "Page number must be non-negative")
        Integer page,
        
        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size cannot exceed 100 for performance reasons")
        Integer size,
        
        String sortBy,
        
        String sortDirection
) {
    
    public MedicationDosageSearchRequest {
        // Set default values for pagination
        page = page != null ? page : 0;
        size = size != null ? size : 20;
        sortBy = sortBy != null ? sortBy : "administrationTime";
        sortDirection = sortDirection != null ? sortDirection : "DESC";
    }
    
    /**
     * Check if this search request has any filter criteria.
     * 
     * @return true if any search criteria are specified
     */
    public boolean hasFilters() {
        return medicationIds != null && !medicationIds.isEmpty() ||
               schedules != null && !schedules.isEmpty() ||
               administered != null ||
               startDate != null ||
               endDate != null;
    }
    
    /**
     * Validate that start date is before end date if both are specified.
     * 
     * @return true if date range is valid
     */
    public boolean isValidDateRange() {
        if (startDate != null && endDate != null) {
            return startDate.isBefore(endDate) || startDate.isEqual(endDate);
        }
        return true;
    }
}