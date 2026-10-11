import React, { InputHTMLAttributes } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
}

export const Input: React.FC<InputProps> = ({
  label,
  error,
  helperText,
  className = '',
  id,
  ...props
}) => {
  const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

  return (
    <div className="w-full flex flex-col gap-1.5">
      {label && (
        <label htmlFor={inputId} className="text-xs font-medium text-text-muted">
          {label}
        </label>
      )}
      <input
        id={inputId}
        className={`w-full bg-surface-2 border ${
          error ? 'border-red' : 'border-border'
        } rounded-input px-3 py-2 text-sm text-text placeholder-text-dim focus:outline-none focus:border-blue focus:ring-1 focus:ring-blue transition-colors disabled:opacity-50 disabled:bg-surface ${className}`}
        {...props}
      />
      {error && <span className="text-xs text-red font-mono">{error}</span>}
      {helperText && !error && <span className="text-xs text-text-dim">{helperText}</span>}
    </div>
  );
};
