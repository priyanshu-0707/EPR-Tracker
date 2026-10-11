package com.eprtracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EprCalculationResponse {
    private String category;
    private String categoryDisplayName;
    private Long unitsSold;
    private String formattedUnitsSold;
    private int lifespanYears;
    private double conversionFactor;
    private String financialYear;
    private double targetPercentage;
    private Long obligationUnits;
    private String formattedObligationUnits;
    private String calculationBreakdown;
}
