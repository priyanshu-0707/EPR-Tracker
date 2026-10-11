import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export const Navbar: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <header className="h-16 border-b border-border bg-surface px-4 md:px-6 flex items-center justify-between sticky top-0 z-30">
      <div className="flex items-center gap-3">
        <div className="w-8 h-8 rounded-card bg-blue/15 border border-blue/40 flex items-center justify-center text-blue font-mono font-bold text-sm">
          EPR
        </div>
        <div>
          <h1 className="text-sm font-semibold text-text tracking-tight flex items-center gap-2">
            EPR Compliance Tracker
            <span className="text-[10px] font-mono uppercase bg-surface-2 border border-border px-1.5 py-0.5 rounded text-text-muted">
              CPCB 2022
            </span>
          </h1>
          <p className="text-[11px] text-text-muted hidden sm:block">
            ClearTax for E-Waste Legal Obligations
          </p>
        </div>
      </div>

      <div className="flex items-center gap-4">
        {user && (
          <div className="text-right hidden sm:block">
            <span className="text-xs font-medium text-text block truncate max-w-[200px]">
              {user.companyName}
            </span>
            <span className="text-[10px] font-mono text-text-dim block">
              {user.state} {user.cpcbRegistrationNumber ? `• ${user.cpcbRegistrationNumber}` : ''}
            </span>
          </div>
        )}

        <button
          onClick={logout}
          className="text-xs text-text-muted hover:text-red border border-border hover:border-red/40 px-3 py-1.5 rounded-input transition-colors font-mono"
        >
          Logout
        </button>
      </div>
    </header>
  );
};

export const Sidebar: React.FC = () => {
  const navItems = [
    { name: 'Dashboard', path: '/', icon: '📊' },
    { name: 'EC Penalty Simulator', path: '/ec-simulator', icon: '⚠️' },
    { name: 'Credit Marketplace', path: '/marketplace', icon: '🛒' },
    { name: 'ERP Data Ingestion', path: '/data-ingestion', icon: '⚡' },
    { name: 'EPR Calculator', path: '/calculator', icon: '🧮' },
    { name: 'Recycler Finder', path: '/recyclers', icon: '📍' },
    { name: 'Compliance Log', path: '/compliance-log', icon: '📋' },
    { name: 'CPCB Form 2 & Reports', path: '/reports', icon: '📄' },
    { name: 'Alert Settings', path: '/alerts', icon: '🔔' },
    { name: 'Company Profile', path: '/profile', icon: '🏢' },
  ];

  return (
    <aside className="w-64 border-r border-border bg-surface flex flex-col hidden md:flex shrink-0">
      <nav className="p-3 flex flex-col gap-1 flex-1">
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            end={item.path === '/'}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2 rounded-input text-xs font-medium transition-colors ${
                isActive
                  ? 'bg-blue/15 text-blue border border-blue/30 font-semibold'
                  : 'text-text-muted hover:text-text hover:bg-surface-2'
              }`
            }
          >
            <span className="text-sm">{item.icon}</span>
            <span>{item.name}</span>
          </NavLink>
        ))}
      </nav>

      <div className="p-3 border-t border-border bg-surface-2/40">
        <div className="p-2.5 rounded-card bg-surface border border-border">
          <p className="text-[11px] font-mono text-text-muted uppercase">FY 2026-27 Target</p>
          <p className="text-xs font-bold text-text mt-0.5">80% Collection Quota</p>
          <p className="text-[10px] text-text-dim mt-1">Rule 4(1) Sched. III</p>
        </div>
      </div>
    </aside>
  );
};

export const MobileTabBar: React.FC = () => {
  const mobileNav = [
    { name: 'Dashboard', path: '/', icon: '📊' },
    { name: 'Simulator', path: '/ec-simulator', icon: '⚠️' },
    { name: 'Market', path: '/marketplace', icon: '🛒' },
    { name: 'Ingest', path: '/data-ingestion', icon: '⚡' },
    { name: 'Logs', path: '/compliance-log', icon: '📋' },
    { name: 'Reports', path: '/reports', icon: '📄' },
  ];

  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 h-14 bg-surface border-t border-border flex items-center justify-around z-30 px-1">
      {mobileNav.map((item) => (
        <NavLink
          key={item.path}
          to={item.path}
          end={item.path === '/'}
          className={({ isActive }) =>
            `flex flex-col items-center justify-center flex-1 py-1 text-[10px] font-medium transition-colors ${
              isActive ? 'text-blue' : 'text-text-muted'
            }`
          }
        >
          <span className="text-base">{item.icon}</span>
          <span>{item.name}</span>
        </NavLink>
      ))}
    </nav>
  );
};
