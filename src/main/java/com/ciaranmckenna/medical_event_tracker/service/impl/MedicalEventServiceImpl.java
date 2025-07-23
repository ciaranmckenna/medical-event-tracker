package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.entity.MedicalEvent;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventCategory;
import com.ciaranmckenna.medical_event_tracker.entity.MedicalEventSeverity;
import com.ciaranmckenna.medical_event_tracker.exception.InvalidMedicalDataException;
import com.ciaranmckenna.medical_event_tracker.exception.MedicalEventNotFoundException;
import com.ciaranmckenna.medical_event_tracker.repository.MedicalEventRepository;
import com.ciaranmckenna.medical_event_tracker.service.MedicalEventService;
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
}