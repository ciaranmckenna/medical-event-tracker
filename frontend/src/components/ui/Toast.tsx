import React, { useState, useEffect } from 'react';

export interface ToastProps {
  id: string;
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  duration?: number;
  onClose: (id: string) => void;
}

export const Toast: React.FC<ToastProps> = ({
  id,
  message,
  type,
  duration = 4000,
  onClose
}) => {
  const [isVisible, setIsVisible] = useState(true);
  const [isExiting, setIsExiting] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsExiting(true);
      setTimeout(() => {
        setIsVisible(false);
        onClose(id);
      }, 300); // Animation duration
    }, duration);

    return () => clearTimeout(timer);
  }, [id, duration, onClose]);

  if (!isVisible) return null;

  const getToastStyle = () => {
    const baseStyle = {
      position: 'relative' as const,
      minWidth: '300px',
      maxWidth: '500px',
      padding: '16px 20px',
      borderRadius: '8px',
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
      display: 'flex',
      alignItems: 'center',
      gap: '12px',
      fontSize: '14px',
      fontWeight: '500',
      transform: isExiting ? 'translateX(100%)' : 'translateX(0)',
      opacity: isExiting ? 0 : 1,
      transition: 'all 0.3s ease-in-out',
      cursor: 'pointer'
    };

    const typeStyles = {
      success: {
        backgroundColor: '#10b981',
        color: 'white',
        border: '1px solid #059669'
      },
      error: {
        backgroundColor: '#dc2626',
        color: 'white',
        border: '1px solid #b91c1c'
      },
      warning: {
        backgroundColor: '#f59e0b',
        color: 'white',
        border: '1px solid #d97706'
      },
      info: {
        backgroundColor: '#3b82f6',
        color: 'white',
        border: '1px solid #2563eb'
      }
    };

    return { ...baseStyle, ...typeStyles[type] };
  };

  const getIcon = () => {
    const icons = {
      success: '✅',
      error: '❌',
      warning: '⚠️',
      info: 'ℹ️'
    };
    return icons[type];
  };

  const handleClick = () => {
    setIsExiting(true);
    setTimeout(() => {
      setIsVisible(false);
      onClose(id);
    }, 300);
  };

  return (
    <div style={getToastStyle()} onClick={handleClick}>
      <span style={{ fontSize: '16px' }}>{getIcon()}</span>
      <span style={{ flex: 1 }}>{message}</span>
      <button
        onClick={handleClick}
        style={{
          background: 'none',
          border: 'none',
          color: 'inherit',
          fontSize: '18px',
          cursor: 'pointer',
          padding: '0',
          lineHeight: '1'
        }}
      >
        ×
      </button>
    </div>
  );
};