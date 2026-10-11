import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { dashboardService } from '../services';
import { DashboardData } from '../types';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { Skeleton } from '../components/ui/Skeleton';
import { EmptyState } from '../components/ui/EmptyState';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';

export const DashboardPage: React.FC = () => {
  const [data, setData] = useState<DashboardData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      setIsLoading(true);
      const res = await dashboardService.getDashboard();
      setData(res);
    } catch {
      // Handled via empty state or retry
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="p-4 md:p-6 flex flex-col gap-6">
        <div className="flex flex-col gap-2">
          <Skeleton className="h-7 w-48" />
          <Skeleton className="h-4 w-72" />
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <Skeleton className="h-28 rounded-card" />
          <Skeleton className="h-28 rounded-card" />
          <Skeleton className="h-28 rounded-card" />
          <Skeleton className="h-28 rounded-card" />
        </div>
        <Skeleton className="h-64 rounded-card" />
      </div>
    );
  }

  if (!data) {
    return (
      <div className="p-4 md:p-6">
        <EmptyState
          title="Compliance Records Not Available"
          description="Unable to retrieve active compliance status. Please ensure your enterprise profile is initialized."
          actionText="Recalculate Status"
          onAction={fetchDashboard}
        />
      </div>
    );
  }

  return (
    <div className="p-4 md:p-6 flex flex-col gap-6">
      {/* Title & Quick Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h2 className="text-xl font-bold text-text">{data.companyName}</h2>
            <Badge status={data.complianceStatus} size="lg" />
          </div>
          <p className="text-xs text-text-muted mt-1 font-mono">
            CPCB Registration: {data.cpcbRegistrationNumber || 'Pending'} • State: {data.state} • FY {data.financialYear}
          </p>
        </div>

        <div className="flex flex-wrap gap-2">
          <Button variant="secondary" size="sm" onClick={() => navigate('/compliance-log')}>
            + Log Collection
          </Button>
          <Button variant="secondary" size="sm" onClick={() => navigate('/recyclers')}>
            📍 Find Recycler
          </Button>
          <Button variant="primary" size="sm" onClick={() => navigate('/reports')}>
            📄 Generate Report
          </Button>
        </div>
      </div>

      {/* 4 Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Card 1: Total Obligation */}
        <div className="p-4 bg-surface border border-border rounded-card flex flex-col justify-between">
          <div>
            <span className="text-xs font-mono uppercase text-text-muted">Total EPR Obligation</span>
            <div className="text-2xl font-bold font-mono text-text mt-2">
              {data.formattedTotalObligationUnits} <span className="text-xs text-text-dim font-sans font-normal">units</span>
            </div>
          </div>
          <p className="text-[11px] text-text-dim mt-3 font-mono">Qp × Cf × 80% (CPCB 2026-27)</p>
        </div>

        {/* Card 2: Collected So Far */}
        <div className="p-4 bg-surface border border-border rounded-card flex flex-col justify-between">
          <div>
            <span className="text-xs font-mono uppercase text-text-muted">Collected So Far</span>
            <div className="text-2xl font-bold font-mono text-green mt-2">
              {data.formattedTotalCollectedUnits} <span className="text-xs text-text-dim font-sans font-normal">kg / units</span>
            </div>
          </div>
          <div className="mt-3">
            <div className="w-full bg-surface-2 h-2 rounded-full overflow-hidden border border-border/40">
              <div
                className={`h-full transition-all duration-500 ${
                  data.fulfillmentPercentage >= 70
                    ? 'bg-green'
                    : data.fulfillmentPercentage >= 40
                    ? 'bg-amber'
                    : 'bg-red'
                }`}
                style={{ width: `${Math.min(100, data.fulfillmentPercentage)}%` }}
              />
            </div>
            <span className="text-[10px] font-mono text-text-muted mt-1 block">
              {data.fulfillmentPercentage}% quota fulfilled
            </span>
          </div>
        </div>

        {/* Card 3: Outstanding */}
        <div className="p-4 bg-surface border border-border rounded-card flex flex-col justify-between">
          <div>
            <span className="text-xs font-mono uppercase text-text-muted">Outstanding Target</span>
            <div className="text-2xl font-bold font-mono text-amber mt-2">
              {data.formattedOutstandingUnits} <span className="text-xs text-text-dim font-sans font-normal">units</span>
            </div>
          </div>
          <p className="text-[11px] text-text-dim mt-3 font-mono">Required before March 31, 2027</p>
        </div>

        {/* Card 4: Days to Year-End */}
        <div className="p-4 bg-surface border border-border rounded-card flex flex-col justify-between">
          <div>
            <span className="text-xs font-mono uppercase text-text-muted">Days to Year-End</span>
            <div className="text-2xl font-bold font-mono text-blue mt-2">
              {data.daysToYearEnd} <span className="text-xs text-text-dim font-sans font-normal">days left</span>
            </div>
          </div>
          <p className="text-[11px] text-text-dim mt-3 font-mono">Next quarterly return in 18 days</p>
        </div>
      </div>

      {/* Monthly Bar Chart */}
      <div className="p-4 md:p-6 bg-surface border border-border rounded-card">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-sm font-semibold text-text">
            Monthly Collection Trajectory vs Obligation Target
          </h3>
          <span className="text-xs font-mono text-text-muted">Financial Year 2026-27</span>
        </div>

        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data.monthlyCollections}>
              <XAxis dataKey="month" stroke="#8B949E" fontSize={11} tickLine={false} />
              <YAxis
                stroke="#8B949E"
                fontSize={11}
                tickLine={false}
                tickFormatter={(val) => (val >= 1000 ? `${val / 1000}k` : val)}
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#161B22',
                  borderColor: '#30363D',
                  color: '#E6EDF3',
                  borderRadius: '6px',
                  fontFamily: 'IBM Plex Mono',
                  fontSize: '12px',
                }}
              />
              <Legend wrapperStyle={{ fontSize: '12px', paddingTop: '10px' }} />
              <Bar dataKey="collectedUnits" name="Collected (Units)" fill="#3FB950" radius={[4, 4, 0, 0]} />
              <Bar dataKey="targetUnits" name="Monthly Target" fill="#30363D" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Recent 5 Compliance Log Entries */}
      <div className="p-4 md:p-6 bg-surface border border-border rounded-card">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-sm font-semibold text-text">Recent Recycling Certificate Logs</h3>
          <Button variant="ghost" size="sm" onClick={() => navigate('/compliance-log')}>
            View All Logs →
          </Button>
        </div>

        {data.recentLogs.length === 0 ? (
          <EmptyState
            title="No Collection Entries Logged"
            description="Log your verified recycling certificates from authorized recyclers to build your compliance record."
            actionText="Log First Certificate"
            onAction={() => navigate('/compliance-log')}
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-surface-2/60 text-text-muted font-mono uppercase border-b border-border">
                <tr>
                  <th className="p-3">Recycler Name</th>
                  <th className="p-3">Quantity</th>
                  <th className="p-3">Certificate Code</th>
                  <th className="p-3">Date Logged</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60">
                {data.recentLogs.map((log) => (
                  <tr key={log.logId} className="hover:bg-surface-2/30 transition-colors">
                    <td className="p-3 font-medium text-text">{log.recyclerName}</td>
                    <td className="p-3 font-mono text-green font-semibold">
                      {log.formattedQuantityCollectedKg}
                    </td>
                    <td className="p-3 font-mono text-text-dim">{log.eprCertificateNumber}</td>
                    <td className="p-3 font-mono text-text-muted">{log.dateLogged}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
