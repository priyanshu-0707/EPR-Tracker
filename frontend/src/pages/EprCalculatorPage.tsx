import React, { useEffect, useState } from 'react';
import { productService, eprService } from '../services';
import { CategoryItem, ProductItem, EprCalculationResult } from '../types';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Skeleton } from '../components/ui/Skeleton';
import { EmptyState } from '../components/ui/EmptyState';
import { Modal } from '../components/ui/Modal';
import { formatIndianNumber } from '../utils/formatters';

export const EprCalculatorPage: React.FC = () => {
  const [categories, setCategories] = useState<CategoryItem[]>([]);
  const [products, setProducts] = useState<ProductItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Form states
  const [productName, setProductName] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('EARPHONE_HEADPHONE');
  const [unitsSold, setUnitsSold] = useState<number | ''>(1000000);
  const [financialYear, setFinancialYear] = useState('2026-27');

  // Instant calculated preview
  const [previewResult, setPreviewResult] = useState<EprCalculationResult | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Edit / Delete modal states
  const [editingProduct, setEditingProduct] = useState<ProductItem | null>(null);
  const [deleteId, setDeleteId] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    if (selectedCategory && unitsSold && typeof unitsSold === 'number' && unitsSold > 0) {
      calculatePreview(selectedCategory, unitsSold, financialYear);
    }
  }, [selectedCategory, unitsSold, financialYear]);

  const loadData = async () => {
    try {
      setIsLoading(true);
      const [catRes, prodRes] = await Promise.all([
        productService.getCategories(),
        productService.getProducts(),
      ]);
      setCategories(catRes);
      setProducts(prodRes);
      if (catRes.length > 0) {
        setSelectedCategory(catRes[0].category);
      }
    } catch {
      // Handled
    } finally {
      setIsLoading(false);
    }
  };

  const calculatePreview = async (cat: string, units: number, fy: string) => {
    try {
      const res = await eprService.calculate({
        category: cat,
        unitsSold: units,
        financialYear: fy,
      });
      setPreviewResult(res);
    } catch {
      // Ignored for instant preview
    }
  };

  const handleAddProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!productName.trim() || !unitsSold || typeof unitsSold !== 'number') return;

    try {
      setIsSubmitting(true);
      const newProd = await productService.createProduct({
        productName,
        productCategory: selectedCategory,
        unitsSold,
        financialYear,
      });
      setProducts((prev) => [newProd, ...prev]);
      setProductName('');
    } catch {
      // Handled
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await productService.deleteProduct(deleteId);
      setProducts((prev) => prev.filter((p) => p.productId !== deleteId));
      setDeleteId(null);
    } catch {
      // Handled
    }
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingProduct) return;
    try {
      const updated = await productService.updateProduct(editingProduct.productId, {
        productName: editingProduct.productName,
        productCategory: editingProduct.productCategory,
        unitsSold: editingProduct.unitsSold,
        financialYear: editingProduct.financialYear,
      });
      setProducts((prev) => prev.map((p) => (p.productId === updated.productId ? updated : p)));
      setEditingProduct(null);
    } catch {
      // Handled
    }
  };

  const totalObligationAll = products.reduce((acc, p) => acc + (p.eprObligationUnits || 0), 0);
  const totalUnitsSoldAll = products.reduce((acc, p) => acc + (p.unitsSold || 0), 0);

  return (
    <div className="p-4 md:p-6 flex flex-col gap-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
        <div>
          <h2 className="text-xl font-bold text-text">EPR Obligation Calculator</h2>
          <p className="text-xs text-text-muted mt-0.5">
            CPCB Formula: Q<sub>EPR</sub> = Units Sold (Q<sub>p</sub>) × Conversion Factor (C<sub>f</sub>) × Target Percentage
          </p>
        </div>

        <div className="flex items-center gap-2">
          <label className="text-xs font-mono text-text-muted">Target Year:</label>
          <select
            value={financialYear}
            onChange={(e) => setFinancialYear(e.target.value)}
            className="bg-surface-2 border border-border text-xs rounded-input px-3 py-1.5 text-text font-mono focus:outline-none focus:border-blue"
          >
            <option value="2026-27">FY2026-27 (80% Target)</option>
            <option value="2025-26">FY2025-26 (70% Target)</option>
            <option value="2024-25">FY2024-25 (65% Target)</option>
            <option value="2023-24">FY2023-24 (60% Target)</option>
            <option value="2027-28">FY2027-28 (90% Target)</option>
          </select>
        </div>
      </div>

      {/* Input Form & Realtime Preview Card */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Form */}
        <div className="lg:col-span-2 p-5 bg-surface border border-border rounded-card">
          <h3 className="text-sm font-semibold text-text mb-4">Register Product for Obligation Tracking</h3>
          <form onSubmit={handleAddProduct} className="flex flex-col gap-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input
                label="Product Name"
                placeholder="e.g. boAt Airdopes 141"
                value={productName}
                onChange={(e) => setProductName(e.target.value)}
                required
              />

              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-medium text-text-muted">CPCB Category</label>
                <select
                  value={selectedCategory}
                  onChange={(e) => setSelectedCategory(e.target.value)}
                  className="w-full bg-surface-2 border border-border rounded-input px-3 py-2 text-sm text-text focus:outline-none focus:border-blue"
                >
                  {categories.map((cat) => (
                    <option key={cat.category} value={cat.category}>
                      {cat.displayName} ({cat.lifespanYears} yrs, Cf: {cat.conversionFactor})
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input
                label="Units Sold in Financial Year"
                type="number"
                placeholder="e.g. 1000000"
                value={unitsSold}
                onChange={(e) => setUnitsSold(e.target.value ? Number(e.target.value) : '')}
                required
                min={1}
              />

              <div className="flex items-end">
                <Button type="submit" variant="primary" isLoading={isSubmitting} className="w-full">
                  + Add to EPR Inventory
                </Button>
              </div>
            </div>
          </form>
        </div>

        {/* Realtime Formula Preview Card */}
        <div className="p-5 bg-surface border border-border rounded-card flex flex-col justify-between">
          <div>
            <span className="text-xs font-mono uppercase text-text-muted">Instant Legal Preview</span>
            {previewResult ? (
              <div className="mt-3">
                <div className="text-3xl font-bold font-mono text-blue">
                  {previewResult.formattedObligationUnits}
                </div>
                <span className="text-xs text-text-muted font-sans">units obligation to collect</span>

                <div className="mt-4 p-3 rounded-input bg-surface-2 border border-border text-xs font-mono text-text-dim flex flex-col gap-1">
                  <div>Lifespan: <span className="text-text">{previewResult.lifespanYears} years</span></div>
                  <div>Cf Factor: <span className="text-text">{previewResult.conversionFactor}</span></div>
                  <div>Target Quota: <span className="text-text">{Math.round(previewResult.targetPercentage * 100)}%</span></div>
                </div>

                <p className="text-[11px] text-text-muted mt-3 font-mono leading-relaxed">
                  {previewResult.calculationBreakdown}
                </p>
              </div>
            ) : (
              <div className="mt-4 text-xs text-text-dim">
                Enter product units to compute instant obligation breakdown.
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Table of Added Products */}
      <div className="p-5 bg-surface border border-border rounded-card">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-sm font-semibold text-text">Registered Product Portfolio</h3>
          <span className="text-xs font-mono text-text-muted">
            {products.length} registered product line{products.length !== 1 ? 's' : ''}
          </span>
        </div>

        {isLoading ? (
          <div className="flex flex-col gap-3">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : products.length === 0 ? (
          <EmptyState
            title="No Products Registered"
            description="Add your electronics products with units sold to automatically calculate your CPCB EPR recycling obligations."
            actionText="Register Product"
            onAction={() => {
              setProductName('Wireless Earbuds');
              setUnitsSold(500000);
            }}
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-surface-2/60 text-text-muted font-mono uppercase border-b border-border">
                <tr>
                  <th className="p-3">Product Name</th>
                  <th className="p-3">Category</th>
                  <th className="p-3">Units Sold</th>
                  <th className="p-3">Conversion Factor</th>
                  <th className="p-3">Obligation Units</th>
                  <th className="p-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60">
                {products.map((p) => (
                  <tr key={p.productId} className="hover:bg-surface-2/30 transition-colors">
                    <td className="p-3 font-medium text-text">{p.productName}</td>
                    <td className="p-3 text-text-muted">{p.categoryDisplayName}</td>
                    <td className="p-3 font-mono text-text">{p.formattedUnitsSold}</td>
                    <td className="p-3 font-mono text-text-dim">{p.conversionFactor}</td>
                    <td className="p-3 font-mono text-blue font-bold">
                      {p.formattedEprObligationUnits}
                    </td>
                    <td className="p-3 text-right flex justify-end gap-2">
                      <button
                        onClick={() => setEditingProduct(p)}
                        className="text-text-muted hover:text-blue transition-colors px-2 py-1"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => setDeleteId(p.productId)}
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

      {/* Grand Total Obligation Card */}
      <div className="p-6 bg-surface-2 border border-border rounded-card flex flex-col sm:flex-row items-center justify-between gap-4">
        <div>
          <span className="text-xs font-mono uppercase text-text-muted">Portfolio Cumulative Total</span>
          <h4 className="text-lg font-bold text-text mt-1">Total Legal EPR Obligation</h4>
          <p className="text-xs text-text-dim font-mono mt-0.5">
            Total Sales: {formatIndianNumber(totalUnitsSoldAll)} units across {products.length} product lines
          </p>
        </div>

        <div className="text-right">
          <div className="text-3xl font-bold font-mono text-green">
            {formatIndianNumber(totalObligationAll)}
          </div>
          <span className="text-xs text-text-muted font-mono">Total Units to Recycle</span>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={!!deleteId}
        onClose={() => setDeleteId(null)}
        title="Confirm Product Removal"
      >
        <p className="text-sm text-text-muted mb-4">
          Are you sure you want to delete this product? It will be removed from your CPCB compliance portfolio and calculations.
        </p>
        <div className="flex justify-end gap-2">
          <Button variant="secondary" size="sm" onClick={() => setDeleteId(null)}>
            Cancel
          </Button>
          <Button variant="danger" size="sm" onClick={handleDelete}>
            Confirm Delete
          </Button>
        </div>
      </Modal>

      {/* Edit Product Modal */}
      <Modal
        isOpen={!!editingProduct}
        onClose={() => setEditingProduct(null)}
        title="Edit Product Details"
      >
        {editingProduct && (
          <form onSubmit={handleUpdate} className="flex flex-col gap-4">
            <Input
              label="Product Name"
              value={editingProduct.productName}
              onChange={(e) =>
                setEditingProduct({ ...editingProduct, productName: e.target.value })
              }
              required
            />
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-medium text-text-muted">CPCB Category</label>
              <select
                value={editingProduct.productCategory}
                onChange={(e) =>
                  setEditingProduct({ ...editingProduct, productCategory: e.target.value })
                }
                className="w-full bg-surface-2 border border-border rounded-input px-3 py-2 text-sm text-text focus:outline-none focus:border-blue"
              >
                {categories.map((cat) => (
                  <option key={cat.category} value={cat.category}>
                    {cat.displayName}
                  </option>
                ))}
              </select>
            </div>
            <Input
              label="Units Sold"
              type="number"
              value={editingProduct.unitsSold}
              onChange={(e) =>
                setEditingProduct({ ...editingProduct, unitsSold: Number(e.target.value) })
              }
              required
            />
            <div className="flex justify-end gap-2 mt-2">
              <Button variant="secondary" size="sm" onClick={() => setEditingProduct(null)}>
                Cancel
              </Button>
              <Button type="submit" variant="primary" size="sm">
                Save Changes
              </Button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
};
