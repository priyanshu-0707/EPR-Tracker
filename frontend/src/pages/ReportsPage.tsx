import React, { useEffect, useState } from 'react';
import { reportService } from '../services';
import { ReportItem } from '../types';
import { Button } from '../components/ui/Button';
import { Skeleton } from '../components/ui/Skeleton';
import { EmptyState } from '../components/ui/EmptyState';
import { Badge } from '../components/ui/Badge';

export const ReportsPage: React.FC = () => {
  const [reports, setReports] = useState<ReportItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isGenerating, setIsGenerating] = useState(false);
  const [financialYear, setFinancialYear] = useState('2026-27');

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    try {
      setIsLoading(true);
      const res = await reportService.getHistory();
      setReports(res);
    } catch {
      // Handled
    } finally {
      setIsLoading(false);
    }
  };

  const handleGenerate = async () => {
    try {
      setIsGenerating(true);
      const newReport = await reportService.generateReport(financialYear);
      setReports((prev) => [newReport, ...prev]);
    } catch {
      alert('Error initiating PDF report generation.');
    } finally {
      setIsGenerating(false);
    }
  };

  const handleDeleteReport = async (reportId: string) => {
    if (!window.confirm('Are you sure you want to delete this PDF compliance report from your archive?')) {
      return;
    }
    try {
      await reportService.deleteReport(reportId);
      setReports((prev) => prev.filter((r) => r.reportId !== reportId));
    } catch (err: any) {
      alert('Failed to delete report: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleDownloadReport = (rep: ReportItem) => {
    // Direct stream download route
    const downloadEndpoint = `/api/reports/download/${rep.reportId}`;
    const link = document.createElement('a');
    link.href = downloadEndpoint;
    link.setAttribute('download', `${rep.reportName}.pdf`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="p-4 md:p-6 flex flex-col gap-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-text">Compliance PDF Reports</h2>
          <p className="text-xs text-text-muted mt-0.5">
            Legal CPCB filing reports generated via iText 7 and securely hosted on Amazon S3
          </p>
        </div>

        <div className="flex items-center gap-3">
          <select
            value={financialYear}
            onChange={(e) => setFinancialYear(e.target.value)}
            className="bg-surface-2 border border-border text-xs rounded-input px-3 py-2 text-text font-mono focus:outline-none focus:border-blue"
          >
            <option value="2026-27">FY 2026-27</option>
            <option value="2025-26">FY 2025-26</option>
          </select>

          <button
            onClick={() => {
              window.open(`/api/reports/form-2-excel?financialYear=${financialYear}`, '_blank');
            }}
            className="px-3.5 py-2 bg-green/10 border border-green/30 hover:border-green text-green rounded-input text-xs font-mono font-semibold transition-colors flex items-center gap-2 shadow-sm"
          >
            <span>📊</span>
            <span>Download CPCB Form 2 (.xlsx)</span>
          </button>

          <Button
            variant="primary"
            size="md"
            isLoading={isGenerating}
            onClick={handleGenerate}
          >
            ⚡ Generate CPCB PDF Report
          </Button>
        </div>
      </div>

      {/* Progress Notification Banner while Generating */}
      {isGenerating && (
        <div className="p-4 bg-surface border border-blue/40 rounded-card flex items-center gap-3 animate-pulse">
          <div className="w-4 h-4 border-2 border-blue border-t-transparent rounded-full animate-spin" />
          <div className="text-xs text-text">
            Compiling executive tables, product inventory, and collection certificates via iText engine...
          </div>
        </div>
      )}

      {/* Report History Table */}
      <div className="p-5 bg-surface border border-border rounded-card">
        <h3 className="text-sm font-semibold text-text mb-4">Generated Report Archive</h3>

        {isLoading ? (
          <div className="flex flex-col gap-3">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : reports.length === 0 ? (
          <EmptyState
            title="No Reports Generated Yet"
            description="Generate your official comprehensive compliance document ready for submission to the CPCB portal."
            actionText="Generate Now"
            onAction={handleGenerate}
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-surface-2/60 text-text-muted font-mono uppercase border-b border-border">
                <tr>
                  <th className="p-3">Report Document</th>
                  <th className="p-3">Financial Year</th>
                  <th className="p-3">Compliance Status</th>
                  <th className="p-3">Fulfillment</th>
                  <th className="p-3">Generated At</th>
                  <th className="p-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60">
                {reports.map((rep) => (
                  <tr key={rep.reportId} className="hover:bg-surface-2/30 transition-colors">
                    <td className="p-3 font-medium text-text flex items-center gap-2">
                      <span>📄</span>
                      <span className="font-mono">{rep.reportName}</span>
                    </td>
                    <td className="p-3 font-mono text-text-dim">{rep.financialYear}</td>
                    <td className="p-3">
                      <Badge status={rep.complianceStatus} size="sm" />
                    </td>
                    <td className="p-3 font-mono text-text">{rep.fulfillmentPercentage}%</td>
                    <td className="p-3 font-mono text-text-muted">
                      {new Date(rep.generatedAt).toLocaleDateString()}
                    </td>
                    <td className="p-3 text-right">
                      <div className="flex items-center justify-end gap-2">
                        {/* Download Button */}
                        <button
                          onClick={() => handleDownloadReport(rep)}
                          className="px-2.5 py-1 rounded bg-blue/10 border border-blue/30 hover:bg-blue/20 text-blue font-mono text-[11px] font-semibold transition-colors flex items-center gap-1 shadow-sm"
                          title="Download PDF Document"
                        >
                          <span>⬇️</span>
                          <span>Download</span>
                        </button>

                        {/* Delete Button */}
                        <button
                          onClick={() => handleDeleteReport(rep.reportId)}
                          className="px-2.5 py-1 rounded bg-red/10 border border-red/30 hover:bg-red/20 text-red font-mono text-[11px] font-semibold transition-colors flex items-center gap-1 shadow-sm"
                          title="Delete Report"
                        >
                          <span>🗑️</span>
                          <span>Delete</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* PDF Specifications Detail */}
      <div className="p-5 bg-surface-2 border border-border rounded-card">
        <h4 className="text-xs font-mono uppercase text-text-muted mb-2">CPCB Compliance Report Specification</h4>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs text-text-dim">
          <ul className="list-disc list-inside space-y-1">
            <li>Section 1: Official Header & Regulatory Subtitle</li>
            <li>Section 2: Corporate details & CPCB Registration number</li>
            <li>Section 3: Executive Summary (obligation, collected, quota status)</li>
          </ul>
          <ul className="list-disc list-inside space-y-1">
            <li>Section 4: Itemized electrical equipment portfolio & Cf factors</li>
            <li>Section 5: Itemized recycler collection certificate numbers</li>
            <li>Section 6 & 7: Official status badge, timestamp & CPCB portal URL</li>
          </ul>
        </div>
      </div>
    </div>
  );
};
