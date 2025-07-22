package com.ciaranmckenna.medical_event_tracker.service.impl;

import com.ciaranmckenna.medical_event_tracker.dto.PatientCreateRequest;
import com.ciaranmckenna.medical_event_tracker.dto.PatientResponse;
import com.ciaranmckenna.medical_event_tracker.dto.PatientUpdateRequest;
import com.ciaranmckenna.medical_event_tracker.entity.Patient;
import com.ciaranmckenna.medical_event_tracker.entity.User;
import com.ciaranmckenna.medical_event_tracker.exception.DuplicatePatientException;
import com.ciaranmckenna.medical_event_tracker.exception.PatientNotFoundException;
import com.ciaranmckenna.medical_event_tracker.repository.PatientRepository;
import com.ciaranmckenna.medical_event_tracker.repository.PatientMedicationRepository;
import com.ciaranmckenna.medical_event_tracker.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMedicationRepository patientMedicationRepository;

    public PatientServiceImpl(PatientRepository patientRepository,
                             PatientMedicationRepository patientMedicationRepository) {
        this.patientRepository = patientRepository;
        this.patientMedicationRepository = patientMedicationRepository;
    }

    @Override
    public PatientResponse createPatient(PatientCreateRequest request, User user) {
        // Check for duplicate patient
        if (patientExists(request.firstName(), request.lastName(), request.dateOfBirth(), user)) {
            throw new DuplicatePatientException(request.firstName(), request.lastName(), request.dateOfBirth().toString());
        }

        Patient patient = new Patient(
            request.firstName(),
            request.lastName(),
            request.dateOfBirth(),
            request.gender(),
            user
        );

        patient.setWeightKg(request.weightKg());
        patient.setHeightCm(request.heightCm());
        patient.setNotes(request.notes());

        Patient savedPatient = patientRepository.save(patient);
        
        return PatientResponse.fromEntity(savedPatient, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getActivePatients(User user) {
        List<Patient> patients = patientRepository.findByUserAndActiveTrue(user);
        return patients.stream()
            .map(patient -> {
                long medicationCount = patientMedicationRepository.countByPatientAndActiveTrue(patient);
                return PatientResponse.fromEntity(patient, (int) medicationCount);
            })
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients(User user) {
        List<Patient> patients = patientRepository.findByUser(user);
        return patients.stream()
            .map(patient -> {
                long medicationCount = patientMedicationRepository.countByPatientAndActiveTrue(patient);
                return PatientResponse.fromEntity(patient, (int) medicationCount);
            })
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(UUID patientId, User user) {
        Patient patient = patientRepository.findByIdAndUser(patientId, user)
            .orElseThrow(() -> new PatientNotFoundException(patientId, user.getId().toString()));
        
        long medicationCount = patientMedicationRepository.countByPatientAndActiveTrue(patient);
        return PatientResponse.fromEntity(patient, (int) medicationCount);
    }

    @Override
    public PatientResponse updatePatient(UUID patientId, PatientUpdateRequest request, User user) {
        Patient patient = patientRepository.findByIdAndUserAndActiveTrue(patientId, user)
            .orElseThrow(() -> new PatientNotFoundException(patientId, user.getId().toString()));

        // Check for duplicate if name or DOB changed
        if (!patient.getFirstName().equals(request.firstName()) ||
            !patient.getLastName().equals(request.lastName()) ||
            !patient.getDateOfBirth().equals(request.dateOfBirth())) {
            
            if (patientExists(request.firstName(), request.lastName(), request.dateOfBirth(), user)) {
                throw new DuplicatePatientException(request.firstName(), request.lastName(), request.dateOfBirth().toString());
            }
        }

        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setWeightKg(request.weightKg());
        patient.setHeightCm(request.heightCm());
        patient.setNotes(request.notes());

        Patient savedPatient = patientRepository.save(patient);
        long medicationCount = patientMedicationRepository.countByPatientAndActiveTrue(patient);
        
        return PatientResponse.fromEntity(savedPatient, (int) medicationCount);
    }

    @Override
    public void deletePatient(UUID patientId, User user) {
        Patient patient = patientRepository.findByIdAndUserAndActiveTrue(patientId, user)
            .orElseThrow(() -> new PatientNotFoundException(patientId, user.getId().toString()));

        patient.softDelete();
        patientRepository.save(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> searchPatientsByName(String searchTerm, User user) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getActivePatients(user);
        }

        List<Patient> patients = patientRepository.findByUserAndNameContaining(user, searchTerm.trim());
        return patients.stream()
            .map(patient -> {
                long medicationCount = patientMedicationRepository.countByPatientAndActiveTrue(patient);
                return PatientResponse.fromEntity(patient, (int) medicationCount);
            })
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> findPatientsByAgeRange(int minAge, int maxAge, User user) {
        if (minAge < 0 || maxAge < 0 || minAge > maxAge) {
            throw new IllegalArgumentException("Invalid age range");
        }

        LocalDate maxBirthDate = LocalDate.now().minusYears(minAge);
        LocalDate minBirthDate = LocalDate.now().minusYears(maxAge + 1);

        List<Patient> patients = patientRepository.findByUserAndDateOfBirthBetween(user, minBirthDate, maxBirthDate);
        return patients.stream()
            .map(patient -> {
                long medicationCount = patientMedicationRepository.countByPatientAndActiveTrue(patient);
                return PatientResponse.fromEntity(patient, (int) medicationCount);
            })
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> getPatientsWithActiveMedications(User user) {
        List<Patient> patients = patientRepository.findByUserWithActiveMedications(user);
        return patients.stream()
            .map(patient -> {
                long medicationCount = patientMedicationRepository.countByPatientAndActiveTrue(patient);
                return PatientResponse.fromEntity(patient, (int) medicationCount);
            })
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getActivePatientCount(User user) {
        return patientRepository.countByUserAndActiveTrue(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean patientExists(String firstName, String lastName, LocalDate dateOfBirth, User user) {
        return patientRepository.existsByUserAndFirstNameAndLastNameAndDateOfBirthAndActiveTrue(
            user, firstName, lastName, dateOfBirth);
    }
}