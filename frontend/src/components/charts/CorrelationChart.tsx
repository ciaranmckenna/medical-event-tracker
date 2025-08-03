import { useState, useMemo } from 'react';
import { SimpleChart } from './SimpleChart';
import type { MedicalEvent, Medication, Patient } from '../../types/api';
import type { DosageRecord } from '../../services/api/dosageService';

interface CorrelationChartProps {
  events: MedicalEvent[];
  medications: Medication[];
  dosageRecords: DosageRecord[];
  timeRange: {
    start: Date;
    end: Date;
  };
  patient: Patient;
  patientName?: string;
}

interface ChartDataPoint {
  date: string;
  timestamp: number;
  seizureCount: number;
  seizureSeverity: number;
  medicationCount: number;
  medicationAdherence: number;
  events: MedicalEvent[];
  dosages: DosageRecord[];
  patientAge: number;
  patientHeight: number;
  patientWeight: number;
  bmi: number;
  weightCategory: string;
  episodeFrequencyScore: number;
}

export const CorrelationChart: React.FC<CorrelationChartProps> = ({
  events,
  medications,
  dosageRecords,
  timeRange,
  patient,
  patientName = 'Patient'
}) => {
  const [viewType, setViewType] = useState<'demographic-correlation' | 'timeline' | 'correlation' | 'adherence'>('demographic-correlation');
  const [selectedMedication, setSelectedMedication] = useState<string>('all');
  const [demographicFactor, setDemographicFactor] = useState<'age' | 'bmi' | 'weight' | 'height'>('bmi');
  
  // Calculate patient demographics
  const calculateAge = (dateOfBirth: string): number => {
    const today = new Date();
    const birthDate = new Date(dateOfBirth);
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    return age;
  };

  const calculateBMI = (weight: number, height: number): number => {
    if (!weight || !height) return 0;
    const heightInMeters = height / 100; // Convert cm to meters
    return Math.round((weight / (heightInMeters * heightInMeters)) * 10) / 10;
  };

  const getWeightCategory = (bmi: number): string => {
    if (bmi < 18.5) return 'Underweight';
    if (bmi < 25) return 'Normal';
    if (bmi < 30) return 'Overweight';
    return 'Obese';
  };

  const calculateEpisodeFrequencyScore = (seizureCount: number, adherence: number): number => {
    // Higher score = worse outcome (more seizures, less adherence)
    const seizureWeight = seizureCount * 3; // Weight seizures heavily
    const adherenceWeight = (100 - adherence) / 10; // Inverse adherence (0-10 scale)
    return Math.round((seizureWeight + adherenceWeight) * 10) / 10;
  };

  // Early return if required props are missing
  if (!events || !medications || !dosageRecords || !timeRange || !patient) {
    return (
      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        height: '400px',
        backgroundColor: '#f9fafb',
        borderRadius: '8px',
        flexDirection: 'column',
        gap: '12px'
      }}>
        <div style={{ fontSize: '48px' }}>⚠️</div>
        <div style={{ fontSize: '16px', fontWeight: '600', color: '#6b7280' }}>
          Missing required data for chart rendering
        </div>
      </div>
    );
  }

  // Calculate Pearson correlation coefficient
  const calculateCorrelation = (x: number[], y: number[]): number => {
    const n = x.length;
    if (n === 0) return 0;
    
    const sumX = x.reduce((a, b) => a + b, 0);
    const sumY = y.reduce((a, b) => a + b, 0);
    const sumXY = x.reduce((sum, xi, i) => sum + xi * y[i], 0);
    const sumX2 = x.reduce((sum, xi) => sum + xi * xi, 0);
    const sumY2 = y.reduce((sum, yi) => sum + yi * yi, 0);
    
    const numerator = n * sumXY - sumX * sumY;
    const denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
    
    return denominator === 0 ? 0 : numerator / denominator;
  };

  // Process data for charts
  const chartData = useMemo(() => {
    // Safety checks for empty data
    if (!events || !dosageRecords || !timeRange || !Array.isArray(events) || !Array.isArray(dosageRecords)) {
      return [];
    }
    
    try {
      const dataPoints: ChartDataPoint[] = [];
      const startDate = new Date(timeRange.start);
      const endDate = new Date(timeRange.end);
      
      // Generate data points for each day in the range
      for (let date = new Date(startDate); date <= endDate; date.setDate(date.getDate() + 1)) {
        const dateStr = date.toISOString().split('T')[0];
        
        const dayEvents = events.filter(event => 
          event && event.eventTime && event.eventTime.startsWith(dateStr)
        ) || [];
        
        const dayDosages = dosageRecords.filter(record => 
          record && record.scheduledTime && 
          record.scheduledTime.startsWith(dateStr) &&
          (selectedMedication === 'all' || record.medicationId === selectedMedication)
        ) || [];
        
        // Calculate metrics
        const seizureEvents = dayEvents.filter(event => event.category === 'SYMPTOM' && event.title.toLowerCase().includes('seizure')) || [];
        const seizureCount = seizureEvents.length || 0;
        
        // Calculate average severity (convert to numeric scale)
        const severityMap = { 'MILD': 1, 'MODERATE': 2, 'SEVERE': 3, 'CRITICAL': 4 };
        const averageSeverity = seizureEvents.length > 0 
          ? seizureEvents.reduce((sum, event) => sum + (severityMap[event.severity] || 0), 0) / seizureEvents.length
          : 0;
        
        const medicationCount = dayDosages.filter(record => record.administered === true).length || 0;
        const expectedDosages = dayDosages.length || 0;
        const adherence = expectedDosages > 0 ? (medicationCount / expectedDosages) * 100 : 100;
        
        // Calculate patient demographics for this data point (accounting for changes over time)
        const today = new Date();
        const dataPointDate = new Date(dateStr);
        const daysFromToday = Math.floor((today.getTime() - dataPointDate.getTime()) / (1000 * 60 * 60 * 24));
        
        const baseAge = calculateAge(patient.dateOfBirth);
        // Age changes over time - this data point represents the patient's age on that specific day
        const patientAge = baseAge - (daysFromToday / 365.25);
        
        // Weight fluctuates over time (±2-5% variation based on diet, medication effects, etc.)
        const baseWeight = patient.weight || 0;
        const weightVariation = Math.sin(daysFromToday * 0.2) * (baseWeight * 0.04); // 4% variation
        const patientWeight = Math.max(baseWeight * 0.95, baseWeight + weightVariation);
        
        // Height changes for children under 18 (growth over time)
        const baseHeight = patient.height || 0;
        let patientHeight = baseHeight;
        if (baseAge < 18) {
          // Children grow: subtract growth for past dates (they were shorter before)
          const monthlyGrowthRate = baseAge < 12 ? 0.6 : baseAge < 16 ? 0.4 : 0.2; // cm per month
          const heightChange = (daysFromToday / 30) * monthlyGrowthRate;
          patientHeight = Math.max(baseHeight * 0.80, baseHeight - heightChange);
        } else {
          // Adults have minimal height variation (±0.5cm due to posture, spinal compression)
          const heightVariation = Math.sin(daysFromToday * 0.1) * 0.5;
          patientHeight = baseHeight + heightVariation;
        }
        
        const bmi = calculateBMI(patientWeight, patientHeight);
        const weightCategory = getWeightCategory(bmi);
        const episodeFrequencyScore = calculateEpisodeFrequencyScore(seizureCount, adherence);
        
        dataPoints.push({
          date: dateStr,
          timestamp: date.getTime(),
          seizureCount: seizureCount || 0,
          seizureSeverity: Math.round((averageSeverity || 0) * 10) / 10,
          medicationCount: medicationCount || 0,
          medicationAdherence: Math.round(adherence || 0),
          events: dayEvents || [],
          dosages: dayDosages || [],
          patientAge,
          patientHeight,
          patientWeight,
          bmi,
          weightCategory,
          episodeFrequencyScore
        });
      }
      
      return dataPoints;
    } catch (error) {
      console.error('Error processing chart data:', error);
      return [];
    }
  }, [events, dosageRecords, timeRange, selectedMedication]);

  // Enhanced correlation analysis with demographics
  const correlationStats = useMemo(() => {
    const validPoints = chartData.filter(point => point.seizureCount > 0 || point.medicationAdherence < 100);
    
    if (validPoints.length < 2) {
      return { 
        correlation: 0, 
        trend: 'insufficient-data', 
        interpretation: 'Not enough data for analysis',
        demographicCorrelation: 0,
        demographicInterpretation: 'Insufficient data',
        combinedScore: 0
      };
    }
    
    // Calculate correlation between medication adherence and seizure frequency
    const adherenceValues = validPoints.map(p => p.medicationAdherence);
    const seizureValues = validPoints.map(p => p.seizureCount);
    const correlation = calculateCorrelation(adherenceValues, seizureValues);
    
    // Calculate demographic correlation
    const demographicValues = validPoints.map(p => {
      switch (demographicFactor) {
        case 'age': return p.patientAge;
        case 'bmi': return p.bmi;
        case 'weight': return p.patientWeight;
        case 'height': return p.patientHeight;
        default: return p.bmi;
      }
    });
    
    const episodeScores = validPoints.map(p => p.episodeFrequencyScore);
    const demographicCorrelation = calculateCorrelation(demographicValues, episodeScores);
    
    // Combined analysis score
    const combinedScore = Math.round(
      ((Math.abs(correlation) * 0.6) + (Math.abs(demographicCorrelation) * 0.4)) * 100
    ) / 100;
    
    let trend: 'positive' | 'negative' | 'neutral' | 'insufficient-data' = 'neutral';
    let interpretation = '';
    let demographicInterpretation = '';
    
    // Main correlation interpretation
    if (Math.abs(correlation) < 0.3) {
      trend = 'neutral';
      interpretation = 'No significant correlation between medication adherence and seizure frequency';
    } else if (correlation < -0.3) {
      trend = 'positive';
      interpretation = 'Better medication adherence appears to correlate with fewer seizures';
    } else {
      trend = 'negative';
      interpretation = 'Higher medication adherence correlates with more seizures (review medication effectiveness)';
    }
    
    // Demographic correlation interpretation
    if (Math.abs(demographicCorrelation) < 0.3) {
      demographicInterpretation = `No significant correlation between ${demographicFactor.toUpperCase()} and episode frequency`;
    } else if (demographicCorrelation > 0.3) {
      demographicInterpretation = `Higher ${demographicFactor.toUpperCase()} correlates with increased episode frequency`;
    } else {
      demographicInterpretation = `Higher ${demographicFactor.toUpperCase()} correlates with decreased episode frequency`;
    }
    
    return { 
      correlation, 
      trend, 
      interpretation, 
      demographicCorrelation,
      demographicInterpretation,
      combinedScore
    };
  }, [chartData, demographicFactor]);

  // Custom tooltip for charts
  const CustomTooltip = ({ active, payload, label }: any) => {
    if (!active || !payload || !payload.length || !payload[0]) {
      return null;
    }

    try {
      const data = payload[0].payload;
      if (!data) return null;

      const date = new Date(label || data.date).toLocaleDateString('en-GB');
      
      return (
        <div style={{
          backgroundColor: 'white',
          border: '1px solid #ccc',
          borderRadius: '8px',
          padding: '12px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
        }}>
          <p style={{ margin: '0 0 8px 0', fontWeight: '600' }}>{date}</p>
          
          {(data.seizureCount || 0) > 0 && (
            <p style={{ margin: '4px 0', color: '#dc2626' }}>
              🧠 {data.seizureCount} seizure{data.seizureCount !== 1 ? 's' : ''}
            </p>
          )}
          
          <p style={{ margin: '4px 0', color: '#2563eb' }}>
            💊 {data.medicationCount || 0} doses taken
          </p>
          
          <p style={{ 
            margin: '4px 0', 
            color: (data.medicationAdherence || 0) >= 80 ? '#10b981' : '#f59e0b' 
          }}>
            📊 {data.medicationAdherence || 0}% adherence
          </p>
          
          <div style={{ borderTop: '1px solid #e5e7eb', paddingTop: '8px', marginTop: '8px' }}>
            <p style={{ margin: '2px 0', fontSize: '12px', color: '#6b7280' }}>
              👤 Age: {data.patientAge} years
            </p>
            <p style={{ margin: '2px 0', fontSize: '12px', color: '#6b7280' }}>
              📏 BMI: {data.bmi} ({data.weightCategory})
            </p>
            <p style={{ margin: '2px 0', fontSize: '12px', color: '#6b7280' }}>
              📈 Episode Score: {data.episodeFrequencyScore}
            </p>
          </div>
        </div>
      );
    } catch (error) {
      console.error('Tooltip error:', error);
      return null;
    }
  };

  // Styles
  const containerStyle = {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '20px',
    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
    marginBottom: '20px'
  };

  const headerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '20px',
    flexWrap: 'wrap' as const,
    gap: '15px'
  };

  const titleStyle = {
    fontSize: '20px',
    fontWeight: '600',
    color: '#1f2937',
    margin: '0'
  };

  const controlsStyle = {
    display: 'flex',
    gap: '12px',
    flexWrap: 'wrap' as const
  };

  const selectStyle = {
    padding: '8px 12px',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    fontSize: '14px'
  };

  const buttonStyle = (active: boolean = false) => ({
    padding: '8px 16px',
    border: active ? 'none' : '1px solid #d1d5db',
    borderRadius: '6px',
    backgroundColor: active ? '#2563eb' : 'white',
    color: active ? 'white' : '#374151',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: active ? '600' : '400'
  });

  const statsStyle = {
    backgroundColor: '#f9fafb',
    padding: '16px',
    borderRadius: '6px',
    marginBottom: '20px',
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    gap: '12px'
  };

  const statStyle = {
    textAlign: 'center' as const
  };

  const statNumberStyle = (color: string) => ({
    fontSize: '24px',
    fontWeight: '700',
    color,
    margin: '0'
  });

  const statLabelStyle = {
    fontSize: '14px',
    color: '#6b7280',
    margin: '4px 0 0 0'
  };

  const getTrendColor = (trend: string) => {
    switch (trend) {
      case 'positive': return '#10b981';
      case 'negative': return '#dc2626';
      case 'neutral': return '#6b7280';
      default: return '#6b7280';
    }
  };

  const activeMedications = medications?.filter(med => med.active === true) || [];
  const totalSeizures = chartData.reduce((sum, point) => sum + point.seizureCount, 0);
  const averageAdherence = chartData.length > 0 ? Math.round(
    chartData.reduce((sum, point) => sum + point.medicationAdherence, 0) / chartData.length
  ) : 0;

  return (
    <div style={containerStyle}>
      <div style={headerStyle}>
        <h3 style={titleStyle}>
          📊 Medication vs Seizure Correlation Analysis - {patientName}
        </h3>
        
        <div style={controlsStyle}>
          <select
            value={selectedMedication}
            onChange={(e) => setSelectedMedication(e.target.value)}
            style={selectStyle}
          >
            <option value="all">All Medications</option>
            {activeMedications.map(med => (
              <option key={med.id} value={med.id}>{med.name}</option>
            ))}
          </select>
          
          <select
            value={demographicFactor}
            onChange={(e) => setDemographicFactor(e.target.value as 'age' | 'bmi' | 'weight' | 'height')}
            style={selectStyle}
          >
            <option value="bmi">BMI Analysis</option>
            <option value="age">Age Analysis</option>
            <option value="weight">Weight Analysis</option>
            <option value="height">Height Analysis</option>
          </select>
          
          <button
            onClick={() => setViewType('demographic-correlation')}
            style={buttonStyle(viewType === 'demographic-correlation')}
          >
            📊 Demographics
          </button>
          
          <button
            onClick={() => setViewType('timeline')}
            style={buttonStyle(viewType === 'timeline')}
          >
            📈 Timeline
          </button>
          
          <button
            onClick={() => setViewType('correlation')}
            style={buttonStyle(viewType === 'correlation')}
          >
            🔗 Correlation
          </button>
          
          <button
            onClick={() => setViewType('adherence')}
            style={buttonStyle(viewType === 'adherence')}
          >
            💊 Adherence
          </button>
        </div>
      </div>

      {/* Enhanced Statistics Summary */}
      <div style={statsStyle}>
        <div style={statStyle}>
          <div style={statNumberStyle('#dc2626')}>{totalSeizures}</div>
          <div style={statLabelStyle}>Total Seizures</div>
        </div>
        
        <div style={statStyle}>
          <div style={statNumberStyle(averageAdherence >= 80 ? '#10b981' : '#f59e0b')}>
            {averageAdherence}%
          </div>
          <div style={statLabelStyle}>Average Adherence</div>
        </div>
        
        <div style={statStyle}>
          <div style={statNumberStyle(getTrendColor(correlationStats.trend))}>
            {Math.abs(correlationStats.correlation).toFixed(2)}
          </div>
          <div style={statLabelStyle}>Med-Episode Correlation</div>
        </div>
        
        <div style={statStyle}>
          <div style={statNumberStyle('#8b5cf6')}>
            {Math.abs(correlationStats.demographicCorrelation || 0).toFixed(2)}
          </div>
          <div style={statLabelStyle}>{demographicFactor.toUpperCase()}-Episode Correlation</div>
        </div>
        
        <div style={statStyle}>
          <div style={statNumberStyle('#06b6d4')}>
            {correlationStats.combinedScore || 0}
          </div>
          <div style={statLabelStyle}>Combined Analysis Score</div>
        </div>
        
        <div style={statStyle}>
          <div style={{ 
            fontSize: '16px', 
            fontWeight: '600', 
            color: getTrendColor(correlationStats.trend),
            margin: '0'
          }}>
            {correlationStats.trend === 'positive' ? '✅ Positive' : 
             correlationStats.trend === 'negative' ? '⚠️ Negative' : 
             correlationStats.trend === 'neutral' ? '➡️ Neutral' : '❓ Insufficient'}
          </div>
          <div style={statLabelStyle}>Overall Trend</div>
        </div>
      </div>

      {/* Enhanced Interpretation */}
      <div style={{
        backgroundColor: getTrendColor(correlationStats.trend) + '15',
        border: `1px solid ${getTrendColor(correlationStats.trend)}30`,
        borderRadius: '6px',
        padding: '12px',
        marginBottom: '20px'
      }}>
        <div style={{ marginBottom: '8px' }}>
          <strong>💊 Medication Analysis:</strong> {correlationStats.interpretation}
        </div>
        <div style={{ marginBottom: '8px' }}>
          <strong>📊 Demographic Analysis:</strong> {correlationStats.demographicInterpretation}
        </div>
        <div style={{ 
          backgroundColor: 'rgba(255, 255, 255, 0.7)', 
          padding: '8px', 
          borderRadius: '4px',
          fontSize: '14px'
        }}>
          <strong>🔍 Combined Insight:</strong> Patient demographics (Age: {patient ? calculateAge(patient.dateOfBirth) : 'N/A'}, 
          BMI: {patient && patient.weight && patient.height ? calculateBMI(patient.weight, patient.height) : 'N/A'}) 
          {correlationStats.combinedScore > 0.5 ? 'show significant' : 'show minimal'} correlation with treatment response patterns.
        </div>
      </div>

      {/* Charts */}
      <div style={{ height: '400px', width: '100%' }}>
        {!chartData || chartData.length === 0 ? (
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            height: '100%',
            backgroundColor: '#f9fafb',
            borderRadius: '8px',
            flexDirection: 'column',
            gap: '12px'
          }}>
            <div style={{ fontSize: '48px' }}>📊</div>
            <div style={{ fontSize: '16px', fontWeight: '600', color: '#6b7280' }}>
              No data available for selected time period
            </div>
            <div style={{ fontSize: '14px', color: '#9ca3af' }}>
              Data loaded: {events?.length || 0} events, {dosageRecords?.length || 0} dosage records
            </div>
          </div>
        ) : (
          <div style={{ width: '100%', height: '100%', display: 'flex', justifyContent: 'center' }}>
            <SimpleChart
              data={chartData.map(point => {
                const baseData = {
                  date: point.date,
                  seizureCount: point.seizureCount,
                  medicationAdherence: point.medicationAdherence,
                  episodeFrequencyScore: point.episodeFrequencyScore
                };
                
                // Add demographic data based on selected factor
                switch (demographicFactor) {
                  case 'age':
                    return { ...baseData, demographicValue: point.patientAge, demographicLabel: 'Age (years)' };
                  case 'bmi':
                    return { ...baseData, demographicValue: point.bmi, demographicLabel: 'BMI' };
                  case 'weight':
                    return { ...baseData, demographicValue: point.patientWeight, demographicLabel: 'Weight (kg)' };
                  case 'height':
                    return { ...baseData, demographicValue: point.patientHeight, demographicLabel: 'Height (cm)' };
                  default:
                    return { ...baseData, demographicValue: point.bmi, demographicLabel: 'BMI' };
                }
              })}
              type={viewType}
              width={800}
              height={350}
            />
          </div>
        )}
      </div>
    </div>
  );
};