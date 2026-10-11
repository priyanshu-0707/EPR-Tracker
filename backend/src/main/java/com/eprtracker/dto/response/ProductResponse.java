package com.eprtracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private String productId;
    private String companyId;
    private String productName;
    private String productCategory;
    private String categoryDisplayName;
    private Long unitsSold;
    private String formattedUnitsSold;
    private Double conversionFactor;
    private String financialYear;
    private Long eprObligationUnits;
    private String formattedEprObligationUnits;
    private String createdAt;
}
