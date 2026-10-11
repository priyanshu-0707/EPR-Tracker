import React, { ReactNode } from 'react';
import { Button } from './Button';

interface EmptyStateProps {
  icon?: ReactNode;
  title: string;
  description: string;
  actionText?: string;
  onAction?: () => void;
}

export const EmptyState: React.FC<EmptyStateProps> = ({
  icon,
  title,
  description,
  actionText,
  onAction,
}) => {
  return (
    <div className="flex flex-col items-center justify-center p-8 text-center bg-surface border border-border rounded-card my-4">
      {icon && <div className="text-text-muted mb-3">{icon}</div>}
      <h3 className="text-base font-semibold text-text mb-1">{title}</h3>
      <p className="text-sm text-text-muted max-w-sm mb-4">{description}</p>
      {actionText && onAction && (
        <Button variant="primary" size="sm" onClick={onAction}>
          {actionText}
        </Button>
      )}
    </div>
  );
};
