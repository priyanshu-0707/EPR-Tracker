import { api, setAuthTokenInMemory } from './api';
import {
  AuthUser,
  RegisterPayload,
  LoginPayload,
  DashboardData,
  ProductItem,
  ProductPayload,
  CategoryItem,
  EprCalculationResult,
  EprSummary,
  RecyclerItem,
  ComplianceLogItem,
  ComplianceLogPayload,
  ReportItem,
  AlertSettings,
  AlertHistoryItem,
  CompanyProfile,
} from '../types';

export const authService = {
  login: async (payload: LoginPayload): Promise<AuthUser> => {
    const res = await api.post<AuthUser>('/api/auth/login', payload);
    setAuthTokenInMemory(res.data.token);
    return res.data;
  },
  register: async (payload: RegisterPayload): Promise<AuthUser> => {
    const res = await api.post<AuthUser>('/api/auth/register', payload);
    setAuthTokenInMemory(res.data.token);
    return res.data;
  },
  logout: () => {
    setAuthTokenInMemory(null);
  },
};

export const dashboardService = {
  getDashboard: async (): Promise<DashboardData> => {
    const res = await api.get<DashboardData>('/api/company/dashboard');
    return res.data;
  },
};

export const productService = {
  getCategories: async (): Promise<CategoryItem[]> => {
    const res = await api.get<CategoryItem[]>('/api/products/categories');
    return res.data;
  },
  getProducts: async (): Promise<ProductItem[]> => {
    const res = await api.get<ProductItem[]>('/api/products');
    return res.data;
  },
  createProduct: async (payload: ProductPayload): Promise<ProductItem> => {
    const res = await api.post<ProductItem>('/api/products', payload);
    return res.data;
  },
  updateProduct: async (id: string, payload: ProductPayload): Promise<ProductItem> => {
    const res = await api.put<ProductItem>(`/api/products/${id}`, payload);
    return res.data;
  },
  deleteProduct: async (id: string): Promise<void> => {
    await api.delete(`/api/products/${id}`);
  },
};

export const eprService = {
  calculate: async (payload: { category: string; unitsSold: number; financialYear?: string }): Promise<EprCalculationResult> => {
    const res = await api.post<EprCalculationResult>('/api/epr/calculate', payload);
    return res.data;
  },
  getSummary: async (financialYear?: string): Promise<EprSummary> => {
    const res = await api.get<EprSummary>('/api/epr/summary', { params: { financialYear } });
    return res.data;
  },
};

export const recyclerService = {
  getRecyclers: async (state?: string, category?: string): Promise<RecyclerItem[]> => {
    const res = await api.get<RecyclerItem[]>('/api/recyclers', { params: { state, category } });
    return res.data;
  },
  getNearby: async (lat: number, lng: number, radiusKm?: number): Promise<RecyclerItem[]> => {
    const res = await api.get<RecyclerItem[]>('/api/recyclers/nearby', { params: { lat, lng, radiusKm } });
    return res.data;
  },
};

export const complianceLogService = {
  getLogs: async (financialYear?: string): Promise<ComplianceLogItem[]> => {
    const res = await api.get<ComplianceLogItem[]>('/api/compliance-log', { params: { financialYear } });
    return res.data;
  },
  createLog: async (payload: ComplianceLogPayload): Promise<ComplianceLogItem> => {
    const res = await api.post<ComplianceLogItem>('/api/compliance-log', payload);
    return res.data;
  },
  deleteLog: async (id: string): Promise<void> => {
    await api.delete(`/api/compliance-log/${id}`);
  },
  exportCsvUrl: (): string => {
    return `${api.defaults.baseURL}/api/compliance-log/export`;
  },
};

export const reportService = {
  generateReport: async (financialYear?: string): Promise<ReportItem> => {
    const res = await api.post<ReportItem>('/api/reports/generate', { financialYear });
    return res.data;
  },
  getHistory: async (): Promise<ReportItem[]> => {
    const res = await api.get<ReportItem[]>('/api/reports/history');
    return res.data;
  },
  deleteReport: async (reportId: string): Promise<void> => {
    await api.delete(`/api/reports/${reportId}`);
  },
};

export const aiService = {
  askAssistant: async (message: string): Promise<{ reply: string; modelUsed: string; timestamp: string }> => {
    const res = await api.post<{ reply: string; modelUsed: string; timestamp: string }>('/api/ai/chat', { message });
    return res.data;
  },
};

export const alertService = {
  getSettings: async (): Promise<AlertSettings> => {
    const res = await api.get<AlertSettings>('/api/alerts/settings');
    return res.data;
  },
  updateSettings: async (payload: Partial<AlertSettings>): Promise<AlertSettings> => {
    const res = await api.put<AlertSettings>('/api/alerts/settings', payload);
    return res.data;
  },
  getHistory: async (): Promise<AlertHistoryItem[]> => {
    const res = await api.get<AlertHistoryItem[]>('/api/alerts/history');
    return res.data;
  },
};

export const companyService = {
  getProfile: async (): Promise<CompanyProfile> => {
    const res = await api.get<CompanyProfile>('/api/company/profile');
    return res.data;
  },
  updateProfile: async (payload: Partial<CompanyProfile>): Promise<CompanyProfile> => {
    const res = await api.put<CompanyProfile>('/api/company/profile', payload);
    return res.data;
  },
  deleteAccount: async (): Promise<void> => {
    await api.delete('/api/company');
  },
};

export const simulatorService = {
  simulateEcLiability: async (payload: import('../types').EcSimulationPayload): Promise<import('../types').EcSimulationResult> => {
    const res = await api.post<import('../types').EcSimulationResult>('/api/simulator/ec-liability', payload);
    return res.data;
  },
};

export const marketplaceService = {
  getRecommendations: async (financialYear?: string): Promise<import('../types').MarketplaceRecommendations> => {
    const res = await api.get<import('../types').MarketplaceRecommendations>('/api/marketplace/recommendations', {
      params: { financialYear },
    });
    return res.data;
  },
  placeOrder: async (payload: import('../types').MarketplaceOrderPayload): Promise<import('../types').MarketplaceOrderResult> => {
    const res = await api.post<import('../types').MarketplaceOrderResult>('/api/marketplace/order', payload);
    return res.data;
  },
};

export const ingestionService = {
  previewFile: async (file: File): Promise<import('../types').IngestionPreviewResult> => {
    const formData = new FormData();
    formData.append('file', file);
    const res = await api.post<import('../types').IngestionPreviewResult>('/api/ingestion/preview', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
  },
  mapAndImport: async (payload: import('../types').IngestionImportPayload): Promise<import('../types').IngestionImportResult> => {
    const res = await api.post<import('../types').IngestionImportResult>('/api/ingestion/import', payload);
    return res.data;
  },
  getTemplate: async (): Promise<Record<string, string>> => {
    const res = await api.get<Record<string, string>>('/api/ingestion/template');
    return res.data;
  },
};

export const validationService = {
  validateCertificate: async (payload: import('../types').CertificateValidationPayload): Promise<import('../types').CertificateValidationResult> => {
    const res = await api.post<import('../types').CertificateValidationResult>('/api/compliance-log/validate-certificate', payload);
    return res.data;
  },
};

