import { ReactNode } from 'react';

interface CardProps {
  children: ReactNode;
  title?: string;
  className?: string;
  style?: React.CSSProperties;
  padding?: string;
  backgroundColor?: string;
  border?: string;
  borderRadius?: string;
}

export const Card: React.FC<CardProps> = ({
  children,
  title,
  className = '',
  style = {},
  padding = '20px',
  backgroundColor = '#ffffff',
  border = '1px solid #e0e0e0',
  borderRadius = '8px',
}) => {
  const cardStyle: React.CSSProperties = {
    backgroundColor,
    border,
    borderRadius,
    padding,
    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
    transition: 'box-shadow 0.2s ease',
    ...style,
  };

  const titleStyle: React.CSSProperties = {
    margin: '0 0 16px 0',
    fontSize: '18px',
    fontWeight: '600',
    color: '#333',
    borderBottom: '1px solid #f0f0f0',
    paddingBottom: '8px',
  };

  return (
    <div className={`card ${className}`} style={cardStyle}>
      {title && <h3 style={titleStyle}>{title}</h3>}
      {children}
    </div>
  );
};