import React, { useState, useEffect } from 'react';
import { simulatorService } from '../services';
import { EcSimulationResult } from '../types';

export const EcSimulatorPage: React.FC = () => {
  const [shortfallMt, setShortfallMt] = useState<number>(50);
  const [baseRate, setBaseRate] = useState<number>(18000);
  const [severityMultiplier, setSeverityMultiplier] = useState<number>(1.2);
  const [delayMonths, setDelayMonths] = useState<number>(2);
  const [marketRate, setMarketRate] = useState<number>(4500);
  const [loading, setLoading] = useState<boolean>(true);
  const [simulation, setSimulation] = useState<EcSimulationResult | null>(null);

  const fetchSimulation = async () => {
    setLoading(true);
    try {
      const data = await simulatorService.simulateEcLiability({
        customShortfallMt: shortfallMt,
        baseRatePerMtInr: baseRate,
        severityMultiplier: severityMultiplier,
        delayMonths: delayMonths,
        marketRatePerMtInr: marketRate,
        financialYear: '2026-27',
      });
      setSimulation(data);
    } catch (err) {
      console.error('Failed to compute EC simulation', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSimulation();
  }, [shortfallMt, baseRate, severityMultiplier, delayMonths, marketRate]);

  return (
    <div className="p-4 md:p-8 max-w-7xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-border">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xl">⚠️</span>
            <h1 className="text-xl font-bold tracking-tight text-text">
              "What-If" Environmental Compensation (EC) Simulator
            </h1>
            {loading && (
              <span className="text-[10px] font-mono text-blue animate-pulse">Calculating...</span>
            )}
            <span className="text-[10px] uppercase font-mono px-2 py-0.5 rounded bg-red/10 text-red border border-red/30">
              CPCB Rule 22 Enforcement
            </span>
          </div>
          <p className="text-xs text-text-muted mt-1">
            Quantify financial exposure and calculate non-compliance penalties in INR under Indian E-Waste Rules 2022.
          </p>
        </div>

        <button
          onClick={() => window.print()}
          className="self-start md:self-auto px-4 py-2 bg-surface border border-border hover:border-blue/50 rounded-input text-xs font-mono text-text hover:text-blue transition-colors flex items-center gap-2 shadow-sm"
        >
          <span>📸</span>
          <span>Export Summary for CFO</span>
        </button>
      </div>

      {/* Main Grid: Sliders / Inputs vs ROI Comparison */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Controls Column */}
        <div className="lg:col-span-5 space-y-4">
          <div className="bg-surface border border-border rounded-card p-5 space-y-5">
            <h2 className="text-xs font-mono uppercase text-text-dim tracking-wider flex items-center gap-2">
              <span>🎛️</span>
              <span>Simulation Variables</span>
            </h2>

            {/* Slider 1: Deficit in MT */}
            <div className="space-y-2">
              <div className="flex justify-between items-center text-xs">
                <label className="font-semibold text-text">Projected Deficit (Metric Tons)</label>
                <span className="font-mono text-blue font-bold px-2 py-0.5 bg-blue/10 rounded">
                  {shortfallMt} MT
                </span>
              </div>
              <input
                type="range"
                min="1"
                max="500"
                step="1"
                value={shortfallMt}
                onChange={(e) => setShortfallMt(Number(e.target.value))}
                className="w-full accent-blue cursor-pointer"
              />
              <div className="flex justify-between text-[10px] text-text-dim font-mono">
                <span>1 MT</span>
                <span>250 MT</span>
                <span>500 MT</span>
              </div>
            </div>

            {/* Slider 2: Months past deadline */}
            <div className="space-y-2">
              <div className="flex justify-between items-center text-xs">
                <label className="font-semibold text-text">Delay Past Deadline</label>
                <span className="font-mono text-amber font-bold px-2 py-0.5 bg-amber/10 rounded">
                  {delayMonths} Month{delayMonths === 1 ? '' : 's'}
                </span>
              </div>
              <input
                type="range"
                min="0"
                max="12"
                step="1"
                value={delayMonths}
                onChange={(e) => setDelayMonths(Number(e.target.value))}
                className="w-full accent-amber cursor-pointer"
              />
              <p className="text-[10px] text-text-dim">
                Rule 22 applies +1% compounding interest surcharge per 30-day delay past fiscal deadline.
              </p>
            </div>

            {/* Severity Multiplier */}
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-text block">Hazardous Severity Factor</label>
              <select
                value={severityMultiplier}
                onChange={(e) => setSeverityMultiplier(Number(e.target.value))}
                className="w-full bg-surface-2 border border-border rounded-input px-3 py-2 text-xs text-text focus:outline-none focus:border-blue"
              >
                <option value={1.0}>1.0x — Standard Consumer Electronics (TVs, ACs)</option>
                <option value={1.2}>1.2x — Mixed E-Waste with Heavy Metals (Default)</option>
                <option value={1.5}>1.5x — Hazardous Telecom & Lithium (Phones, Batteries)</option>
                <option value={2.0}>2.0x — High-Toxicity Industrial PCBs & Monitors</option>
              </select>
            </div>

            {/* Pricing Rates */}
            <div className="grid grid-cols-2 gap-3 pt-2 border-t border-border">
              <div>
                <label className="text-[11px] font-mono text-text-muted block">CPCB Base Fine</label>
                <div className="mt-1 flex items-center bg-surface-2 border border-border rounded-input px-2.5 py-1.5">
                  <span className="text-xs text-text-dim mr-1">₹</span>
                  <input
                    type="number"
                    value={baseRate}
                    onChange={(e) => setBaseRate(Number(e.target.value))}
                    className="w-full bg-transparent text-xs font-mono text-text focus:outline-none"
                  />
                  <span className="text-[10px] text-text-dim">/MT</span>
                </div>
              </div>
              <div>
                <label className="text-[11px] font-mono text-text-muted block">Market Credit Cost</label>
                <div className="mt-1 flex items-center bg-surface-2 border border-border rounded-input px-2.5 py-1.5">
                  <span className="text-xs text-text-dim mr-1">₹</span>
                  <input
                    type="number"
                    value={marketRate}
                    onChange={(e) => setMarketRate(Number(e.target.value))}
                    className="w-full bg-transparent text-xs font-mono text-text focus:outline-none"
                  />
                  <span className="text-[10px] text-text-dim">/MT</span>
                </div>
              </div>
            </div>
          </div>

          {/* Regulatory Citation Notice */}
          <div className="bg-surface-2/60 border border-border rounded-card p-4 text-xs text-text-muted space-y-2">
            <span className="font-mono text-[10px] uppercase text-text font-bold block">
              CPCB Official Guidelines Clause
            </span>
            <p className="text-[11px] leading-relaxed">
              {simulation?.regulatoryClauseCiting ||
                'Rule 22 of E-Waste Rules 2022: Payment of Environmental Compensation does not absolve the producer from the collection requirement. Failure to settle invites statutory prosecution.'}
            </p>
          </div>
        </div>

        {/* Financial Comparison Column */}
        <div className="lg:col-span-7 space-y-4">
          {/* Executive CFO Verdict Card */}
          <div className="bg-gradient-to-br from-surface to-surface-2 border-2 border-red/40 rounded-card p-6 shadow-md relative overflow-hidden">
            <div className="absolute top-0 right-0 bg-red text-white text-[10px] font-mono font-bold uppercase px-3 py-1 rounded-bl">
              Statutory Liability Alert
            </div>

            <div className="text-xs font-mono uppercase text-red tracking-wider">
              Financial Liability Analysis
            </div>

            <div className="mt-3 flex items-baseline gap-3">
              <span className="text-3xl md:text-4xl font-extrabold font-mono text-red tracking-tight">
                {simulation?.formattedEstimatedEcPenaltyInr || '₹0'}
              </span>
              <span className="text-xs text-text-muted font-mono">CPCB Penalty Incurred</span>
            </div>

            <div className="mt-4 p-3 bg-red/10 border border-red/20 rounded-input">
              <p className="text-xs text-text font-semibold leading-relaxed">
                📢 {simulation?.cfoRoiHeadline}
              </p>
            </div>
          </div>

          {/* Head-to-Head Comparison: Inaction vs Procurement */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {/* Scenario A: Do Nothing */}
            <div className="bg-surface border border-red/30 rounded-card p-5 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-mono text-red uppercase font-semibold">Scenario A</span>
                <span className="text-[10px] px-2 py-0.5 rounded bg-red/10 text-red font-mono">Failing to Act</span>
              </div>
              <h3 className="text-sm font-bold text-text">Direct CPCB Penalty</h3>
              <p className="text-2xl font-mono font-bold text-red">
                {simulation?.formattedEstimatedEcPenaltyInr}
              </p>
              <ul className="text-[11px] text-text-muted space-y-1 font-mono pt-2 border-t border-border">
                <li>• No audit certificate obtained</li>
                <li>• Ongoing regulatory prosecution</li>
                <li>• Blacklist risk on CPCB portal</li>
              </ul>
            </div>

            {/* Scenario B: Gap-Filling Credits */}
            <div className="bg-surface border border-green/30 rounded-card p-5 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-mono text-green uppercase font-semibold">Scenario B</span>
                <span className="text-[10px] px-2 py-0.5 rounded bg-green/10 text-green font-mono">Buy EPR Credits</span>
              </div>
              <h3 className="text-sm font-bold text-text">Procurement Cost</h3>
              <p className="text-2xl font-mono font-bold text-green">
                {simulation?.formattedEstimatedMarketProcurementCostInr}
              </p>
              <ul className="text-[11px] text-text-muted space-y-1 font-mono pt-2 border-t border-border">
                <li>• 100% Audit-Ready Certificate</li>
                <li>• Full regulatory immunity</li>
                <li>• Saves {simulation?.formattedNetFinancialSavingsInr}</li>
              </ul>
            </div>
          </div>

          {/* ROI Metric Badge */}
          <div className="bg-surface border border-border rounded-card p-5 flex flex-col sm:flex-row items-center justify-between gap-4">
            <div>
              <span className="text-[11px] font-mono text-text-dim uppercase block">Immediate Net Capital Protected</span>
              <span className="text-2xl font-extrabold font-mono text-text mt-0.5 block">
                {simulation?.formattedNetFinancialSavingsInr}
              </span>
              <p className="text-[11px] text-text-muted mt-1">
                Yields a <strong>{simulation?.penaltyToCostRatio}x</strong> ROI over government fine.
              </p>
            </div>

            <a
              href="/marketplace"
              className="px-5 py-2.5 bg-blue hover:bg-blue-dark text-white rounded-input text-xs font-medium font-mono transition-colors text-center shrink-0 shadow"
            >
              Procure Gap Credits Now →
            </a>
          </div>
        </div>
      </div>
    </div>
  );
};
