package com.eprtracker.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EcSimulationRequest {

    /**
     * Optional manual override of shortfall in Metric Tons.
     * If null, system automatically calculates shortfall from active company obligation vs collected credits.
     */
    private Double customShortfallMt;

    /**
     * Regulatory base penalty rate in INR per Metric Ton.
     * CPCB standard default: ₹18,000 / MT.
     */
    @Builder.Default
    private Double baseRatePerMtInr = 18000.0;

    /**
     * Environmental severity multiplier based on e-waste hazardous fraction (1.0 to 2.5).
     * Default: 1.2
     */
    @Builder.Default
    private Double severityMultiplier = 1.2;

    /**
     * Delay in months past CPCB deadline (causes compound interest/penalty).
     * Rule 22 introduces a 12% annual (1% monthly) escalation for non-compliance window.
     */
    @Builder.Default
    private Integer delayMonths = 0;

    /**
     * Estimated market procurement rate per Metric Ton credit.
     * Default current Indian EPR market benchmark: ₹4,500 / MT.
     */
    @Builder.Default
    private Double marketRatePerMtInr = 4500.0;

    /**
     * Target Financial Year (e.g. 2026-27).
     */
    @Builder.Default
    private String financialYear = "2026-27";
}
