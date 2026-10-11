package com.eprtracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EprSummaryResponse {
    private Long totalObligationUnits;
    private String formattedTotalObligationUnits;
    private Long totalCollectedUnits;
    private String formattedTotalCollectedUnits;
    private Long outstandingUnits;
    private String formattedOutstandingUnits;
    private double fulfillmentPercentage;
    private String complianceStatus; // ON TRACK, AT RISK, NON-COMPLIANT
    private String financialYear;
}
