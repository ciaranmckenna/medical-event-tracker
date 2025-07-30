import { useState, useMemo } from 'react';
import { SimpleChart } from './SimpleChart';
import type { MedicalEvent, Medication } from '../../types/api';
import type { DosageRecord } from '../../services/api/dosageService';

interface CorrelationChartProps {
  events: MedicalEvent[];
  medications: Medication[];
  dosageRecords: DosageRecord[];
  timeRange: {
    start: Date;
    end: Date;
  };
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
}

export const CorrelationChart: React.FC<CorrelationChartProps> = ({
  events,
  medications,
  dosageRecords,
  timeRange,
  patientName = 'Patient'
}) => {
  const [viewType, setViewType] = useState<'timeline' | 'correlation' | 'adherence'>('timeline');
  const [selectedMedication, setSelectedMedication] = useState<string>('all');
  
  // Early return if required props are missing
  if (!events || !medications || !dosageRecords || !timeRange) {
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
          event && event.eventTimestamp && event.eventTimestamp.startsWith(dateStr)
        ) || [];
        
        const dayDosages = dosageRecords.filter(record => 
          record && record.scheduledTime && 
          record.scheduledTime.startsWith(dateStr) &&
          (selectedMedication === 'all' || record.medicationId === selectedMedication)
        ) || [];
        
        // Calculate metrics
        const seizureEvents = dayEvents.filter(event => event.type === 'SEIZURE') || [];
        const seizureCount = seizureEvents.length || 0;
        
        // Calculate average severity (convert to numeric scale)
        const severityMap = { 'MILD': 1, 'MODERATE': 2, 'SEVERE': 3, 'CRITICAL': 4 };
        const averageSeverity = seizureEvents.length > 0 
          ? seizureEvents.reduce((sum, event) => sum + (severityMap[event.severity] || 0), 0) / seizureEvents.length
          : 0;
        
        const medicationCount = dayDosages.filter(record => record.administered === true).length || 0;
        const expectedDosages = dayDosages.length || 0;
        const adherence = expectedDosages > 0 ? (medicationCount / expectedDosages) * 100 : 100;
        
        dataPoints.push({
          date: dateStr,
          timestamp: date.getTime(),
          seizureCount: seizureCount || 0,
          seizureSeverity: Math.round((averageSeverity || 0) * 10) / 10,
          medicationCount: medicationCount || 0,
          medicationAdherence: Math.round(adherence || 0),
          events: dayEvents || [],
          dosages: dayDosages || []
        });
      }
      
      return dataPoints;
    } catch (error) {
      console.error('Error processing chart data:', error);
      return [];
    }
  }, [events, dosageRecords, timeRange, selectedMedication]);

  // Correlation analysis
  const correlationStats = useMemo(() => {
    const validPoints = chartData.filter(point => point.seizureCount > 0 || point.medicationAdherence < 100);
    
    if (validPoints.length < 2) {
      return { correlation: 0, trend: 'insufficient-data', interpretation: 'Not enough data for analysis' };
    }
    
    // Calculate correlation between medication adherence and seizure frequency
    const adherenceValues = validPoints.map(p => p.medicationAdherence);
    const seizureValues = validPoints.map(p => p.seizureCount);
    
    const correlation = calculateCorrelation(adherenceValues, seizureValues);
    
    let trend: 'positive' | 'negative' | 'neutral' | 'insufficient-data' = 'neutral';
    let interpretation = '';
    
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
    
    return { correlation, trend, interpretation };
  }, [chartData]);

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

  const activeMedications = medications?.filter(med => med.status === 'ACTIVE') || [];
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
          
          <button
            onClick={() => setViewType('timeline')}
            style={buttonStyle(viewType === 'timeline')}
          >
            Timeline
          </button>
          
          <button
            onClick={() => setViewType('correlation')}
            style={buttonStyle(viewType === 'correlation')}
          >
            Correlation
          </button>
          
          <button
            onClick={() => setViewType('adherence')}
            style={buttonStyle(viewType === 'adherence')}
          >
            Adherence
          </button>
        </div>
      </div>

      {/* Statistics Summary */}
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
          <div style={statLabelStyle}>Correlation Strength</div>
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
          <div style={statLabelStyle}>Correlation Trend</div>
        </div>
      </div>

      {/* Interpretation */}
      <div style={{
        backgroundColor: getTrendColor(correlationStats.trend) + '15',
        border: `1px solid ${getTrendColor(correlationStats.trend)}30`,
        borderRadius: '6px',
        padding: '12px',
        marginBottom: '20px'
      }}>
        <strong>Analysis:</strong> {correlationStats.interpretation}
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
              data={chartData.map(point => ({
                date: point.date,
                seizureCount: point.seizureCount,
                medicationAdherence: point.medicationAdherence
              }))}
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