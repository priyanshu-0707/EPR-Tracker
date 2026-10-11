import React, { ButtonHTMLAttributes, ReactNode } from 'react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  children: ReactNode;
  isLoading?: boolean;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  children,
  isLoading = false,
  className = '',
  disabled,
  ...props
}) => {
  const base = 'inline-flex items-center justify-center font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-blue/50 disabled:opacity-50 disabled:cursor-not-allowed';

  const variants = {
    primary: 'bg-blue text-bg font-semibold hover:bg-blue/90',
    secondary: 'bg-surface-2 border border-border text-text hover:bg-surface-2/80 hover:border-text-dim',
    danger: 'bg-red/20 text-red border border-red/40 hover:bg-red/30',
    ghost: 'text-text-muted hover:text-text hover:bg-surface-2',
  };

  const sizes = {
    sm: 'text-xs px-2.5 py-1.5 rounded-input',
    md: 'text-sm px-4 py-2 rounded-input',
    lg: 'text-base px-6 py-2.5 rounded-input',
  };

  return (
    <button
      className={`${base} ${variants[variant]} ${sizes[size]} ${className}`}
      disabled={disabled || isLoading}
      {...props}
    >
      {isLoading && (
        <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-current" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
      )}
      {children}
    </button>
  );
};
