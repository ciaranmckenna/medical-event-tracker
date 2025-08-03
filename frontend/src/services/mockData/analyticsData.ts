import type { Patient, Medication, MedicalEvent } from '../../types/api';
import type { DosageRecord } from '../api/dosageService';

// Mock data that demonstrates clear correlations between demographics, medication, and episodes
export const generateMockAnalyticsData = () => {
  const patients: Patient[] = [
    {
      id: 'patient-1',
      firstName: 'Emma',
      lastName: 'Johnson',
      dateOfBirth: '2010-03-15', // 14 years old
      height: 155,
      weight: 45,
      emergencyContact: 'Sarah Johnson',
      emergencyPhone: '+44 7700 900001',
      notes: 'Young patient, responds well to medication',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'patient-2',
      firstName: 'James',
      lastName: 'Wilson',
      dateOfBirth: '2005-08-22', // 19 years old
      height: 175,
      weight: 70,
      emergencyContact: 'Margaret Wilson',
      emergencyPhone: '+44 7700 900002',
      notes: 'Good medication adherence, stable condition',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'patient-3',
      firstName: 'Sophia',
      lastName: 'Brown',
      dateOfBirth: '1995-12-10', // 28 years old
      height: 160,
      weight: 55,
      emergencyContact: 'David Brown',
      emergencyPhone: '+44 7700 900003',
      notes: 'Adult patient, self-managing medication',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'patient-4',
      firstName: 'Oliver',
      lastName: 'Davis',
      dateOfBirth: '2012-06-03', // 12 years old
      height: 140,
      weight: 35,
      emergencyContact: 'Lisa Davis',
      emergencyPhone: '+44 7700 900004',
      notes: 'Child patient, requires supervision for medication',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'patient-5',
      firstName: 'Isabella',
      lastName: 'Miller',
      dateOfBirth: '1988-04-18', // 36 years old
      height: 168,
      weight: 75,
      emergencyContact: 'Robert Miller',
      emergencyPhone: '+44 7700 900005',
      notes: 'Adult patient, higher BMI may affect medication metabolism',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'patient-6',
      firstName: 'Ethan',
      lastName: 'Garcia',
      dateOfBirth: '2008-11-25', // 15 years old
      height: 165,
      weight: 50,
      emergencyContact: 'Maria Garcia',
      emergencyPhone: '+44 7700 900006',
      notes: 'Teenager, medication compliance challenges',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'patient-7',
      firstName: 'Ava',
      lastName: 'Rodriguez',
      dateOfBirth: '1975-09-14', // 49 years old
      height: 163,
      weight: 68,
      emergencyContact: 'Carlos Rodriguez',
      emergencyPhone: '+44 7700 900007',
      notes: 'Older adult, may need dosage adjustments',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'patient-8',
      firstName: 'William',
      lastName: 'Martinez',
      dateOfBirth: '2000-01-30', // 24 years old
      height: 180,
      weight: 85,
      emergencyContact: 'Ana Martinez',
      emergencyPhone: '+44 7700 900008',
      notes: 'Young adult, athletic build, good adherence',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'patient-9',
      firstName: 'Mia',
      lastName: 'Anderson',
      dateOfBirth: '2014-07-08', // 10 years old
      height: 135,
      weight: 30,
      emergencyContact: 'Jennifer Anderson',
      emergencyPhone: '+44 7700 900009',
      notes: 'Young child, very sensitive to medication changes',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'patient-10',
      firstName: 'Alexander',
      lastName: 'Taylor',
      dateOfBirth: '1982-05-20', // 42 years old
      height: 178,
      weight: 90,
      emergencyContact: 'Helen Taylor',
      emergencyPhone: '+44 7700 900010',
      notes: 'Middle-aged adult, overweight, medication absorption issues',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    }
  ];

  const medications: Medication[] = [
    {
      id: 'med-1',
      patientId: 'patient-1',
      name: 'Levetiracetam',
      dosage: 250,
      unit: 'mg',
      frequency: 'TWICE_DAILY',
      startDate: '2024-01-01',
      active: true,
      notes: 'Age-appropriate dosage for pediatric patient',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'med-2',
      patientId: 'patient-2',
      name: 'Lamotrigine',
      dosage: 100,
      unit: 'mg',
      frequency: 'TWICE_DAILY',
      startDate: '2024-01-01',
      active: true,
      notes: 'Standard adult dosage',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'med-3',
      patientId: 'patient-3',
      name: 'Carbamazepine',
      dosage: 200,
      unit: 'mg',
      frequency: 'TWICE_DAILY',
      startDate: '2024-01-01',
      active: true,
      notes: 'Well-controlled on current dose',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'med-4',
      patientId: 'patient-4',
      name: 'Valproic Acid',
      dosage: 125,
      unit: 'mg',
      frequency: 'TWICE_DAILY',
      startDate: '2024-01-01',
      active: true,
      notes: 'Weight-based dosing for child',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'med-5',
      patientId: 'patient-5',
      name: 'Phenytoin',
      dosage: 300,
      unit: 'mg',
      frequency: 'ONCE_DAILY',
      startDate: '2024-01-01',
      active: true,
      notes: 'Higher dose due to weight and metabolism',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'med-6',
      patientId: 'patient-6',
      name: 'Oxcarbazepine',
      dosage: 300,
      unit: 'mg',
      frequency: 'TWICE_DAILY',
      startDate: '2024-01-01',
      active: true,
      notes: 'Teenage dosing, compliance issues noted',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'med-7',
      patientId: 'patient-7',
      name: 'Levetiracetam',
      dosage: 500,
      unit: 'mg',
      frequency: 'TWICE_DAILY',
      startDate: '2024-01-01',
      active: true,
      notes: 'Age-adjusted dosing for older adult',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'med-8',
      patientId: 'patient-8',
      name: 'Lamotrigine',
      dosage: 150,
      unit: 'mg',
      frequency: 'TWICE_DAILY',
      startDate: '2024-01-01',
      active: true,
      notes: 'Higher dose for larger body mass',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'med-9',
      patientId: 'patient-9',
      name: 'Levetiracetam',
      dosage: 125,
      unit: 'mg',
      frequency: 'TWICE_DAILY',
      startDate: '2024-01-01',
      active: true,
      notes: 'Very low dose for young child',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    },
    {
      id: 'med-10',
      patientId: 'patient-10',
      name: 'Carbamazepine',
      dosage: 400,
      unit: 'mg',
      frequency: 'TWICE_DAILY',
      startDate: '2024-01-01',
      active: true,
      notes: 'High dose due to weight and poor absorption',
      createdAt: '2024-01-01T00:00:00Z',
      updatedAt: '2024-01-01T00:00:00Z'
    }
  ];

  // Generate dosage records for the last 30 days with realistic adherence patterns
  const generateDosageRecords = (): DosageRecord[] => {
    const records: DosageRecord[] = [];
    const today = new Date();
    
    patients.forEach(patient => {
      const medication = medications.find(med => med.patientId === patient.id);
      if (!medication) return;

      // Calculate adherence pattern based on demographics
      const age = Math.floor((today.getTime() - new Date(patient.dateOfBirth).getTime()) / (365.25 * 24 * 60 * 60 * 1000));
      const bmi = patient.weight && patient.height ? (patient.weight / Math.pow(patient.height / 100, 2)) : 22;
      
      // Demographics-based adherence patterns:
      // - Children (5-12): 85% adherence (parent-supervised)
      // - Teenagers (13-19): 65% adherence (compliance issues)
      // - Young adults (20-30): 80% adherence (good self-management)
      // - Adults (31-45): 75% adherence (busy lifestyle)
      // - Older adults (45+): 70% adherence (potential memory issues)
      // - Higher BMI: -5% adherence (medication absorption issues)
      
      let baseAdherence = 80;
      if (age <= 12) baseAdherence = 85;
      else if (age <= 19) baseAdherence = 65;
      else if (age <= 30) baseAdherence = 80;
      else if (age <= 45) baseAdherence = 75;
      else baseAdherence = 70;
      
      if (bmi > 25) baseAdherence -= 5;
      if (bmi > 30) baseAdherence -= 5;
      
      const dosesPerDay = medication.frequency === 'ONCE_DAILY' ? 1 : 2;
      
      for (let day = 0; day < 30; day++) {
        const date = new Date(today);
        date.setDate(date.getDate() - day);
        const dateStr = date.toISOString().split('T')[0];
        
        for (let dose = 0; dose < dosesPerDay; dose++) {
          const hour = dose === 0 ? 8 : 20; // 8 AM and 8 PM
          const scheduledTime = `${dateStr}T${hour.toString().padStart(2, '0')}:00:00`;
          
          // Add some randomness but maintain overall adherence pattern
          const randomFactor = Math.random();
          const administered = randomFactor < (baseAdherence / 100);
          
          records.push({
            id: `dosage-${patient.id}-${day}-${dose}`,
            patientId: patient.id,
            medicationId: medication.id,
            scheduledTime,
            administered,
            administeredTime: administered ? scheduledTime : undefined,
            dosageAmount: medication.dosage,
            schedule: dose === 0 ? 'AM' as const : 'PM' as const,
            notes: administered ? undefined : 'Missed dose',
            createdAt: scheduledTime,
            updatedAt: scheduledTime
          });
        }
      }
    });
    
    return records;
  };

  // Generate medical events with realistic correlations
  const generateMedicalEvents = (): MedicalEvent[] => {
    const events: MedicalEvent[] = [];
    const today = new Date();
    
    patients.forEach(patient => {
      const age = Math.floor((today.getTime() - new Date(patient.dateOfBirth).getTime()) / (365.25 * 24 * 60 * 60 * 1000));
      const bmi = patient.weight && patient.height ? (patient.weight / Math.pow(patient.height / 100, 2)) : 22;
      
      // Calculate expected seizure frequency based on demographics and adherence
      // DEMO VERSION: Higher frequency for demonstration purposes
      // Lower adherence = more seizures
      // Extreme ages (very young/old) = potentially more seizures
      // Higher BMI = potentially more seizures (medication absorption issues)
      
      let baseSeizureRisk = 0.6; // 60% chance per week base (DEMO: much higher for visibility)
      
      // Age factors
      if (age <= 10) baseSeizureRisk += 0.2; // Young children more vulnerable
      if (age >= 45) baseSeizureRisk += 0.15; // Older adults more vulnerable
      if (age >= 13 && age <= 19) baseSeizureRisk += 0.25; // Teenagers with compliance issues
      
      // BMI factors
      if (bmi > 25) baseSeizureRisk += 0.1;
      if (bmi > 30) baseSeizureRisk += 0.15;
      
      // Adherence factor (we'll simulate based on patient profile)
      const medication = medications.find(med => med.patientId === patient.id);
      if (medication) {
        // Simulate adherence based on age (as calculated above)
        let adherence = 80;
        if (age <= 12) adherence = 85;
        else if (age <= 19) adherence = 65;
        else if (age <= 30) adherence = 80;
        else if (age <= 45) adherence = 75;
        else adherence = 70;
        
        if (bmi > 25) adherence -= 5;
        if (bmi > 30) adherence -= 5;
        
        // Poor adherence increases seizure risk significantly
        if (adherence < 70) baseSeizureRisk += 0.4; // DEMO: Higher impact
        else if (adherence < 80) baseSeizureRisk += 0.2;
      }
      
      // Generate events for the last 30 days
      for (let day = 0; day < 30; day++) {
        const date = new Date(today);
        date.setDate(date.getDate() - day);
        
        // Check if a seizure occurs (weekly probability converted to daily)
        const dailyRisk = baseSeizureRisk / 4; // DEMO: Convert to higher daily probability
        
        // DEMO: Allow multiple episodes per day for high-risk patients
        const maxEpisodesPerDay = baseSeizureRisk > 1.0 ? 2 : 1;
        
        for (let episode = 0; episode < maxEpisodesPerDay; episode++) {
          if (Math.random() < dailyRisk) {
            const hour = Math.floor(Math.random() * 24);
            const minute = Math.floor(Math.random() * 60);
            const eventTime = `${date.toISOString().split('T')[0]}T${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:00`;
            
            // Severity based on age and control
            let severity: 'MILD' | 'MODERATE' | 'SEVERE' | 'CRITICAL' = 'MILD';
            const severityRoll = Math.random();
            
            if (age <= 10 || age >= 45) {
              // Extreme ages more likely to have severe episodes
              if (severityRoll < 0.1) severity = 'CRITICAL';
              else if (severityRoll < 0.3) severity = 'SEVERE';
              else if (severityRoll < 0.6) severity = 'MODERATE';
            } else {
              if (severityRoll < 0.05) severity = 'CRITICAL';
              else if (severityRoll < 0.2) severity = 'SEVERE';
              else if (severityRoll < 0.5) severity = 'MODERATE';
            }
            
            events.push({
              id: `event-${patient.id}-${day}-${episode}-${Date.now()}`,
              patientId: patient.id,
              medicationId: medication?.id,
              eventTime,
              title: `Seizure Episode - ${severity.toLowerCase()}`,
              description: `${severity.toLowerCase()} seizure episode`,
              severity,
              category: 'SYMPTOM',
              duration: Math.floor(Math.random() * 300) + 30, // 30 seconds to 5 minutes
              resolved: true,
              createdAt: eventTime,
              updatedAt: eventTime
            } as MedicalEvent);
          }
        }
      }
    });
    
    return events;
  };

  return {
    patients,
    medications,
    dosageRecords: generateDosageRecords(),
    medicalEvents: generateMedicalEvents()
  };
};

// Export the generated mock data
export const mockAnalyticsData = generateMockAnalyticsData();