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
public class CertificateValidationResponse {
    private boolean valid;
    private String recyclerId;
    private String recyclerName;
    private String certificateNumber;
    private Double certificateQuantityKg;
    private Integer annualCapacityTonnes;
    private Double totalUtilizedCapacityTonnes;
    private Double remainingCapacityTonnes;
    private Double capacityUtilizationPercent;
    private boolean duplicateAcrossSystem;
    private boolean exceedsCapacityLimit;
    private String auditGuaranteeHash;
    private String verificationStatus; // e.g. "AUDIT_READY_GUARANTEED", "FLAGGED_EXCEEDS_CAPACITY", "FLAGGED_DUPLICATE_CERTIFICATE"
    private String riskLevel; // "NONE", "LOW", "CRITICAL"
    private List<String> warnings;
}
