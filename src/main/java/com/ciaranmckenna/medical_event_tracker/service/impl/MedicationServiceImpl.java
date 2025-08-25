package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.dto.CreateMedicationRequest;
import com.ciaranmckenna.medical_event_tracker.dto.MedicationResponse;
import com.ciaranmckenna.medical_event_tracker.dto.UpdateMedicationRequest;
import com.ciaranmckenna.medical_event_tracker.entity.Medication;
import com.ciaranmckenna.medical_event_tracker.exception.MedicationNotFoundException;
import com.ciaranmckenna.medical_event_tracker.repository.MedicationRepository;
import com.ciaranmckenna.medical_event_tracker.service.MedicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MedicationServiceImpl implements MedicationService {

    private static final Logger logger = LoggerFactory.getLogger(MedicationServiceImpl.class);

    private final MedicationRepository medicationRepository;

    public MedicationServiceImpl(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    @Override
    public MedicationResponse createMedication(CreateMedicationRequest request) {
        logger.info("Creating new medication: {}", request.name());

        // Check if medication with same name already exists
        if (medicationRepository.existsByNameIgnoreCaseAndActiveTrue(request.name())) {
            throw new IllegalArgumentException("Medication with name '" + request.name() + "' already exists");
        }

        Medication medication = new Medication();
        medication.setName(request.name());
        medication.setGenericName(request.genericName());
        medication.setType(request.type());
        medication.setStrength(request.strength());
        medication.setUnit(request.unit());
        medication.setManufacturer(request.manufacturer());
        medication.setDescription(request.description());
        medication.setActive(true);

        Medication savedMedication = medicationRepository.save(medication);
        logger.info("Successfully created medication with ID: {}", savedMedication.getId());

        return MedicationResponse.of(savedMedication);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationResponse> getAllActiveMedications() {
        logger.debug("Retrieving all active medications");
        
        List<Medication> medications = medicationRepository.findByActiveTrue();
        return medications.stream()
                .map(MedicationResponse::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicationResponse getMedicationById(UUID id) {
        logger.debug("Retrieving medication by ID: {}", id);
        
        Medication medication = medicationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new MedicationNotFoundException("Medication not found with ID: " + id));
        
        return MedicationResponse.of(medication);
    }

    @Override
    public MedicationResponse updateMedication(UUID id, UpdateMedicationRequest request) {
        logger.info("Updating medication with ID: {}", id);
        
        Medication medication = medicationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new MedicationNotFoundException("Medication not found with ID: " + id));

        // Update fields if provided
        if (request.name() != null && !request.name().isBlank()) {
            // Check for duplicate name if name is changing
            if (!medication.getName().equalsIgnoreCase(request.name()) &&
                medicationRepository.existsByNameIgnoreCaseAndActiveTrue(request.name())) {
                throw new IllegalArgumentException("Medication with name '" + request.name() + "' already exists");
            }
            medication.setName(request.name());
        }
        
        if (request.genericName() != null) {
            medication.setGenericName(request.genericName());
        }
        
        if (request.type() != null) {
            medication.setType(request.type());
        }
        
        if (request.strength() != null) {
            medication.setStrength(request.strength());
        }
        
        if (request.unit() != null) {
            medication.setUnit(request.unit());
        }
        
        if (request.manufacturer() != null) {
            medication.setManufacturer(request.manufacturer());
        }
        
        if (request.description() != null) {
            medication.setDescription(request.description());
        }

        Medication updatedMedication = medicationRepository.save(medication);
        logger.info("Successfully updated medication with ID: {}", updatedMedication.getId());
        
        return MedicationResponse.of(updatedMedication);
    }

    @Override
    public void deleteMedication(UUID id) {
        logger.info("Soft deleting medication with ID: {}", id);
        
        Medication medication = medicationRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new MedicationNotFoundException("Medication not found with ID: " + id));

        medication.softDelete();
        medicationRepository.save(medication);
        
        logger.info("Successfully soft deleted medication with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationResponse> searchMedications(String searchTerm) {
        logger.debug("Searching medications with term: {}", searchTerm);
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllActiveMedications();
        }
        
        List<Medication> medications = medicationRepository.findByNameContaining(searchTerm.trim());
        return medications.stream()
                .map(MedicationResponse::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationResponse> getMedicationsByType(Medication.MedicationType type) {
        logger.debug("Retrieving medications by type: {}", type);
        
        List<Medication> medications = medicationRepository.findByTypeAndActiveTrue(type);
        return medications.stream()
                .map(MedicationResponse::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationResponse> getMostCommonlyUsedMedications() {
        logger.debug("Retrieving most commonly used medications");
        
        List<Medication> medications = medicationRepository.findMostCommonlyUsed();
        return medications.stream()
                .map(MedicationResponse::of)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean medicationExistsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        return medicationRepository.existsByNameIgnoreCaseAndActiveTrue(name.trim());
    }
}