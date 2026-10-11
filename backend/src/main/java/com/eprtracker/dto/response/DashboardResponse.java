package com.eprtracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private String companyName;
    private String cpcbRegistrationNumber;
    private String state;
    private String financialYear;

    // 4 Primary KPI Stats
    private Long totalObligationUnits;
    private String formattedTotalObligationUnits;
    private Long totalCollectedUnits;
    private String formattedTotalCollectedUnits;
    private Long outstandingUnits;
    private String formattedOutstandingUnits;
    private Long daysToYearEnd;

    // Status Badge & Progress
    private double fulfillmentPercentage;
    private String complianceStatus; // ON TRACK (green >70%), AT RISK (amber 40-70%), NON-COMPLIANT (red <40%)

    // Monthly Bar Chart Data (Recharts)
    private List<MonthlyMetric> monthlyCollections;

    // Recent 5 entries
    private List<ComplianceLogResponse> recentLogs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyMetric {
        private String month;
        private Long collectedUnits;
        private Long targetUnits;
    }
}
