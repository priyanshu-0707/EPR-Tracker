import React from 'react';

interface BadgeProps {
  status: 'ON TRACK' | 'AT RISK' | 'NON-COMPLIANT' | string;
  size?: 'sm' | 'md' | 'lg';
}

export const Badge: React.FC<BadgeProps> = ({ status, size = 'md' }) => {
  const norm = status.toUpperCase().trim();

  let colorClass = 'bg-surface-2 text-text-muted border-border';
  let dotColor = 'bg-text-muted';

  if (norm === 'ON TRACK') {
    colorClass = 'bg-green/15 text-green border-green/30';
    dotColor = 'bg-green';
  } else if (norm === 'AT RISK') {
    colorClass = 'bg-amber/15 text-amber border-amber/30';
    dotColor = 'bg-amber';
  } else if (norm === 'NON-COMPLIANT') {
    colorClass = 'bg-red/15 text-red border-red/30';
    dotColor = 'bg-red';
  }

  const sizes = {
    sm: 'text-xs px-2 py-0.5',
    md: 'text-xs px-2.5 py-1',
    lg: 'text-sm px-3 py-1.5',
  };

  return (
    <span
      className={`inline-flex items-center gap-1.5 font-mono font-medium rounded-badge border ${colorClass} ${sizes[size]}`}
    >
      <span className={`w-1.5 h-1.5 rounded-full ${dotColor} animate-pulse`} />
      {norm}
    </span>
  );
};
