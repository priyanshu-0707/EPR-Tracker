package com.eprtracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceLogResponse {
    private String logId;
    private String companyId;
    private String recyclerId;
    private String recyclerName;
    private Double quantityCollectedKg;
    private String formattedQuantityCollectedKg;
    private String eprCertificateNumber;
    private String dateLogged;
    private String financialYear;
}
