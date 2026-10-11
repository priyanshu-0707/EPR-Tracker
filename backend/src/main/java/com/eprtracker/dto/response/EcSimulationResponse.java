package com.eprtracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcSimulationResponse {
    // Current Enterprise Obligation State
    private Long totalObligationUnits;
    private String formattedTotalObligationUnits;
    private Long totalCollectedUnits;
    private String formattedTotalCollectedUnits;
    private Long shortfallUnits;
    private String formattedShortfallUnits;
    private Double shortfallMetricTons;
    private String formattedShortfallMetricTons;

    // Environmental Compensation (Penalty) Model
    private Double baseRatePerMetricTonInr;
    private Double environmentalSeverityMultiplier;
    private Double delayPenaltyMultiplier;
    private Long estimatedEcPenaltyInr;
    private String formattedEstimatedEcPenaltyInr;

    // Marketplace Alternative (ROI)
    private Double estimatedMarketRatePerMtInr;
    private Long estimatedMarketProcurementCostInr;
    private String formattedEstimatedMarketProcurementCostInr;

    // Executive Value Justification (ROI Metric)
    private Long netFinancialSavingsInr;
    private String formattedNetFinancialSavingsInr;
    private Double penaltyToCostRatio;
    private String cfoRoiHeadline;
    private String regulatoryClauseCiting;
}
