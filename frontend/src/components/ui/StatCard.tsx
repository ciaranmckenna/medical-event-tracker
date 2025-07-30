import { ReactNode } from 'react';
import { Card } from './Card';

interface StatCardProps {
  title: string;
  value: string | number;
  icon?: string;
  subtitle?: string;
  trend?: {
    value: number;
    direction: 'up' | 'down' | 'neutral';
  };
  color?: 'primary' | 'success' | 'warning' | 'danger' | 'info';
  onClick?: () => void;
}

export const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  icon,
  subtitle,
  trend,
  color = 'primary',
  onClick,
}) => {
  const colorMap = {
    primary: '#007bff',
    success: '#28a745',
    warning: '#ffc107',
    danger: '#dc3545',
    info: '#17a2b8',
  };

  const cardStyle: React.CSSProperties = {
    cursor: onClick ? 'pointer' : 'default',
    borderLeft: `4px solid ${colorMap[color]}`,
    transition: 'transform 0.2s ease, box-shadow 0.2s ease',
  };

  const hoverStyle: React.CSSProperties = onClick ? {
    transform: 'translateY(-2px)',
    boxShadow: '0 4px 8px rgba(0, 0, 0, 0.15)',
  } : {};

  const headerStyle: React.CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: '12px',
  };

  const titleStyle: React.CSSProperties = {
    fontSize: '14px',
    fontWeight: '500',
    color: '#666',
    margin: 0,
  };

  const valueStyle: React.CSSProperties = {
    fontSize: '32px',
    fontWeight: 'bold',
    color: '#333',
    margin: '8px 0',
  };

  const subtitleStyle: React.CSSProperties = {
    fontSize: '12px',
    color: '#888',
    margin: 0,
  };

  const trendStyle: React.CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    fontSize: '12px',
    fontWeight: '500',
    color: trend?.direction === 'up' ? '#28a745' : 
           trend?.direction === 'down' ? '#dc3545' : '#6c757d',
  };

  const getTrendIcon = () => {
    if (!trend) return null;
    switch (trend.direction) {
      case 'up': return '↗️';
      case 'down': return '↘️';
      default: return '➡️';
    }
  };

  return (
    <Card
      style={{
        ...cardStyle,
        ...(onClick ? hoverStyle : {}),
      }}
      onClick={onClick}
      padding="16px"
    >
      <div style={headerStyle}>
        <h4 style={titleStyle}>{title}</h4>
        {icon && <span style={{ fontSize: '24px' }}>{icon}</span>}
      </div>
      
      <div style={valueStyle}>{value}</div>
      
      {subtitle && <p style={subtitleStyle}>{subtitle}</p>}
      
      {trend && (
        <div style={trendStyle}>
          <span style={{ marginRight: '4px' }}>{getTrendIcon()}</span>
          {Math.abs(trend.value)}% from last month
        </div>
      )}
    </Card>
  );
};