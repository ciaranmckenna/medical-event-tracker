package com.ciaranmckenna.medical_event_tracker.dto;

import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MedicalEventSearchRequestTest {

    private final UUID patientId = UUID.randomUUID();

    @Test
    void constructor_SetsDefaultValues() {
        // Given & When
        MedicalEventSearchRequest request = new MedicalEventSearchRequest(
                patientId, null, null, null, null, null, null, null, null, null, null);

        // Then
        assertThat(request.page()).isEqualTo(0);
        assertThat(request.size()).isEqualTo(20);
        assertThat(request.sortBy()).isEqualTo("eventTime");
        assertThat(request.sortDirection()).isEqualTo("DESC");
    }

    @Test
    void constructor_PreservesProvidedValues() {
        // Given & When
        MedicalEventSearchRequest request = new MedicalEventSearchRequest(
                patientId, "headache", List.of(MedicalEventCategory.SYMPTOM), 
                List.of(MedicalEventSeverity.MODERATE), null, null, null, 1, 50, "title", "ASC");

        // Then
        assertThat(request.patientId()).isEqualTo(patientId);
        assertThat(request.searchText()).isEqualTo("headache");
        assertThat(request.categories()).containsExactly(MedicalEventCategory.SYMPTOM);
        assertThat(request.severities()).containsExactly(MedicalEventSeverity.MODERATE);
        assertThat(request.page()).isEqualTo(1);
        assertThat(request.size()).isEqualTo(50);
        assertThat(request.sortBy()).isEqualTo("title");
        assertThat(request.sortDirection()).isEqualTo("ASC");
    }

    @Test
    void hasFilters_ReturnsFalse_WhenNoFiltersSet() {
        // Given
        MedicalEventSearchRequest request = new MedicalEventSearchRequest(
                patientId, null, null, null, null, null, null, null, null, null, null);

        // When & Then
        assertThat(request.hasFilters()).isFalse();
    }

    @Test
    void hasFilters_ReturnsTrue_WhenSearchTextProvided() {
        // Given
        MedicalEventSearchRequest request = new MedicalEventSearchRequest(
                patientId, "headache", null, null, null, null, null, null, null, null, null);

        // When & Then
        assertThat(request.hasFilters()).isTrue();
    }

    @Test
    void hasFilters_ReturnsTrue_WhenCategoriesProvided() {
        // Given
        MedicalEventSearchRequest request = new MedicalEventSearchRequest(
                patientId, null, List.of(MedicalEventCategory.SYMPTOM), null, null, null, null, null, null, null, null);

        // When & Then
        assertThat(request.hasFilters()).isTrue();
    }

    @Test
    void hasFilters_ReturnsTrue_WhenDateRangeProvided() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        MedicalEventSearchRequest request = new MedicalEventSearchRequest(
                patientId, null, null, null, null, startDate, null, null, null, null, null);

        // When & Then
        assertThat(request.hasFilters()).isTrue();
    }

    @Test
    void isValidDateRange_ReturnsTrue_WhenNoDatesProvided() {
        // Given
        MedicalEventSearchRequest request = new MedicalEventSearchRequest(
                patientId, null, null, null, null, null, null, null, null, null, null);

        // When & Then
        assertThat(request.isValidDateRange()).isTrue();
    }

    @Test
    void isValidDateRange_ReturnsTrue_WhenValidDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        MedicalEventSearchRequest request = new MedicalEventSearchRequest(
                patientId, null, null, null, null, startDate, endDate, null, null, null, null);

        // When & Then
        assertThat(request.isValidDateRange()).isTrue();
    }

    @Test
    void isValidDateRange_ReturnsFalse_WhenInvalidDateRange() {
        // Given
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().minusDays(7);
        MedicalEventSearchRequest request = new MedicalEventSearchRequest(
                patientId, null, null, null, null, startDate, endDate, null, null, null, null);

        // When & Then
        assertThat(request.isValidDateRange()).isFalse();
    }

    @Test
    void isValidDateRange_ReturnsTrue_WhenSameDates() {
        // Given
        LocalDateTime sameDate = LocalDateTime.now();
        MedicalEventSearchRequest request = new MedicalEventSearchRequest(
                patientId, null, null, null, null, sameDate, sameDate, null, null, null, null);

        // When & Then
        assertThat(request.isValidDateRange()).isTrue();
    }
}