package com.eprtracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceOrderResponse {
    private String orderId;
    private String companyId;
    private String recyclerId;
    private String recyclerName;
    private String category;
    private Double creditsMetricTons;
    private Double ratePerMtInr;
    private Double totalAmountInr;
    private String formattedTotalAmountInr;
    private String orderStatus; // "PENDING_CONFIRMATION", "SETTLED", "CERTIFICATE_ISSUED"
    private String generatedCertificateNumber;
    private String createdDate;
    private String message;
}
