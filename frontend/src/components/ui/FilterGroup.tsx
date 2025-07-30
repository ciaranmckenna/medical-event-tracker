import { ReactNode } from 'react';

interface FilterOption {
  label: string;
  value: string;
  count?: number;
}

interface FilterGroupProps {
  title: string;
  options: FilterOption[];
  selectedValue?: string;
  onSelectionChange: (value: string) => void;
  showCounts?: boolean;
  multiSelect?: boolean;
  selectedValues?: string[];
  onMultiSelectionChange?: (values: string[]) => void;
}

export const FilterGroup: React.FC<FilterGroupProps> = ({
  title,
  options,
  selectedValue,
  onSelectionChange,
  showCounts = false,
  multiSelect = false,
  selectedValues = [],
  onMultiSelectionChange,
}) => {
  const containerStyle: React.CSSProperties = {
    marginBottom: '20px',
  };

  const titleStyle: React.CSSProperties = {
    fontSize: '14px',
    fontWeight: '600',
    color: '#333',
    marginBottom: '8px',
  };

  const optionsStyle: React.CSSProperties = {
    display: 'flex',
    flexWrap: 'wrap',
    gap: '8px',
  };

  const optionStyle = (isSelected: boolean): React.CSSProperties => ({
    padding: '6px 12px',
    border: '1px solid #ddd',
    borderRadius: '20px',
    fontSize: '12px',
    cursor: 'pointer',
    backgroundColor: isSelected ? '#007bff' : '#ffffff',
    color: isSelected ? '#ffffff' : '#666',
    transition: 'all 0.2s ease',
    display: 'flex',
    alignItems: 'center',
    gap: '4px',
  });

  const handleOptionClick = (value: string) => {
    if (multiSelect && onMultiSelectionChange) {
      const newValues = selectedValues.includes(value)
        ? selectedValues.filter(v => v !== value)
        : [...selectedValues, value];
      onMultiSelectionChange(newValues);
    } else {
      onSelectionChange(value === selectedValue ? '' : value);
    }
  };

  const isSelected = (value: string) => {
    if (multiSelect) {
      return selectedValues.includes(value);
    }
    return selectedValue === value;
  };

  return (
    <div style={containerStyle}>
      <div style={titleStyle}>{title}</div>
      <div style={optionsStyle}>
        {options.map((option) => (
          <button
            key={option.value}
            style={optionStyle(isSelected(option.value))}
            onClick={() => handleOptionClick(option.value)}
          >
            <span>{option.label}</span>
            {showCounts && option.count !== undefined && (
              <span style={{ 
                backgroundColor: 'rgba(255, 255, 255, 0.2)', 
                borderRadius: '10px', 
                padding: '2px 6px',
                fontSize: '10px',
                fontWeight: 'bold',
              }}>
                {option.count}
              </span>
            )}
          </button>
        ))}
      </div>
    </div>
  );
};