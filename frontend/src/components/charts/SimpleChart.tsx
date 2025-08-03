import React from 'react';

interface ChartDataPoint {
  date: string;
  seizureCount: number;
  medicationAdherence: number;
  episodeFrequencyScore?: number;
  demographicValue?: number;
  demographicLabel?: string;
}

interface SimpleChartProps {
  data: ChartDataPoint[];
  width?: number;
  height?: number;
  type: 'demographic-correlation' | 'timeline' | 'correlation' | 'adherence';
}

export const SimpleChart: React.FC<SimpleChartProps> = ({
  data,
  width = 800,
  height = 300,
  type
}) => {
  if (!data || data.length === 0) {
    return (
      <div style={{
        width: '100%',
        height: '300px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#f9fafb',
        borderRadius: '8px',
        color: '#6b7280'
      }}>
        No data to display
      </div>
    );
  }

  const margin = { top: 20, right: 50, bottom: 40, left: 50 };
  const chartWidth = width - margin.left - margin.right;
  const chartHeight = height - margin.top - margin.bottom;

  // Calculate scales
  const maxSeizures = Math.max(...data.map(d => d.seizureCount), 1);
  const maxAdherence = 100;

  const renderTimelineChart = () => {
    return (
      <svg width={width} height={height} style={{ border: '1px solid #e5e7eb', borderRadius: '8px' }}>
        {/* Background */}
        <rect width={width} height={height} fill="white" />
        
        {/* Grid lines */}
        {[0, 1, 2, 3, 4, 5].map(i => (
          <g key={`grid-${i}`}>
            <line
              x1={margin.left}
              y1={margin.top + (chartHeight / 5) * i}
              x2={width - margin.right}
              y2={margin.top + (chartHeight / 5) * i}
              stroke="#f3f4f6"
              strokeDasharray="2,2"
            />
          </g>
        ))}

        {/* Y-axis labels (left - seizures) */}
        {[0, 1, 2, 3, 4].map(i => (
          <text
            key={`y-label-${i}`}
            x={margin.left - 10}
            y={margin.top + chartHeight - (chartHeight / 4) * i + 5}
            textAnchor="end"
            fontSize="11"
            fontWeight="600"
            fill="#dc2626"
          >
            {i}
          </text>
        ))}

        {/* Y-axis labels (right - adherence) */}
        {[0, 25, 50, 75, 100].map((value, i) => (
          <text
            key={`y-label-right-${i}`}
            x={width - margin.right + 15}
            y={margin.top + chartHeight - (chartHeight / 4) * i + 5}
            textAnchor="start"
            fontSize="11"
            fontWeight="600"
            fill="#0ea5e9"
          >
            {value}%
          </text>
        ))}

        {/* Data points and lines */}
        {data.map((point, index) => {
          const x = margin.left + (chartWidth / (data.length - 1)) * index;
          const seizureY = margin.top + chartHeight - (point.seizureCount / maxSeizures) * chartHeight;
          const adherenceY = margin.top + chartHeight - (point.medicationAdherence / maxAdherence) * chartHeight;

          return (
            <g key={`point-${index}`}>
              {/* Seizure point */}
              <circle
                cx={x}
                cy={seizureY}
                r="6"
                fill="#dc2626"
                stroke="white"
                strokeWidth="3"
              />
              
              {/* Adherence point */}
              <circle
                cx={x}
                cy={adherenceY}
                r="4"
                fill="#0ea5e9"
                stroke="white"
                strokeWidth="2"
              />

              {/* Connect lines */}
              {index > 0 && (
                <>
                  <line
                    x1={margin.left + (chartWidth / (data.length - 1)) * (index - 1)}
                    y1={margin.top + chartHeight - (data[index - 1].seizureCount / maxSeizures) * chartHeight}
                    x2={x}
                    y2={seizureY}
                    stroke="#dc2626"
                    strokeWidth="4"
                  />
                  <line
                    x1={margin.left + (chartWidth / (data.length - 1)) * (index - 1)}
                    y1={margin.top + chartHeight - (data[index - 1].medicationAdherence / maxAdherence) * chartHeight}
                    x2={x}
                    y2={adherenceY}
                    stroke="#0ea5e9"
                    strokeWidth="3"
                  />
                </>
              )}

              {/* Date labels */}
              {index % Math.ceil(data.length / 5) === 0 && (
                <text
                  x={x}
                  y={height - 10}
                  textAnchor="middle"
                  fontSize="10"
                  fill="#6b7280"
                >
                  {new Date(point.date).toLocaleDateString('en-GB', { 
                    month: 'short', 
                    day: 'numeric' 
                  })}
                </text>
              )}
            </g>
          );
        })}

        {/* Axes */}
        <line
          x1={margin.left}
          y1={margin.top}
          x2={margin.left}
          y2={height - margin.bottom}
          stroke="#1f2937"
          strokeWidth="2"
        />
        <line
          x1={margin.left}
          y1={height - margin.bottom}
          x2={width - margin.right}
          y2={height - margin.bottom}
          stroke="#1f2937"
          strokeWidth="2"
        />

        {/* Axis labels */}
        <text
          x={25}
          y={height / 2}
          textAnchor="middle"
          fontSize="12"
          fontWeight="600"
          fill="#dc2626"
          transform={`rotate(-90, 25, ${height / 2})`}
        >
          Seizure Count
        </text>
        <text
          x={width - 25}
          y={height / 2}
          textAnchor="middle"
          fontSize="12"
          fontWeight="600"
          fill="#0ea5e9"
          transform={`rotate(90, ${width - 25}, ${height / 2})`}
        >
          Medication Adherence (%)
        </text>
      </svg>
    );
  };

  const renderCorrelationChart = () => {
    return (
      <svg width={width} height={height} style={{ border: '1px solid #e5e7eb', borderRadius: '8px' }}>
        {/* Background */}
        <rect width={width} height={height} fill="white" />
        
        {/* Grid lines */}
        {[0, 1, 2, 3, 4, 5].map(i => (
          <g key={`grid-${i}`}>
            <line
              x1={margin.left + (chartWidth / 5) * i}
              y1={margin.top}
              x2={margin.left + (chartWidth / 5) * i}
              y2={height - margin.bottom}
              stroke="#f3f4f6"
              strokeDasharray="2,2"
            />
            <line
              x1={margin.left}
              y1={margin.top + (chartHeight / 5) * i}
              x2={width - margin.right}
              y2={margin.top + (chartHeight / 5) * i}
              stroke="#f3f4f6"
              strokeDasharray="2,2"
            />
          </g>
        ))}

        {/* Data points */}
        {data.map((point, index) => {
          const x = margin.left + (point.medicationAdherence / 100) * chartWidth;
          const y = margin.top + chartHeight - (point.seizureCount / maxSeizures) * chartHeight;

          return (
            <circle
              key={`scatter-${index}`}
              cx={x}
              cy={y}
              r="6"
              fill="#0ea5e9"
              fillOpacity="0.8"
              stroke="#0284c7"
              strokeWidth="3"
            />
          );
        })}

        {/* Axes */}
        <line
          x1={margin.left}
          y1={margin.top}
          x2={margin.left}
          y2={height - margin.bottom}
          stroke="#1f2937"
          strokeWidth="2"
        />
        <line
          x1={margin.left}
          y1={height - margin.bottom}
          x2={width - margin.right}
          y2={height - margin.bottom}
          stroke="#1f2937"
          strokeWidth="2"
        />

        {/* Axis labels */}
        <text
          x={width / 2}
          y={height - 5}
          textAnchor="middle"
          fontSize="12"
          fill="#374151"
        >
          Medication Adherence (%)
        </text>
        <text
          x={20}
          y={height / 2}
          textAnchor="middle"
          fontSize="12"
          fill="#374151"
          transform={`rotate(-90, 20, ${height / 2})`}
        >
          Seizure Count
        </text>

        {/* Scale labels */}
        {[0, 25, 50, 75, 100].map((value, i) => (
          <text
            key={`x-label-${i}`}
            x={margin.left + (chartWidth / 4) * i}
            y={height - margin.bottom + 15}
            textAnchor="middle"
            fontSize="10"
            fill="#6b7280"
          >
            {value}
          </text>
        ))}
        {[0, 1, 2, 3, 4].map((value, i) => (
          <text
            key={`y-label-${i}`}
            x={margin.left - 10}
            y={margin.top + chartHeight - (chartHeight / 4) * i + 5}
            textAnchor="end"
            fontSize="10"
            fill="#6b7280"
          >
            {value}
          </text>
        ))}
      </svg>
    );
  };

  const renderDemographicCorrelationChart = () => {
    // Sort data by date for proper line connection
    const sortedData = [...data].sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
    const demographicLabel = data[0]?.demographicLabel || 'Demographic Factor';
    
    // Get demographic values and dynamically adjust scales
    const demographicValues = sortedData.map(d => d.demographicValue || 0);
    const episodeScores = sortedData.map(d => d.episodeFrequencyScore || 0);
    const seizureCounts = sortedData.map(d => d.seizureCount);
    const adherenceValues = sortedData.map(d => d.medicationAdherence);
    
    // Dynamic scaling based on actual patient data
    const minDemographic = Math.min(...demographicValues);
    const maxDemographic = Math.max(...demographicValues);
    const minEpisodeScore = Math.min(...episodeScores);
    const maxEpisodeScore = Math.max(...episodeScores, 1);
    const maxSeizures = Math.max(...seizureCounts, 1);
    
    // Add padding to ranges for better visualization
    const demographicRange = maxDemographic - minDemographic;
    const episodeRange = maxEpisodeScore - minEpisodeScore;
    const demographicPadding = Math.max(demographicRange * 0.1, 1);
    const episodePadding = Math.max(episodeRange * 0.1, 0.5);
    
    const scaledMinDemo = Math.max(0, minDemographic - demographicPadding);
    const scaledMaxDemo = maxDemographic + demographicPadding;
    const scaledMinEpisode = Math.max(0, minEpisodeScore - episodePadding);
    const scaledMaxEpisode = maxEpisodeScore + episodePadding;
    
    return (
    <div style={{ position: 'relative', display: 'inline-block' }}>
      <svg width={width} height={height} style={{ border: '1px solid #e5e7eb', borderRadius: '8px' }}>
        {/* Background */}
        <rect width={width} height={height} fill="white" />
        
        {/* Grid lines */}
        {[0, 1, 2, 3, 4, 5].map(i => (
          <g key={`grid-${i}`}>
            <line
              x1={margin.left}
              y1={margin.top + (chartHeight / 5) * i}
              x2={width - margin.right}
              y2={margin.top + (chartHeight / 5) * i}
              stroke="#f1f5f9"
              strokeWidth="1"
            />
          </g>
        ))}

        {/* Primary Line: Selected Demographic Factor */}
        {sortedData.map((point, index) => {
          const x = margin.left + (chartWidth / (sortedData.length - 1)) * index;
          const demographicY = margin.top + chartHeight - 
            (((point.demographicValue || 0) - scaledMinDemo) / (scaledMaxDemo - scaledMinDemo)) * chartHeight;
          
          return (
            <g key={`demographic-line-${index}`}>
              {/* Connect demographic line */}
              {index > 0 && (
                <line
                  x1={margin.left + (chartWidth / (sortedData.length - 1)) * (index - 1)}
                  y1={margin.top + chartHeight - 
                    (((sortedData[index - 1].demographicValue || 0) - scaledMinDemo) / (scaledMaxDemo - scaledMinDemo)) * chartHeight}
                  x2={x}
                  y2={demographicY}
                  stroke="#3b82f6"
                  strokeWidth="4"
                />
              )}
              
              {/* Demographic point */}
              <circle
                cx={x}
                cy={demographicY}
                r="6"
                fill="#3b82f6"
                stroke="white"
                strokeWidth="3"
              />
            </g>
          );
        })}

        {/* Secondary Line: Episode Frequency Score */}
        {sortedData.map((point, index) => {
          const x = margin.left + (chartWidth / (sortedData.length - 1)) * index;
          const episodeY = margin.top + chartHeight - 
            (((point.episodeFrequencyScore || 0) - scaledMinEpisode) / (scaledMaxEpisode - scaledMinEpisode)) * chartHeight;
          
          return (
            <g key={`episode-line-${index}`}>
              {/* Connect episode frequency line */}
              {index > 0 && (
                <line
                  x1={margin.left + (chartWidth / (sortedData.length - 1)) * (index - 1)}
                  y1={margin.top + chartHeight - 
                    (((sortedData[index - 1].episodeFrequencyScore || 0) - scaledMinEpisode) / (scaledMaxEpisode - scaledMinEpisode)) * chartHeight}
                  x2={x}
                  y2={episodeY}
                  stroke="#dc2626"
                  strokeWidth="4"
                />
              )}
              
              {/* Episode frequency point */}
              <circle
                cx={x}
                cy={episodeY}
                r="6"
                fill="#dc2626"
                stroke="white"
                strokeWidth="3"
              />
            </g>
          );
        })}

        {/* Tertiary Line: Medication Adherence (normalized) */}
        {sortedData.map((point, index) => {
          const x = margin.left + (chartWidth / (sortedData.length - 1)) * index;
          const adherenceY = margin.top + chartHeight - (point.medicationAdherence / 100) * chartHeight;
          
          return (
            <g key={`adherence-line-${index}`}>
              {/* Connect adherence line */}
              {index > 0 && (
                <line
                  x1={margin.left + (chartWidth / (sortedData.length - 1)) * (index - 1)}
                  y1={margin.top + chartHeight - (sortedData[index - 1].medicationAdherence / 100) * chartHeight}
                  x2={x}
                  y2={adherenceY}
                  stroke="#10b981"
                  strokeWidth="3"
                  strokeDasharray="6,3"
                />
              )}
              
              {/* Adherence point */}
              <circle
                cx={x}
                cy={adherenceY}
                r="4"
                fill="#10b981"
                stroke="white"
                strokeWidth="2"
              />
            </g>
          );
        })}

        {/* Seizure indicators as vertical bars */}
        {sortedData.map((point, index) => {
          if (point.seizureCount > 0) {
            const x = margin.left + (chartWidth / (sortedData.length - 1)) * index;
            const barHeight = (point.seizureCount / maxSeizures) * 30; // Max 30px height
            const barY = height - margin.bottom - barHeight;
            
            return (
              <g key={`seizure-indicator-${index}`}>
                <rect
                  x={x - 4}
                  y={barY}
                  width="8"
                  height={barHeight}
                  fill="#fbbf24"
                  stroke="#f59e0b"
                  strokeWidth="1"
                  rx="2"
                />
                <text
                  x={x}
                  y={barY - 3}
                  textAnchor="middle"
                  fontSize="9"
                  fontWeight="600"
                  fill="#92400e"
                >
                  {point.seizureCount}
                </text>
              </g>
            );
          }
          return null;
        })}


        {/* Axes */}
        <line
          x1={margin.left}
          y1={margin.top}
          x2={margin.left}
          y2={height - margin.bottom}
          stroke="#1f2937"
          strokeWidth="2"
        />
        <line
          x1={margin.left}
          y1={height - margin.bottom}
          x2={width - margin.right}
          y2={height - margin.bottom}
          stroke="#1f2937"
          strokeWidth="2"
        />

        {/* Y-axis labels (left - Demographic Values) */}
        {[0, 1, 2, 3, 4].map(i => {
          const value = scaledMinDemo + ((scaledMaxDemo - scaledMinDemo) / 4) * i;
          const displayValue = demographicLabel === 'BMI' ? value.toFixed(1) :
                              demographicLabel.includes('Age') ? Math.round(value) :
                              Math.round(value);
          return (
            <text
              key={`y-label-demo-${i}`}
              x={margin.left - 10}
              y={margin.top + chartHeight - (chartHeight / 4) * i + 5}
              textAnchor="end"
              fontSize="11"
              fontWeight="600"
              fill="#3b82f6"
            >
              {displayValue}
            </text>
          );
        })}

        {/* Y-axis labels (right - Episode Score) */}
        {[0, 1, 2, 3, 4].map(i => {
          const value = scaledMinEpisode + ((scaledMaxEpisode - scaledMinEpisode) / 4) * i;
          return (
            <text
              key={`y-label-episode-${i}`}
              x={width - margin.right + 15}
              y={margin.top + chartHeight - (chartHeight / 4) * i + 5}
              textAnchor="start"
              fontSize="11"
              fontWeight="600"
              fill="#dc2626"
            >
              {value.toFixed(1)}
            </text>
          );
        })}

        {/* X-axis labels (Date) */}
        {sortedData.map((point, index) => {
          if (index % Math.ceil(sortedData.length / 6) === 0) {
            const x = margin.left + (chartWidth / (sortedData.length - 1)) * index;
            return (
              <text
                key={`date-${index}`}
                x={x}
                y={height - 8}
                textAnchor="middle"
                fontSize="10"
                fill="#6b7280"
              >
                {new Date(point.date).toLocaleDateString('en-GB', { 
                  month: 'short', 
                  day: 'numeric' 
                })}
              </text>
            );
          }
          return null;
        })}


        {/* Axis labels */}
        <text
          x={25}
          y={height / 2}
          textAnchor="middle"
          fontSize="12"
          fontWeight="600"
          fill="#3b82f6"
          transform={`rotate(-90, 25, ${height / 2})`}
        >
          {demographicLabel}
        </text>
        <text
          x={width - 25}
          y={height / 2}
          textAnchor="middle"
          fontSize="12"
          fontWeight="600"
          fill="#dc2626"
          transform={`rotate(90, ${width - 25}, ${height / 2})`}
        >
          Episode Frequency Score
        </text>
        <text
          x={width / 2}
          y={height - 5}
          textAnchor="middle"
          fontSize="12"
          fontWeight="600"
          fill="#6b7280"
        >
          Time Period
        </text>

      </svg>
      
      {/* Legend positioned completely outside the chart */}
      <div style={{
        position: 'absolute',
        right: '-220px',
        top: '20px',
        width: '200px',
        backgroundColor: 'white',
        border: '2px solid #d1d5db',
        borderRadius: '8px',
        padding: '15px',
        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
      }}>
        <div style={{ fontSize: '12px', fontWeight: '700', color: '#1f2937', marginBottom: '12px' }}>Legend</div>
        
        {/* Primary line - Demographic */}
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '8px' }}>
          <div style={{
            width: '20px',
            height: '4px',
            backgroundColor: '#3b82f6',
            marginRight: '8px',
            borderRadius: '2px'
          }} />
          <span style={{ fontSize: '10px', fontWeight: '600', color: '#3b82f6' }}>{demographicLabel}</span>
        </div>
        
        {/* Secondary line - Episodes */}
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: '8px' }}>
          <div style={{
            width: '20px',
            height: '4px',
            backgroundColor: '#dc2626',
            marginRight: '8px',
            borderRadius: '2px'
          }} />
          <span style={{ fontSize: '10px', fontWeight: '600', color: '#dc2626' }}>Episode Frequency</span>
        </div>
        
        {/* Tertiary line - Adherence */}
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <div style={{
            width: '20px',
            height: '3px',
            backgroundColor: '#10b981',
            marginRight: '8px',
            borderRadius: '2px',
            background: 'repeating-linear-gradient(90deg, #10b981 0, #10b981 6px, transparent 6px, transparent 9px)'
          }} />
          <span style={{ fontSize: '10px', fontWeight: '600', color: '#10b981' }}>Medication Adherence</span>
        </div>
      </div>
    </div>
  );
};

const renderAdherenceChart = () => {
    return (
      <svg width={width} height={height} style={{ border: '1px solid #e5e7eb', borderRadius: '8px' }}>
        {/* Background */}
        <rect width={width} height={height} fill="white" />
        
        {/* Grid lines */}
        {[0, 1, 2, 3, 4, 5].map(i => (
          <line
            key={`grid-${i}`}
            x1={margin.left}
            y1={margin.top + (chartHeight / 5) * i}
            x2={width - margin.right}
            y2={margin.top + (chartHeight / 5) * i}
            stroke="#f3f4f6"
            strokeDasharray="2,2"
          />
        ))}

        {/* Reference lines */}
        <line
          x1={margin.left}
          y1={margin.top + chartHeight - (80 / 100) * chartHeight}
          x2={width - margin.right}
          y2={margin.top + chartHeight - (80 / 100) * chartHeight}
          stroke="#10b981"
          strokeDasharray="8,4"
          strokeWidth="3"
        />
        <line
          x1={margin.left}
          y1={margin.top + chartHeight - (60 / 100) * chartHeight}
          x2={width - margin.right}
          y2={margin.top + chartHeight - (60 / 100) * chartHeight}
          stroke="#f59e0b"
          strokeDasharray="8,4"
          strokeWidth="3"
        />

        {/* Data line and points */}
        {data.map((point, index) => {
          const x = margin.left + (chartWidth / (data.length - 1)) * index;
          const y = margin.top + chartHeight - (point.medicationAdherence / 100) * chartHeight;

          return (
            <g key={`adherence-${index}`}>
              <circle
                cx={x}
                cy={y}
                r="5"
                fill="#0ea5e9"
                stroke="white"
                strokeWidth="3"
              />

              {/* Connect lines */}
              {index > 0 && (
                <line
                  x1={margin.left + (chartWidth / (data.length - 1)) * (index - 1)}
                  y1={margin.top + chartHeight - (data[index - 1].medicationAdherence / 100) * chartHeight}
                  x2={x}
                  y2={y}
                  stroke="#0ea5e9"
                  strokeWidth="4"
                />
              )}
            </g>
          );
        })}

        {/* Axes */}
        <line
          x1={margin.left}
          y1={margin.top}
          x2={margin.left}
          y2={height - margin.bottom}
          stroke="#1f2937"
          strokeWidth="2"
        />
        <line
          x1={margin.left}
          y1={height - margin.bottom}
          x2={width - margin.right}
          y2={height - margin.bottom}
          stroke="#1f2937"
          strokeWidth="2"
        />

        {/* Labels */}
        <text
          x={20}
          y={height / 2}
          textAnchor="middle"
          fontSize="12"
          fill="#374151"
          transform={`rotate(-90, 20, ${height / 2})`}
        >
          Adherence (%)
        </text>

        {/* Reference line labels */}
        <text
          x={width - margin.right + 5}
          y={margin.top + chartHeight - (80 / 100) * chartHeight + 5}
          fontSize="11"
          fontWeight="600"
          fill="#10b981"
        >
          🎯 Target 80%
        </text>
        <text
          x={width - margin.right + 5}
          y={margin.top + chartHeight - (60 / 100) * chartHeight + 5}
          fontSize="11"
          fontWeight="600"
          fill="#f59e0b"
        >
          ⚠️ Warning 60%
        </text>
      </svg>
    );
  };

  switch (type) {
    case 'demographic-correlation':
      return renderDemographicCorrelationChart();
    case 'timeline':
      return renderTimelineChart();
    case 'correlation':
      return renderCorrelationChart();
    case 'adherence':
      return renderAdherenceChart();
    default:
      return renderTimelineChart();
  }
};