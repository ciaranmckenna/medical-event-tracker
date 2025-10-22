package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import com.ciaranmckenna.medical_event_tracker.validation.ValidDateRange;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for advanced medical event searching with filtering and pagination.
 * Supports complex medical event queries with multiple criteria and sorting options.
 */
@ValidDateRange
public record MedicalEventSearchRequest(
        
        UUID patientId,
        
        @Size(max = 200, message = "Search text cannot exceed 200 characters")
        String searchText,
        
        List<MedicalEventCategory> categories,
        
        List<MedicalEventSeverity> severities,
        
        List<UUID> medicationIds,
        
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
    
    public MedicalEventSearchRequest {
        // Set default values for pagination
        page = page != null ? page : 0;
        size = size != null ? size : 20;
        sortBy = sortBy != null ? sortBy : "eventTime";
        sortDirection = sortDirection != null ? sortDirection : "DESC";
    }
    
    /**
     * Check if this search request has any filter criteria.
     * 
     * @return true if any search criteria are specified
     */
    public boolean hasFilters() {
        return searchText != null && !searchText.trim().isEmpty() ||
               categories != null && !categories.isEmpty() ||
               severities != null && !severities.isEmpty() ||
               medicationIds != null && !medicationIds.isEmpty() ||
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