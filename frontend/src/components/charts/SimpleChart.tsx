import React from 'react';

interface ChartDataPoint {
  date: string;
  seizureCount: number;
  medicationAdherence: number;
}

interface SimpleChartProps {
  data: ChartDataPoint[];
  width?: number;
  height?: number;
  type: 'timeline' | 'correlation' | 'adherence';
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
            fontSize="12"
            fill="#6b7280"
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
            fontSize="12"
            fill="#2563eb"
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
                r="4"
                fill="#dc2626"
                stroke="white"
                strokeWidth="2"
              />
              
              {/* Adherence point */}
              <circle
                cx={x}
                cy={adherenceY}
                r="3"
                fill="#2563eb"
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
                    strokeWidth="2"
                  />
                  <line
                    x1={margin.left + (chartWidth / (data.length - 1)) * (index - 1)}
                    y1={margin.top + chartHeight - (data[index - 1].medicationAdherence / maxAdherence) * chartHeight}
                    x2={x}
                    y2={adherenceY}
                    stroke="#2563eb"
                    strokeWidth="2"
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
          stroke="#374151"
          strokeWidth="1"
        />
        <line
          x1={margin.left}
          y1={height - margin.bottom}
          x2={width - margin.right}
          y2={height - margin.bottom}
          stroke="#374151"
          strokeWidth="1"
        />

        {/* Axis labels */}
        <text
          x={20}
          y={height / 2}
          textAnchor="middle"
          fontSize="12"
          fill="#dc2626"
          transform={`rotate(-90, 20, ${height / 2})`}
        >
          Seizures
        </text>
        <text
          x={width - 20}
          y={height / 2}
          textAnchor="middle"
          fontSize="12"
          fill="#2563eb"
          transform={`rotate(90, ${width - 20}, ${height / 2})`}
        >
          Adherence (%)
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
              r="4"
              fill="#2563eb"
              fillOpacity="0.7"
              stroke="#1d4ed8"
              strokeWidth="1"
            />
          );
        })}

        {/* Axes */}
        <line
          x1={margin.left}
          y1={margin.top}
          x2={margin.left}
          y2={height - margin.bottom}
          stroke="#374151"
          strokeWidth="1"
        />
        <line
          x1={margin.left}
          y1={height - margin.bottom}
          x2={width - margin.right}
          y2={height - margin.bottom}
          stroke="#374151"
          strokeWidth="1"
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
          strokeDasharray="5,5"
          strokeWidth="2"
        />
        <line
          x1={margin.left}
          y1={margin.top + chartHeight - (60 / 100) * chartHeight}
          x2={width - margin.right}
          y2={margin.top + chartHeight - (60 / 100) * chartHeight}
          stroke="#f59e0b"
          strokeDasharray="5,5"
          strokeWidth="2"
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
                r="3"
                fill="#2563eb"
                stroke="white"
                strokeWidth="2"
              />

              {/* Connect lines */}
              {index > 0 && (
                <line
                  x1={margin.left + (chartWidth / (data.length - 1)) * (index - 1)}
                  y1={margin.top + chartHeight - (data[index - 1].medicationAdherence / 100) * chartHeight}
                  x2={x}
                  y2={y}
                  stroke="#2563eb"
                  strokeWidth="2"
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
          stroke="#374151"
          strokeWidth="1"
        />
        <line
          x1={margin.left}
          y1={height - margin.bottom}
          x2={width - margin.right}
          y2={height - margin.bottom}
          stroke="#374151"
          strokeWidth="1"
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
          fontSize="10"
          fill="#10b981"
        >
          Target 80%
        </text>
        <text
          x={width - margin.right + 5}
          y={margin.top + chartHeight - (60 / 100) * chartHeight + 5}
          fontSize="10"
          fill="#f59e0b"
        >
          Warning 60%
        </text>
      </svg>
    );
  };

  switch (type) {
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