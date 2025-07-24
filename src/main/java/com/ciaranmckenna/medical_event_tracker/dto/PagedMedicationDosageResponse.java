package com.ciaranmckenna.medical_event_tracker.dto;

import java.util.List;

/**
 * Paginated response DTO for medication dosage search results.
 * Provides comprehensive pagination metadata for dosage tracking analysis.
 */
public record PagedMedicationDosageResponse(
        
        List<MedicationDosageResponse> content,
        
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
     * Create a paginated response from medication dosages and pagination metadata.
     * 
     * @param dosages the list of medication dosage responses
     * @param page current page number
     * @param size page size
     * @param totalElements total number of elements across all pages
     * @param sortBy the field used for sorting
     * @param sortDirection the sort direction (ASC/DESC)
     * @return paginated medication dosage response
     */
    public static PagedMedicationDosageResponse of(List<MedicationDosageResponse> dosages, 
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
        
        return new PagedMedicationDosageResponse(
                dosages,
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