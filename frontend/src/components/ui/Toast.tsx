import React from 'react';

interface ToastProps {
  message: string;
  type: 'success' | 'error' | 'info';
  onClose: () => void;
}

export const Toast: React.FC<ToastProps> = ({ message, type, onClose }) => {
  const borderColors = {
    success: 'border-green text-green',
    error: 'border-red text-red',
    info: 'border-blue text-blue',
  };

  return (
    <div
      className={`fixed top-4 right-4 z-50 flex items-center gap-3 px-4 py-3 bg-surface border ${borderColors[type]} rounded-card shadow-lg text-sm max-w-sm animate-slideIn`}
    >
      <span className="flex-1 text-text">{message}</span>
      <button
        onClick={onClose}
        className="text-text-muted hover:text-text transition-colors text-base leading-none"
      >
        &times;
      </button>
    </div>
  );
};
