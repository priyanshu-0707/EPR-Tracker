import React, { useEffect, useState } from 'react';
import { alertService } from '../services';
import { AlertSettings, AlertHistoryItem } from '../types';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Skeleton } from '../components/ui/Skeleton';

export const AlertSettingsPage: React.FC = () => {
  const [settings, setSettings] = useState<AlertSettings | null>(null);
  const [history, setHistory] = useState<AlertHistoryItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  // Form states
  const [emailAlerts, setEmailAlerts] = useState(true);
  const [smsAlerts, setSmsAlerts] = useState(true);
  const [alertEmail, setAlertEmail] = useState('');
  const [alertPhone, setAlertPhone] = useState('');

  useEffect(() => {
    loadAlertData();
  }, []);

  const loadAlertData = async () => {
    try {
      setIsLoading(true);
      const [settingsRes, historyRes] = await Promise.all([
        alertService.getSettings(),
        alertService.getHistory(),
      ]);
      setSettings(settingsRes);
      setHistory(historyRes);
      setEmailAlerts(settingsRes.emailAlertsEnabled);
      setSmsAlerts(settingsRes.smsAlertsEnabled);
      setAlertEmail(settingsRes.alertEmail || '');
      setAlertPhone(settingsRes.alertPhone || '');
    } catch {
      // Handled
    } finally {
      setIsLoading(false);
    }
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setIsSaving(true);
      const updated = await alertService.updateSettings({
        emailAlertsEnabled: emailAlerts,
        smsAlertsEnabled: smsAlerts,
        alertEmail,
        alertPhone,
      });
      setSettings(updated);
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 3000);
    } catch {
      alert('Unable to update alert preferences.');
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <div className="p-4 md:p-6 flex flex-col gap-6">
        <Skeleton className="h-8 w-60" />
        <Skeleton className="h-48 w-full rounded-card" />
        <Skeleton className="h-64 w-full rounded-card" />
      </div>
    );
  }

  return (
    <div className="p-4 md:p-6 flex flex-col gap-6">
      {/* Header */}
      <div>
        <h2 className="text-xl font-bold text-text">CPCB Deadline & Threshold Alerts</h2>
        <p className="text-xs text-text-muted mt-0.5">
          Automated compliance monitoring via Amazon SNS (Email & SMS channels) and EventBridge schedules
        </p>
      </div>

      {saveSuccess && (
        <div className="p-3 bg-green/10 border border-green/30 text-green rounded-card text-xs font-mono">
          ✓ Alert preferences successfully saved and synchronized with Amazon SNS.
        </div>
      )}

      {/* Settings Form */}
      <div className="p-5 bg-surface border border-border rounded-card">
        <h3 className="text-sm font-semibold text-text mb-4">Notification Channels & Delivery Target</h3>
        <form onSubmit={handleSave} className="flex flex-col gap-5">
          {/* Toggles */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="p-4 bg-surface-2 border border-border rounded-input flex items-center justify-between">
              <div>
                <span className="text-xs font-medium text-text block">Email Compliance Alerts</span>
                <span className="text-[11px] text-text-muted">Receive regulatory briefs & deadline reminders</span>
              </div>
              <input
                type="checkbox"
                checked={emailAlerts}
                onChange={(e) => setEmailAlerts(e.target.checked)}
                className="w-4 h-4 rounded border-border accent-blue cursor-pointer"
              />
            </div>

            <div className="p-4 bg-surface-2 border border-border rounded-input flex items-center justify-between">
              <div>
                <span className="text-xs font-medium text-text block">SMS Critical Alerts</span>
                <span className="text-[11px] text-text-muted">Urgent alerts when under-quota within 30 days</span>
              </div>
              <input
                type="checkbox"
                checked={smsAlerts}
                onChange={(e) => setSmsAlerts(e.target.checked)}
                className="w-4 h-4 rounded border-border accent-blue cursor-pointer"
              />
            </div>
          </div>

          {/* Delivery Coordinates */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Notification Email Address"
              type="email"
              value={alertEmail}
              onChange={(e) => setAlertEmail(e.target.value)}
              placeholder="compliance@enterprise.com"
              required
            />

            <Input
              label="Notification Mobile Number"
              type="tel"
              value={alertPhone}
              onChange={(e) => setAlertPhone(e.target.value)}
              placeholder="+91 98765 43210"
              required
            />
          </div>

          <div className="flex justify-end">
            <Button type="submit" variant="primary" isLoading={isSaving}>
              Save Alert Preferences
            </Button>
          </div>
        </form>
      </div>

      {/* Active Triggers Matrix */}
      <div className="p-5 bg-surface border border-border rounded-card">
        <h3 className="text-sm font-semibold text-text mb-4">Configured Compliance Triggers</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {settings?.activeTriggers.map((t, idx) => (
            <div key={idx} className="p-4 bg-surface-2 border border-border rounded-card flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between">
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-blue/15 text-blue border border-blue/30 uppercase">
                    {t.frequency}
                  </span>
                  <span className="w-2 h-2 rounded-full bg-green" />
                </div>
                <h4 className="text-xs font-bold text-text mt-3">{t.name}</h4>
                <p className="text-[11px] text-text-muted mt-1 leading-relaxed">{t.description}</p>
              </div>
              <div className="mt-4 pt-3 border-t border-border/60 text-[10px] font-mono text-green">
                Status: Active
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Alert History Table */}
      <div className="p-5 bg-surface border border-border rounded-card">
        <h3 className="text-sm font-semibold text-text mb-4">Historical Dispatched Alerts</h3>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-surface-2/60 text-text-muted font-mono uppercase border-b border-border">
              <tr>
                <th className="p-3">Channel</th>
                <th className="p-3">Subject / Trigger</th>
                <th className="p-3">Message Snippet</th>
                <th className="p-3">Recipient</th>
                <th className="p-3">Dispatched At</th>
                <th className="p-3 text-right">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/60">
              {history.map((h) => (
                <tr key={h.alertId} className="hover:bg-surface-2/30 transition-colors">
                  <td className="p-3 font-mono font-medium text-blue">{h.alertType}</td>
                  <td className="p-3 font-semibold text-text">{h.title}</td>
                  <td className="p-3 text-text-muted truncate max-w-xs">{h.message}</td>
                  <td className="p-3 font-mono text-text-dim">{h.recipient}</td>
                  <td className="p-3 font-mono text-text-muted">
                    {new Date(h.sentAt).toLocaleString()}
                  </td>
                  <td className="p-3 text-right">
                    <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-green/15 text-green border border-green/30">
                      {h.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
