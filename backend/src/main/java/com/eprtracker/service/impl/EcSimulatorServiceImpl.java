package com.eprtracker.service.impl;

import com.eprtracker.dto.request.EcSimulationRequest;
import com.eprtracker.dto.response.EcSimulationResponse;
import com.eprtracker.dto.response.EprSummaryResponse;
import com.eprtracker.service.EcSimulatorService;
import com.eprtracker.service.EprCalculatorService;
import com.eprtracker.util.IndianNumberFormatter;
import org.springframework.stereotype.Service;

@Service
public class EcSimulatorServiceImpl implements EcSimulatorService {

    private final EprCalculatorService eprCalculatorService;

    public EcSimulatorServiceImpl(EprCalculatorService eprCalculatorService) {
        this.eprCalculatorService = eprCalculatorService;
    }

    @Override
    public EcSimulationResponse simulateLiability(String companyId, EcSimulationRequest request) {
        String fy = (request != null && request.getFinancialYear() != null && !request.getFinancialYear().isBlank())
                ? request.getFinancialYear() : "2026-27";

        EprSummaryResponse summary = eprCalculatorService.getSummary(companyId, fy);
        long totalObligation = summary != null && summary.getTotalObligationUnits() != null ? summary.getTotalObligationUnits() : 0L;
        long totalCollected = summary != null && summary.getTotalCollectedUnits() != null ? summary.getTotalCollectedUnits() : 0L;

        long calculatedShortfallUnits = Math.max(0L, totalObligation - totalCollected);
        // Average weight per typical mixed EEE unit is ~1.25 kg (or 0.00125 MT per unit)
        double estimatedShortfallMt = calculatedShortfallUnits * 0.00125;

        // Custom override if user adjusts the slider or specifies an exact MT deficit
        if (request != null && request.getCustomShortfallMt() != null && request.getCustomShortfallMt() >= 0) {
            estimatedShortfallMt = request.getCustomShortfallMt();
            calculatedShortfallUnits = Math.round(estimatedShortfallMt / 0.00125);
        }

        double baseRate = (request != null && request.getBaseRatePerMtInr() != null && request.getBaseRatePerMtInr() > 0)
                ? request.getBaseRatePerMtInr() : 18000.0;

        double severity = (request != null && request.getSeverityMultiplier() != null && request.getSeverityMultiplier() > 0)
                ? request.getSeverityMultiplier() : 1.2;

        int delayMonths = (request != null && request.getDelayMonths() != null && request.getDelayMonths() >= 0)
                ? request.getDelayMonths() : 0;

        // CPCB Escalation: 1.0 + (1% per month overdue under CPCB Environmental Compensation guidelines)
        double delayMultiplier = 1.0 + (delayMonths * 0.01);

        // Environmental Compensation Formula: Shortfall(MT) * BaseRate * Severity * DelayMultiplier
        long estimatedEcPenalty = Math.round(estimatedShortfallMt * baseRate * severity * delayMultiplier);

        // Market Credit Alternative
        double marketRate = (request != null && request.getMarketRatePerMtInr() != null && request.getMarketRatePerMtInr() > 0)
                ? request.getMarketRatePerMtInr() : 4500.0;

        long marketProcurementCost = Math.round(estimatedShortfallMt * marketRate);
        long netSavings = Math.max(0L, estimatedEcPenalty - marketProcurementCost);
        double penaltyToCostRatio = marketProcurementCost > 0
                ? Math.round(((double) estimatedEcPenalty / marketProcurementCost) * 10.0) / 10.0
                : (estimatedEcPenalty > 0 ? 4.8 : 1.0);

        String cfoHeadline = String.format(
                "Spending ₹%s on credits immediately prevents a ₹%s regulatory penalty from CPCB (ROI: %.1fx Savings)",
                IndianNumberFormatter.format(marketProcurementCost),
                IndianNumberFormatter.format(estimatedEcPenalty),
                penaltyToCostRatio
        );

        String regulatoryClause = "Under Rule 22 & Schedule VI of E-Waste (Management) Rules 2022, failure to meet annual collection targets attracts non-compoundable Environmental Compensation (EC) assessed at ₹18,000/MT plus severity surcharges. Payment of EC does not absolve the producer from the ongoing obligation.";

        return EcSimulationResponse.builder()
                .totalObligationUnits(totalObligation)
                .formattedTotalObligationUnits(IndianNumberFormatter.format(totalObligation))
                .totalCollectedUnits(totalCollected)
                .formattedTotalCollectedUnits(IndianNumberFormatter.format(totalCollected))
                .shortfallUnits(calculatedShortfallUnits)
                .formattedShortfallUnits(IndianNumberFormatter.format(calculatedShortfallUnits))
                .shortfallMetricTons(Math.round(estimatedShortfallMt * 100.0) / 100.0)
                .formattedShortfallMetricTons(IndianNumberFormatter.format(estimatedShortfallMt) + " MT")
                .baseRatePerMetricTonInr(baseRate)
                .environmentalSeverityMultiplier(severity)
                .delayPenaltyMultiplier(delayMultiplier)
                .estimatedEcPenaltyInr(estimatedEcPenalty)
                .formattedEstimatedEcPenaltyInr("₹" + IndianNumberFormatter.format(estimatedEcPenalty))
                .estimatedMarketRatePerMtInr(marketRate)
                .estimatedMarketProcurementCostInr(marketProcurementCost)
                .formattedEstimatedMarketProcurementCostInr("₹" + IndianNumberFormatter.format(marketProcurementCost))
                .netFinancialSavingsInr(netSavings)
                .formattedNetFinancialSavingsInr("₹" + IndianNumberFormatter.format(netSavings))
                .penaltyToCostRatio(penaltyToCostRatio)
                .cfoRoiHeadline(cfoHeadline)
                .regulatoryClauseCiting(regulatoryClause)
                .build();
    }
}
