package com.ciaranmckenna.medical_event_tracker.dto;

import java.util.List;

/**
 * Paginated response DTO for medical event search results.
 * Provides comprehensive pagination metadata for medical event lists.
 */
public record PagedMedicalEventResponse(
        
        List<MedicalEventResponse> content,
        
        int page,
        
        int size,
        
        long totalElements,
        
        int totalPages,
        
        boolean first,
        
        boolean last,
        
        boolean hasNext,
        
        boolean hasPrevious,
        
        String sortBy,
        
        String sortDirection
) {
    
    /**
     * Create a paginated response from medical events and pagination metadata.
     * 
     * @param events the list of medical event responses
     * @param page current page number
     * @param size page size
     * @param totalElements total number of elements across all pages
     * @param sortBy the field used for sorting
     * @param sortDirection the sort direction (ASC/DESC)
     * @return paginated medical event response
     */
    public static PagedMedicalEventResponse of(List<MedicalEventResponse> events, 
                                             int page, 
                                             int size, 
                                             long totalElements,
                                             String sortBy,
                                             String sortDirection) {
        
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean isFirst = page == 0;
        boolean isLast = page >= totalPages - 1;
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;
        
        return new PagedMedicalEventResponse(
                events,
                page,
                size,
                totalElements,
                totalPages,
                isFirst,
                isLast,
                hasNext,
                hasPrevious,
                sortBy,
                sortDirection
        );
    }
}