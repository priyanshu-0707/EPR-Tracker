import React, { useState, useEffect } from 'react';
import { marketplaceService } from '../services';
import { MarketplaceRecommendations, RecyclerMarketOffer } from '../types';

export const MarketplacePage: React.FC = () => {
  const [data, setData] = useState<MarketplaceRecommendations | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedOffer, setSelectedOffer] = useState<RecyclerMarketOffer | null>(null);
  const [procureAmountMt, setProcureAmountMt] = useState<number>(10);
  const [orderSubmitting, setOrderSubmitting] = useState<boolean>(false);
  const [orderSuccess, setOrderSuccess] = useState<string | null>(null);

  const loadMarketplace = async () => {
    setLoading(true);
    try {
      const res = await marketplaceService.getRecommendations('2026-27');
      setData(res);
      if (res.totalDeficitMetricTons && res.totalDeficitMetricTons > 0) {
        setProcureAmountMt(Math.min(res.totalDeficitMetricTons, 25));
      }
    } catch (err) {
      console.error('Failed to load marketplace data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMarketplace();
  }, []);

  const handleExecuteOrder = async () => {
    if (!selectedOffer) return;
    setOrderSubmitting(true);
    try {
      const res = await marketplaceService.placeOrder({
        recyclerId: selectedOffer.recyclerId,
        category: selectedOffer.matchingCategory,
        creditsMetricTons: procureAmountMt,
        agreedRatePerMtInr: selectedOffer.ratePerMtInr,
        financialYear: '2026-27',
        notes: 'Procured via Automated Gap-Filling Marketplace',
      });
      setOrderSuccess(res.message);
      setSelectedOffer(null);
      loadMarketplace(); // Refresh deficits
    } catch (err: any) {
      alert('Order failed: ' + (err.response?.data?.message || err.message));
    } finally {
      setOrderSubmitting(false);
    }
  };

  return (
    <div className="p-4 md:p-8 max-w-7xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-border">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xl">🛒</span>
            <h1 className="text-xl font-bold tracking-tight text-text">
              Automated "Gap-Filling" Marketplace Procurement
            </h1>
            {loading && (
              <span className="text-[10px] font-mono text-blue animate-pulse">Scanning Recyclers...</span>
            )}
            <span className="text-[10px] uppercase font-mono px-2 py-0.5 rounded bg-green/10 text-green border border-green/30">
              CPCB Authorized Spot Market
            </span>
          </div>
          <p className="text-xs text-text-muted mt-1">
            Detect obligation shortfalls by EEE category and execute instant credit procurement from verified recyclers.
          </p>
        </div>

        <a
          href="/ec-simulator"
          className="self-start md:self-auto px-3.5 py-2 bg-surface-2 border border-border hover:border-text-muted rounded-input text-xs font-mono text-text transition-colors flex items-center gap-2"
        >
          <span>📉</span>
          <span>View Penalty Simulator</span>
        </a>
      </div>

      {orderSuccess && (
        <div className="p-4 bg-green/10 border border-green/30 rounded-card flex items-center justify-between">
          <div className="flex items-center gap-2 text-xs font-mono text-green">
            <span>✅</span>
            <span>{orderSuccess}</span>
          </div>
          <button
            onClick={() => setOrderSuccess(null)}
            className="text-xs text-text-dim hover:text-text"
          >
            ✕
          </button>
        </div>
      )}

      {/* Top Cards: Total Shortfall & Deficit Breakdown */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Total Gap Card */}
        <div className="bg-surface border border-border rounded-card p-5 space-y-2">
          <span className="text-[11px] font-mono text-text-dim uppercase">Total Unfulfilled Deficit</span>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-extrabold font-mono text-amber">
              {data?.formattedTotalDeficitMetricTons || '0 MT'}
            </span>
            <span className="text-xs font-mono text-text-muted">
              ({data?.totalDeficitUnits?.toLocaleString('en-IN') || 0} units)
            </span>
          </div>
          <p className="text-[10px] text-text-dim">
            Accumulated gap across all scheduled EEE categories for FY 2026-27.
          </p>
        </div>

        {/* Categories with Deficits */}
        <div className="md:col-span-2 bg-surface border border-border rounded-card p-5 space-y-3">
          <span className="text-[11px] font-mono text-text-dim uppercase block">
            Category-Specific Compliance Shortfalls
          </span>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            {data?.categoryDeficits.map((def) => (
              <div
                key={def.category}
                className="p-2.5 bg-surface-2 rounded-input border border-border flex justify-between items-center text-xs"
              >
                <div>
                  <span className="font-semibold text-text block truncate max-w-[180px]">
                    {def.categoryDisplayName}
                  </span>
                  <span className="text-[10px] font-mono text-text-dim">
                    Target: {def.targetUnits.toLocaleString('en-IN')} units
                  </span>
                </div>
                <div className="text-right">
                  <span className="font-mono text-amber font-bold block">
                    {def.formattedDeficitMetricTons}
                  </span>
                  <span className="text-[10px] font-mono text-text-muted">shortfall</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Recycler Credit Offers Table */}
      <div className="bg-surface border border-border rounded-card overflow-hidden">
        <div className="p-4 border-b border-border flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="text-xs font-mono uppercase text-text font-bold">
              Available Verified Surplus Credits
            </span>
            <span className="text-[10px] px-1.5 py-0.5 rounded bg-blue/10 text-blue font-mono">
              Live CPCB Quota
            </span>
          </div>
          <span className="text-[11px] text-text-dim font-mono">
            Sorted by Best Price (₹/MT)
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left">
            <thead className="bg-surface-2/60 text-text-dim font-mono uppercase text-[10px] border-b border-border">
              <tr>
                <th className="p-3">Recycler Name</th>
                <th className="p-3">Location</th>
                <th className="p-3">Verified Surplus</th>
                <th className="p-3">Credit Spot Rate</th>
                <th className="p-3">CPCB Status</th>
                <th className="p-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border font-sans">
              {data?.matchingRecyclerOffers.map((offer) => (
                <tr key={offer.recyclerId} className="hover:bg-surface-2/40 transition-colors">
                  <td className="p-3 font-medium text-text">
                    <div className="font-semibold">{offer.recyclerName}</div>
                    <div className="text-[10px] font-mono text-text-dim">{offer.contactEmail}</div>
                  </td>
                  <td className="p-3 text-text-muted">
                    {offer.city}, {offer.state}
                  </td>
                  <td className="p-3 font-mono font-semibold text-text">
                    {offer.availableCreditsTonnes} MT
                  </td>
                  <td className="p-3 font-mono font-bold text-green">
                    {offer.formattedRatePerMtInr}
                  </td>
                  <td className="p-3">
                    <span className="inline-flex items-center gap-1 text-[10px] font-mono text-blue bg-blue/10 border border-blue/20 px-2 py-0.5 rounded">
                      <span>✓</span>
                      <span>CPCB Certified</span>
                    </span>
                  </td>
                  <td className="p-3 text-right">
                    <button
                      onClick={() => {
                        setSelectedOffer(offer);
                        setProcureAmountMt(Math.min(offer.availableCreditsTonnes, data?.totalDeficitMetricTons || 10));
                      }}
                      className="px-3 py-1.5 bg-blue hover:bg-blue-dark text-white rounded-input text-xs font-mono transition-colors shadow-sm"
                    >
                      Procure Credits →
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Procurement Modal / Drawer */}
      {selectedOffer && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-surface border border-border rounded-card max-w-lg w-full p-6 space-y-5 shadow-2xl">
            <div className="flex items-center justify-between pb-3 border-b border-border">
              <div>
                <h3 className="text-sm font-bold text-text">Confirm EPR Credit Purchase</h3>
                <p className="text-[11px] text-text-dim">
                  Issuing verified credit certificate from {selectedOffer.recyclerName}
                </p>
              </div>
              <button
                onClick={() => setSelectedOffer(null)}
                className="text-text-dim hover:text-text text-sm"
              >
                ✕
              </button>
            </div>

            <div className="space-y-4">
              <div className="p-3 bg-surface-2 rounded-input border border-border space-y-1 text-xs">
                <div className="flex justify-between">
                  <span className="text-text-dim">Recycler:</span>
                  <span className="font-semibold text-text">{selectedOffer.recyclerName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-text-dim">Unit Price:</span>
                  <span className="font-mono text-green font-bold">{selectedOffer.formattedRatePerMtInr}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-text-dim">CPCB Status:</span>
                  <span className="font-mono text-blue">100% Authorized Capacity Validated</span>
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-text block">
                  Quantity to Procure (Metric Tons)
                </label>
                <input
                  type="number"
                  min="0.1"
                  max={selectedOffer.availableCreditsTonnes}
                  step="0.5"
                  value={procureAmountMt}
                  onChange={(e) => setProcureAmountMt(Number(e.target.value))}
                  className="w-full bg-surface-2 border border-border rounded-input px-3 py-2 text-xs font-mono text-text focus:outline-none focus:border-blue"
                />
                <span className="text-[10px] text-text-dim">
                  Max available quota: {selectedOffer.availableCreditsTonnes} MT
                </span>
              </div>

              {/* Total Calculation */}
              <div className="p-3 bg-green/10 border border-green/30 rounded-input flex justify-between items-center">
                <span className="text-xs font-mono uppercase text-green font-semibold">Total Order Value</span>
                <span className="text-base font-extrabold font-mono text-green">
                  ₹{((procureAmountMt || 0) * selectedOffer.ratePerMtInr).toLocaleString('en-IN')}
                </span>
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-3 border-t border-border">
              <button
                onClick={() => setSelectedOffer(null)}
                className="px-4 py-2 border border-border rounded-input text-xs font-mono text-text-muted hover:text-text"
              >
                Cancel
              </button>
              <button
                disabled={orderSubmitting}
                onClick={handleExecuteOrder}
                className="px-5 py-2 bg-green hover:bg-green/90 text-white font-mono text-xs rounded-input font-bold transition-colors shadow flex items-center gap-2"
              >
                {orderSubmitting ? 'Processing...' : 'Confirm & Auto-Log Certificate'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
