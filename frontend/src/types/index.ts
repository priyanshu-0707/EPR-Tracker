export interface AuthUser {
  token: string;
  companyId: string;
  companyName: string;
  email: string;
  state: string;
  cpcbRegistrationNumber?: string;
}

export interface RegisterPayload {
  companyName: string;
  email: string;
  state: string;
  password: string;
  cpcbRegistrationNumber?: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface ProductItem {
  productId: string;
  companyId: string;
  productName: string;
  productCategory: string;
  categoryDisplayName: string;
  unitsSold: number;
  formattedUnitsSold: string;
  conversionFactor: number;
  financialYear: string;
  eprObligationUnits: number;
  formattedEprObligationUnits: string;
  createdAt: string;
}

export interface ProductPayload {
  productName: string;
  productCategory: string;
  unitsSold: number;
  financialYear?: string;
}

export interface CategoryItem {
  category: string;
  displayName: string;
  lifespanYears: number;
  conversionFactor: number;
}

export interface EprCalculationResult {
  category: string;
  categoryDisplayName: string;
  unitsSold: number;
  formattedUnitsSold: string;
  lifespanYears: number;
  conversionFactor: number;
  financialYear: string;
  targetPercentage: number;
  obligationUnits: number;
  formattedObligationUnits: string;
  calculationBreakdown: string;
}

export interface EprSummary {
  totalObligationUnits: number;
  formattedTotalObligationUnits: string;
  totalCollectedUnits: number;
  formattedTotalCollectedUnits: string;
  outstandingUnits: number;
  formattedOutstandingUnits: string;
  fulfillmentPercentage: number;
  complianceStatus: 'ON TRACK' | 'AT RISK' | 'NON-COMPLIANT' | string;
  financialYear: string;
}

export interface RecyclerItem {
  recyclerId: string;
  name: string;
  state: string;
  city: string;
  latitude: number;
  longitude: number;
  categoriesHandled: string[];
  cpcbCertified: boolean;
  contactEmail: string;
  contactPhone: string;
  capacityTonnesPerYear: number;
  formattedCapacity: string;
  distanceKm?: number;
}

export interface ComplianceLogItem {
  logId: string;
  companyId: string;
  recyclerId: string;
  recyclerName: string;
  quantityCollectedKg: number;
  formattedQuantityCollectedKg: string;
  eprCertificateNumber: string;
  dateLogged: string;
  financialYear: string;
}

export interface ComplianceLogPayload {
  recyclerId: string;
  recyclerName?: string;
  quantityCollectedKg: number;
  eprCertificateNumber: string;
  dateLogged: string;
  financialYear?: string;
}

export interface MonthlyMetric {
  month: string;
  collectedUnits: number;
  targetUnits: number;
}

export interface DashboardData {
  companyName: string;
  cpcbRegistrationNumber: string;
  state: string;
  financialYear: string;
  totalObligationUnits: number;
  formattedTotalObligationUnits: string;
  totalCollectedUnits: number;
  formattedTotalCollectedUnits: string;
  outstandingUnits: number;
  formattedOutstandingUnits: string;
  daysToYearEnd: number;
  fulfillmentPercentage: number;
  complianceStatus: 'ON TRACK' | 'AT RISK' | 'NON-COMPLIANT' | string;
  monthlyCollections: MonthlyMetric[];
  recentLogs: ComplianceLogItem[];
}

export interface ReportItem {
  reportId: string;
  reportName: string;
  downloadUrl: string;
  generatedAt: string;
  financialYear: string;
  complianceStatus: string;
  fulfillmentPercentage: number;
}

export interface ActiveTrigger {
  name: string;
  description: string;
  frequency: string;
  active: boolean;
}

export interface AlertSettings {
  emailAlertsEnabled: boolean;
  smsAlertsEnabled: boolean;
  alertEmail: string;
  alertPhone: string;
  activeTriggers: ActiveTrigger[];
}

export interface AlertHistoryItem {
  alertId: string;
  alertType: string;
  recipient: string;
  title: string;
  message: string;
  sentAt: string;
  status: string;
}

export interface CompanyProfile {
  companyId: string;
  companyName: string;
  email: string;
  state: string;
  cpcbRegistrationNumber: string;
  registeredOn: string;
  alertsEnabled: boolean;
  alertEmail: string;
  alertPhone: string;
}

// ----------------------------------------------------
// Enterprise Feature 1: "What-If" EC Simulator
// ----------------------------------------------------
export interface EcSimulationPayload {
  customShortfallMt?: number;
  baseRatePerMtInr?: number;
  severityMultiplier?: number;
  delayMonths?: number;
  marketRatePerMtInr?: number;
  financialYear?: string;
}

export interface EcSimulationResult {
  totalObligationUnits: number;
  formattedTotalObligationUnits: string;
  totalCollectedUnits: number;
  formattedTotalCollectedUnits: string;
  shortfallUnits: number;
  formattedShortfallUnits: string;
  shortfallMetricTons: number;
  formattedShortfallMetricTons: string;
  baseRatePerMetricTonInr: number;
  environmentalSeverityMultiplier: number;
  delayPenaltyMultiplier: number;
  estimatedEcPenaltyInr: number;
  formattedEstimatedEcPenaltyInr: string;
  estimatedMarketRatePerMtInr: number;
  estimatedMarketProcurementCostInr: number;
  formattedEstimatedMarketProcurementCostInr: string;
  netFinancialSavingsInr: number;
  formattedNetFinancialSavingsInr: string;
  penaltyToCostRatio: number;
  cfoRoiHeadline: string;
  regulatoryClauseCiting: string;
}

// ----------------------------------------------------
// Enterprise Feature 2: Chain of Custody Validation
// ----------------------------------------------------
export interface CertificateValidationPayload {
  recyclerId: string;
  certificateNumber: string;
  quantityKg: number;
  financialYear?: string;
}

export interface CertificateValidationResult {
  valid: boolean;
  recyclerId: string;
  recyclerName: string;
  certificateNumber: string;
  certificateQuantityKg: number;
  annualCapacityTonnes: number;
  totalUtilizedCapacityTonnes: number;
  remainingCapacityTonnes: number;
  capacityUtilizationPercent: number;
  duplicateAcrossSystem: boolean;
  exceedsCapacityLimit: boolean;
  auditGuaranteeHash: string;
  verificationStatus: 'AUDIT_READY_GUARANTEED' | 'FLAGGED_EXCEEDS_CAPACITY' | 'FLAGGED_DUPLICATE_CERTIFICATE' | string;
  riskLevel: 'NONE' | 'LOW' | 'HIGH' | 'CRITICAL' | string;
  warnings: string[];
}

// ----------------------------------------------------
// Enterprise Feature 3: Gap-Filling Marketplace
// ----------------------------------------------------
export interface CategoryDeficitItem {
  category: string;
  categoryDisplayName: string;
  targetUnits: number;
  collectedUnits: number;
  deficitUnits: number;
  deficitMetricTons: number;
  formattedDeficitMetricTons: string;
}

export interface RecyclerMarketOffer {
  recyclerId: string;
  recyclerName: string;
  state: string;
  city: string;
  matchingCategory: string;
  totalCapacityTonnesPerYear: number;
  availableCreditsTonnes: number;
  ratePerMtInr: number;
  formattedRatePerMtInr: string;
  totalEstimatedCostInr: number;
  formattedTotalEstimatedCostInr: string;
  cpcbCertified: boolean;
  contactEmail: string;
  contactPhone: string;
}

export interface MarketplaceRecommendations {
  companyId: string;
  financialYear: string;
  totalDeficitUnits: number;
  totalDeficitMetricTons: number;
  formattedTotalDeficitMetricTons: string;
  categoryDeficits: CategoryDeficitItem[];
  matchingRecyclerOffers: RecyclerMarketOffer[];
}

export interface MarketplaceOrderPayload {
  recyclerId: string;
  category: string;
  creditsMetricTons: number;
  agreedRatePerMtInr?: number;
  financialYear?: string;
  notes?: string;
}

export interface MarketplaceOrderResult {
  orderId: string;
  companyId: string;
  recyclerId: string;
  recyclerName: string;
  category: string;
  creditsMetricTons: number;
  ratePerMtInr: number;
  totalAmountInr: number;
  formattedTotalAmountInr: string;
  orderStatus: string;
  generatedCertificateNumber: string;
  createdDate: string;
  message: string;
}

// ----------------------------------------------------
// Enterprise Feature 4: Smart Data Ingestion Mapper
// ----------------------------------------------------
export interface IngestionPreviewResult {
  filename: string;
  totalRows: number;
  detectedHeaders: string[];
  previewRows: Array<Record<string, string>>;
  suggestedMapping: Record<string, string>;
}

export interface IngestionImportPayload {
  fileContentBase64?: string;
  filename?: string;
  columnMapping: Record<string, string>;
  saveTemplate: boolean;
  templateName?: string;
}

export interface IngestionImportResult {
  importedCount: number;
  skippedCount: number;
  importedProductNames: string[];
  errors: string[];
  templateSaved?: string;
  message: string;
}

