import React, { useState } from 'react';
import { ingestionService } from '../services';
import { IngestionPreviewResult, IngestionImportResult } from '../types';

export const DataIngestionPage: React.FC = () => {
  const [preview, setPreview] = useState<IngestionPreviewResult | null>(null);
  const [columnMapping, setColumnMapping] = useState<Record<string, string>>({});
  const [saveTemplate, setSaveTemplate] = useState<boolean>(true);
  const [loading, setLoading] = useState<boolean>(false);
  const [importResult, setImportResult] = useState<IngestionImportResult | null>(null);

  const TARGET_FIELDS = [
    { key: 'productName', label: 'Product Name / Description *' },
    { key: 'category', label: 'EEE Category Code (e.g. ITEW1) *' },
    { key: 'unitsSold', label: 'Sales Volume (Units Sold) *' },
    { key: 'financialYear', label: 'Financial Year (e.g. 2026-27)' },
    { key: 'weightKg', label: 'Weight per Unit (kg)' },
  ];

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const selected = e.target.files?.[0];
    if (!selected) return;
    setLoading(true);
    setImportResult(null);

    try {
      const data = await ingestionService.previewFile(selected);
      setPreview(data);
      // Pre-fill suggested mappings
      setColumnMapping(data.suggestedMapping || {});
    } catch (err: any) {
      alert('Failed to preview file: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleMappingChange = (sourceHeader: string, targetField: string) => {
    setColumnMapping((prev) => {
      const updated = { ...prev };
      if (!targetField) {
        delete updated[sourceHeader];
      } else {
        updated[sourceHeader] = targetField;
      }
      return updated;
    });
  };

  const handleRunImport = async () => {
    if (!preview) return;
    setLoading(true);

    try {
      const result = await ingestionService.mapAndImport({
        filename: preview.filename,
        columnMapping,
        saveTemplate,
        templateName: 'Default ERP Schema',
      });
      setImportResult(result);
    } catch (err: any) {
      alert('Import failed: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-4 md:p-8 max-w-7xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-border">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xl">⚡</span>
            <h1 className="text-xl font-bold tracking-tight text-text">
              Smart Data Ingestion & Schema Mapper
            </h1>
            <span className="text-[10px] uppercase font-mono px-2 py-0.5 rounded bg-blue/10 text-blue border border-blue/30">
              SAP / Oracle / Custom ERP
            </span>
          </div>
          <p className="text-xs text-text-muted mt-1">
            Zero manual entry: upload raw ERP spreadsheets and visually map custom column headers to CPCB legal schema.
          </p>
        </div>
      </div>

      {/* Upload Box */}
      <div className="bg-surface border border-border rounded-card p-6">
        <div className="border-2 border-dashed border-border hover:border-blue/50 rounded-card p-8 text-center space-y-3 transition-colors bg-surface-2/30">
          <span className="text-3xl block">📁</span>
          <div className="space-y-1">
            <p className="text-xs font-semibold text-text">
              Upload SAP, Oracle, or Custom Sales Spreadsheet
            </p>
            <p className="text-[11px] text-text-muted font-mono">
              Supports .csv, .xlsx, .xls exports
            </p>
          </div>
          <label className="inline-block px-4 py-2 bg-blue hover:bg-blue-dark text-white rounded-input text-xs font-mono font-medium cursor-pointer transition-colors shadow">
            <span>Browse Spreadsheet</span>
            <input
              type="file"
              accept=".csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
              onChange={handleFileUpload}
              className="hidden"
            />
          </label>
        </div>
      </div>

      {/* Preview & Mapping Grid */}
      {preview && (
        <div className="space-y-6">
          {/* Mapping Controls */}
          <div className="bg-surface border border-border rounded-card p-5 space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-border">
              <div>
                <h3 className="text-xs font-mono uppercase text-text font-bold">
                  Dynamic Column Schema Mapping
                </h3>
                <p className="text-[11px] text-text-dim">
                  Match your raw ERP columns (left) with official CPCB target fields (right).
                </p>
              </div>
              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="saveTemplate"
                  checked={saveTemplate}
                  onChange={(e) => setSaveTemplate(e.target.checked)}
                  className="rounded accent-blue"
                />
                <label htmlFor="saveTemplate" className="text-xs text-text font-mono cursor-pointer">
                  Save as Company Template
                </label>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {preview.detectedHeaders.map((header) => {
                const currentMapped = columnMapping[header] || '';
                return (
                  <div
                    key={header}
                    className="p-3 bg-surface-2 rounded-input border border-border flex items-center justify-between gap-3 text-xs"
                  >
                    <div className="font-mono text-text font-medium truncate max-w-[180px]">
                      {header}
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-text-dim font-mono text-[10px]">→</span>
                      <select
                        value={currentMapped}
                        onChange={(e) => handleMappingChange(header, e.target.value)}
                        className={`bg-surface border rounded-input px-2.5 py-1.5 text-xs font-mono focus:outline-none ${
                          currentMapped ? 'border-blue text-blue font-semibold' : 'border-border text-text-dim'
                        }`}
                      >
                        <option value="">(Ignore Column)</option>
                        {TARGET_FIELDS.map((tf) => (
                          <option key={tf.key} value={tf.key}>
                            {tf.label}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Execute Button */}
            <div className="flex justify-end pt-3 border-t border-border">
              <button
                disabled={loading}
                onClick={handleRunImport}
                className="px-6 py-2.5 bg-blue hover:bg-blue-dark text-white rounded-input text-xs font-mono font-bold transition-colors shadow flex items-center gap-2"
              >
                {loading ? 'Ingesting Data...' : '⚡ Ingest & Convert to CPCB Schema'}
              </button>
            </div>
          </div>

          {/* Import Result Feedback */}
          {importResult && (
            <div className="p-4 bg-green/10 border border-green/30 rounded-card space-y-2 text-xs">
              <div className="flex items-center gap-2 text-green font-mono font-bold">
                <span>🎉</span>
                <span>{importResult.message}</span>
              </div>
              <p className="text-text-muted text-[11px]">
                Imported: <strong>{importResult.importedCount} products</strong>. Saved schema will auto-map subsequent monthly uploads.
              </p>
            </div>
          )}

          {/* Raw File Preview Table */}
          <div className="bg-surface border border-border rounded-card overflow-hidden">
            <div className="p-4 border-b border-border">
              <h4 className="text-xs font-mono uppercase text-text font-bold">
                Data Preview ({preview.filename} • {preview.totalRows} rows)
              </h4>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-surface-2/60 text-text-dim font-mono uppercase text-[10px] border-b border-border">
                  <tr>
                    {preview.detectedHeaders.map((h) => (
                      <th key={h} className="p-3 whitespace-nowrap">
                        {h}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-border font-sans font-mono text-[11px]">
                  {preview.previewRows.map((row, idx) => (
                    <tr key={idx} className="hover:bg-surface-2/40">
                      {preview.detectedHeaders.map((h) => (
                        <td key={h} className="p-3 whitespace-nowrap text-text-muted">
                          {row[h] || '—'}
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
