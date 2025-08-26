package com.ciaranmckenna.medical_event_tracker.dto;

import java.util.List;

public record PaginatedResponse<T>(
    List<T> content,
    int totalElements,
    int totalPages,
    int page,
    int size,
    boolean first,
    boolean last
) {
    
    public static <T> PaginatedResponse<T> of(List<T> content, int page, int size) {
        int totalElements = content.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        // Calculate pagination slice
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        List<T> pageContent = start < totalElements ? content.subList(start, end) : List.of();
        
        return new PaginatedResponse<>(
            pageContent,
            totalElements,
            totalPages,
            page,
            size,
            page == 0,
            page >= totalPages - 1
        );
    }
    
    public static <T> PaginatedResponse<T> empty(int page, int size) {
        return new PaginatedResponse<>(
            List.of(),
            0,
            0,
            page,
            size,
            true,
            true
        );
    }
}