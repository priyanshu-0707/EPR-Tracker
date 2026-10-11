package com.eprtracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private String reportId;
    private String reportName;
    private String downloadUrl;
    private String generatedAt;
    private String financialYear;
    private String complianceStatus;
    private double fulfillmentPercentage;
}
