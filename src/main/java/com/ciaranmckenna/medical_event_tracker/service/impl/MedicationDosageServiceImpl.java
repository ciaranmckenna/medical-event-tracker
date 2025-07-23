package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.entity.DosageSchedule;
import com.ciaranmckenna.medical_event_tracker.entity.MedicationDosage;
import com.ciaranmckenna.medical_event_tracker.exception.InvalidMedicalDataException;
import com.ciaranmckenna.medical_event_tracker.exception.MedicationDosageNotFoundException;
import com.ciaranmckenna.medical_event_tracker.repository.MedicationDosageRepository;
import com.ciaranmckenna.medical_event_tracker.service.MedicationDosageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of MedicationDosageService interface.
 * Provides business logic for managing medication dosages with proper transaction management.
 */
@Service
@Transactional
public class MedicationDosageServiceImpl implements MedicationDosageService {

    private final MedicationDosageRepository medicationDosageRepository;

    public MedicationDosageServiceImpl(MedicationDosageRepository medicationDosageRepository) {
        this.medicationDosageRepository = medicationDosageRepository;
    }

    @Override
    public MedicationDosage createMedicationDosage(MedicationDosage medicationDosage) {
        if (medicationDosage == null) {
            throw new InvalidMedicalDataException("Medication dosage cannot be null");
        }
        
        return medicationDosageRepository.save(medicationDosage);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MedicationDosage> getMedicationDosageById(UUID id) {
        return medicationDosageRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationDosage> getMedicationDosagesByPatientId(UUID patientId) {
        return medicationDosageRepository.findByPatientIdOrderByAdministrationTimeDesc(patientId);
    }

    @Override
    public MedicationDosage updateMedicationDosage(MedicationDosage medicationDosage) {
        if (!medicationDosageRepository.existsById(medicationDosage.getId())) {
            throw new MedicationDosageNotFoundException(medicationDosage.getId());
        }
        
        return medicationDosageRepository.save(medicationDosage);
    }

    @Override
    public void deleteMedicationDosage(UUID id) {
        if (!medicationDosageRepository.existsById(id)) {
            throw new MedicationDosageNotFoundException(id);
        }
        
        medicationDosageRepository.deleteById(id);
    }

    @Override
    public MedicationDosage markDosageAsAdministered(UUID id) {
        Optional<MedicationDosage> dosageOpt = medicationDosageRepository.findById(id);
        if (dosageOpt.isEmpty()) {
            throw new MedicationDosageNotFoundException(id);
        }
        
        MedicationDosage dosage = dosageOpt.get();
        dosage.setAdministered(true);
        return medicationDosageRepository.save(dosage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationDosage> getMedicationDosagesByPatientIdAndMedicationId(UUID patientId, 
                                                                               UUID medicationId) {
        return medicationDosageRepository.findByPatientIdAndMedicationId(patientId, medicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationDosage> getMedicationDosagesByPatientIdAndSchedule(UUID patientId, 
                                                                            DosageSchedule schedule) {
        return medicationDosageRepository.findByPatientIdAndSchedule(patientId, schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationDosage> getMedicationDosagesByPatientIdAndAdministered(UUID patientId, 
                                                                               boolean administered) {
        return medicationDosageRepository.findByPatientIdAndAdministered(patientId, administered);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationDosage> getMedicationDosagesByPatientIdAndDateRange(UUID patientId, 
                                                                             LocalDateTime startTime, 
                                                                             LocalDateTime endTime) {
        return medicationDosageRepository.findByPatientIdAndAdministrationTimeBetween(
                patientId, startTime, endTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationDosage> getMissedDosages(UUID patientId, LocalDateTime cutoffTime) {
        return medicationDosageRepository.findMissedDosagesByPatientId(patientId, cutoffTime);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationDosage> getUpcomingDosages(UUID patientId, 
                                                    LocalDateTime currentTime, 
                                                    LocalDateTime futureTime) {
        return medicationDosageRepository.findUpcomingDosagesByPatientId(patientId, currentTime, futureTime);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageDosageAmount(UUID patientId, UUID medicationId) {
        return medicationDosageRepository.getAverageDosageAmount(patientId, medicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationDosage> getRecentDosages(UUID patientId, int daysBack) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysBack);
        return medicationDosageRepository.findRecentDosagesByPatientId(patientId, cutoffDate);
    }

    @Override
    @Transactional(readOnly = true)
    public long countDosagesByPatientIdAndSchedule(UUID patientId, DosageSchedule schedule) {
        return medicationDosageRepository.countByPatientIdAndSchedule(patientId, schedule);
    }
}