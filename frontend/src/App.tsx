import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Navbar, Sidebar, MobileTabBar } from './components/layout/Navigation';
import { AiAssistantDrawer } from './components/ai/AiAssistantDrawer';

// Pages
import { AuthPage } from './pages/AuthPage';
import { DashboardPage } from './pages/DashboardPage';
import { EprCalculatorPage } from './pages/EprCalculatorPage';
import { RecyclerFinderPage } from './pages/RecyclerFinderPage';
import { ComplianceLogPage } from './pages/ComplianceLogPage';
import { ReportsPage } from './pages/ReportsPage';
import { AlertSettingsPage } from './pages/AlertSettingsPage';
import { CompanyProfilePage } from './pages/CompanyProfilePage';
import { EcSimulatorPage } from './pages/EcSimulatorPage';
import { MarketplacePage } from './pages/MarketplacePage';
import { DataIngestionPage } from './pages/DataIngestionPage';

const ProtectedLayout: React.FC = () => {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="min-h-screen bg-bg flex flex-col text-text font-sans">
      <Navbar />
      <div className="flex flex-1 overflow-hidden">
        <Sidebar />
        <main className="flex-1 overflow-y-auto pb-16 md:pb-6">
          <Routes>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/ec-simulator" element={<EcSimulatorPage />} />
            <Route path="/marketplace" element={<MarketplacePage />} />
            <Route path="/data-ingestion" element={<DataIngestionPage />} />
            <Route path="/calculator" element={<EprCalculatorPage />} />
            <Route path="/recyclers" element={<RecyclerFinderPage />} />
            <Route path="/compliance-log" element={<ComplianceLogPage />} />
            <Route path="/reports" element={<ReportsPage />} />
            <Route path="/alerts" element={<AlertSettingsPage />} />
            <Route path="/profile" element={<CompanyProfilePage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </div>
      <MobileTabBar />
      <AiAssistantDrawer />
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<AuthPage />} />
          <Route path="/*" element={<ProtectedLayout />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
