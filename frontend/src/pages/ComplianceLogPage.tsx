import React, { useEffect, useState } from 'react';
import { complianceLogService, recyclerService } from '../services';
import { ComplianceLogItem, RecyclerItem } from '../types';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Skeleton } from '../components/ui/Skeleton';
import { EmptyState } from '../components/ui/EmptyState';
import { Modal } from '../components/ui/Modal';
import { formatIndianNumber } from '../utils/formatters';

export const ComplianceLogPage: React.FC = () => {
  const [logs, setLogs] = useState<ComplianceLogItem[]>([]);
  const [recyclers, setRecyclers] = useState<RecyclerItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Form inputs
  const [selectedRecyclerId, setSelectedRecyclerId] = useState('');
  const [quantityKg, setQuantityKg] = useState<number | ''>('');
  const [certNumber, setCertNumber] = useState('');
  const [dateLogged, setDateLogged] = useState(new Date().toISOString().split('T')[0]);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Delete modal
  const [deleteLogId, setDeleteLogId] = useState<string | null>(null);

  // Table filter/sort
  const [searchTerm, setSearchTerm] = useState('');

  // Feature 2: Anti-Fraud Chain of Custody Validation State
  const [validationResult, setValidationResult] = useState<import('../types').CertificateValidationResult | null>(null);
  const [isValidating, setIsValidating] = useState(false);

  const handleValidateCertificate = async () => {
    if (!selectedRecyclerId || !certNumber || !quantityKg) {
      alert('Please fill in Recycler, Quantity, and Certificate number first.');
      return;
    }
    setIsValidating(true);
    try {
      const res = await (await import('../services')).validationService.validateCertificate({
        recyclerId: selectedRecyclerId,
        certificateNumber: certNumber,
        quantityKg: Number(quantityKg),
        financialYear: '2026-27',
      });
      setValidationResult(res);
    } catch (err: any) {
      alert('Validation check failed: ' + (err.response?.data?.message || err.message));
    } finally {
      setIsValidating(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setIsLoading(true);
      const [logsRes, recRes] = await Promise.all([
        complianceLogService.getLogs('2026-27'),
        recyclerService.getRecyclers(),
      ]);
      setLogs(logsRes);
      setRecyclers(recRes);
      if (recRes.length > 0) {
        setSelectedRecyclerId(recRes[0].recyclerId);
      }
    } catch {
      // Handled
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedRecyclerId || !quantityKg || typeof quantityKg !== 'number' || !certNumber.trim()) return;

    try {
      setIsSubmitting(true);
      const selectedRec = recyclers.find((r) => r.recyclerId === selectedRecyclerId);
      const newLog = await complianceLogService.createLog({
        recyclerId: selectedRecyclerId,
        recyclerName: selectedRec ? selectedRec.name : undefined,
        quantityCollectedKg: quantityKg,
        eprCertificateNumber: certNumber,
        dateLogged,
        financialYear: '2026-27',
      });

      setLogs((prev) => [newLog, ...prev]);
      setQuantityKg('');
      setCertNumber('');
    } catch {
      // Handled
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteLogId) return;
    try {
      await complianceLogService.deleteLog(deleteLogId);
      setLogs((prev) => prev.filter((l) => l.logId !== deleteLogId));
      setDeleteLogId(null);
    } catch {
      // Handled
    }
  };

  const handleExportCsv = () => {
    window.open(complianceLogService.exportCsvUrl(), '_blank');
  };

  const totalCollectedKg = logs.reduce((acc, l) => acc + (l.quantityCollectedKg || 0), 0);

  const filteredLogs = logs.filter(
    (l) =>
      l.recyclerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      l.eprCertificateNumber.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="p-4 md:p-6 flex flex-col gap-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-text">Compliance Collection Log</h2>
          <p className="text-xs text-text-muted mt-0.5">
            Rule 6: Track end-to-end e-waste collection and trading certificate numbers
          </p>
        </div>

        <div className="flex gap-2">
          <Button variant="secondary" size="sm" onClick={handleExportCsv}>
            📥 Export CSV
          </Button>
        </div>
      </div>

      {/* Cumulative Tally Card */}
      <div className="p-5 bg-surface border border-border rounded-card flex flex-col sm:flex-row items-center justify-between gap-4">
        <div>
          <span className="text-xs font-mono uppercase text-text-muted">Total Verified Recycling</span>
          <h3 className="text-2xl font-bold font-mono text-green mt-1">
            {formatIndianNumber(totalCollectedKg)} <span className="text-xs font-sans text-text-dim">kg</span>
          </h3>
          <p className="text-xs text-text-muted font-mono mt-0.5">
            Backed by {logs.length} authenticated recycling certificates
          </p>
        </div>

        <div className="text-right hidden sm:block">
          <span className="text-xs font-mono text-text-dim">CPCB Portal Compliant</span>
          <div className="text-xs font-mono text-blue font-semibold mt-1">
            Rule 13 Verified Records
          </div>
        </div>
      </div>

      {/* Form: Log New Certificate */}
      <div className="p-5 bg-surface border border-border rounded-card">
        <h3 className="text-sm font-semibold text-text mb-4">Record New Recycled E-Waste Certificate</h3>
        <form onSubmit={handleCreate} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 items-end">
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-text-muted">Select CPCB Recycler</label>
            <select
              value={selectedRecyclerId}
              onChange={(e) => setSelectedRecyclerId(e.target.value)}
              className="w-full bg-surface-2 border border-border rounded-input px-3 py-2 text-xs text-text focus:outline-none focus:border-blue"
            >
              {recyclers.map((r) => (
                <option key={r.recyclerId} value={r.recyclerId}>
                  {r.name} ({r.city})
                </option>
              ))}
            </select>
          </div>

          <Input
            label="Quantity Collected (kg)"
            type="number"
            placeholder="e.g. 5000"
            value={quantityKg}
            onChange={(e) => setQuantityKg(e.target.value ? Number(e.target.value) : '')}
            required
            min={1}
          />

          <Input
            label="EPR Certificate No."
            placeholder="e.g. CPCB/REC/2026/9021"
            value={certNumber}
            onChange={(e) => setCertNumber(e.target.value)}
            required
          />

          <Input
            label="Date Logged"
            type="date"
            value={dateLogged}
            onChange={(e) => setDateLogged(e.target.value)}
            required
          />

          <div className="sm:col-span-2 lg:col-span-4 flex flex-col sm:flex-row items-center justify-between gap-3 mt-2 pt-3 border-t border-border">
            <button
              type="button"
              disabled={isValidating}
              onClick={handleValidateCertificate}
              className="text-xs font-mono font-medium px-3.5 py-2 rounded-input bg-surface-2 border border-border hover:border-blue/50 text-text transition-colors flex items-center gap-2"
            >
              <span>🛡️</span>
              <span>{isValidating ? 'Checking Ledger...' : 'Run Anti-Fraud Validation Gate'}</span>
            </button>

            <Button type="submit" variant="primary" isLoading={isSubmitting}>
              + Record Certificate
            </Button>
          </div>

          {/* Validation Result Box */}
          {validationResult && (
            <div
              className={`sm:col-span-2 lg:col-span-4 p-4 rounded-input border text-xs font-mono space-y-2 ${
                validationResult.valid
                  ? 'bg-green/10 border-green/30 text-green'
                  : 'bg-red/10 border-red/30 text-red'
              }`}
            >
              <div className="flex items-center justify-between">
                <span className="font-bold flex items-center gap-1.5">
                  {validationResult.valid ? '✅ AUDIT-READY GUARANTEE CERTIFIED' : '⚠️ FRAUD / REGULATORY RISK FLAGGED'}
                </span>
                <span className="text-[10px] bg-surface px-2 py-0.5 rounded border border-border text-text">
                  Hash: {validationResult.auditGuaranteeHash}
                </span>
              </div>
              <p className="text-[11px] text-text">
                Capacity utilization: {validationResult.totalUtilizedCapacityTonnes} MT / {validationResult.annualCapacityTonnes} MT ({validationResult.capacityUtilizationPercent}%).
                {validationResult.valid
                  ? ' Zero ghost-credit duplicate risk found in national registry.'
                  : ` Warnings: ${validationResult.warnings.join(' ')}`}
              </p>
            </div>
          )}
        </form>
      </div>

      {/* Table of Entries */}
      <div className="p-5 bg-surface border border-border rounded-card">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
          <h3 className="text-sm font-semibold text-text">Recorded Certificate Ledger</h3>
          <div className="w-full sm:w-64">
            <input
              type="text"
              placeholder="Filter by recycler or certificate..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full bg-surface-2 border border-border rounded-input px-3 py-1.5 text-xs text-text placeholder-text-dim focus:outline-none focus:border-blue"
            />
          </div>
        </div>

        {isLoading ? (
          <div className="flex flex-col gap-3">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : filteredLogs.length === 0 ? (
          <EmptyState
            title="No Compliance Logs Found"
            description="Log your verified recycling collection quantities from certified e-waste recyclers to ensure audit readiness."
            actionText="Sample Log"
            onAction={() => {
              setQuantityKg(2500);
              setCertNumber('CPCB/REC/MAH/2026/0199');
            }}
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-surface-2/60 text-text-muted font-mono uppercase border-b border-border">
                <tr>
                  <th className="p-3">Recycler Name</th>
                  <th className="p-3">Quantity Collected</th>
                  <th className="p-3">EPR Certificate No.</th>
                  <th className="p-3">Date Logged</th>
                  <th className="p-3">Financial Year</th>
                  <th className="p-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60">
                {filteredLogs.map((item) => (
                  <tr key={item.logId} className="hover:bg-surface-2/30 transition-colors">
                    <td className="p-3 font-medium text-text">{item.recyclerName}</td>
                    <td className="p-3 font-mono text-green font-bold">
                      {item.formattedQuantityCollectedKg}
                    </td>
                    <td className="p-3 font-mono text-text">{item.eprCertificateNumber}</td>
                    <td className="p-3 font-mono text-text-muted">{item.dateLogged}</td>
                    <td className="p-3 font-mono text-text-dim">{item.financialYear}</td>
                    <td className="p-3 text-right">
                      <button
                        onClick={() => setDeleteLogId(item.logId)}
                        className="text-text-muted hover:text-red transition-colors px-2 py-1"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={!!deleteLogId}
        onClose={() => setDeleteLogId(null)}
        title="Delete Compliance Record"
      >
        <p className="text-sm text-text-muted mb-4">
          Are you sure you want to remove this recycling certificate entry? This will reduce your recorded fulfillment progress.
        </p>
        <div className="flex justify-end gap-2">
          <Button variant="secondary" size="sm" onClick={() => setDeleteLogId(null)}>
            Cancel
          </Button>
          <Button variant="danger" size="sm" onClick={handleDelete}>
            Confirm Delete
          </Button>
        </div>
      </Modal>
    </div>
  );
};
