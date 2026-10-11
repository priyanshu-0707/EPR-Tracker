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
public class MarketplaceRecommendationResponse {
    private String companyId;
    private String financialYear;
    private Long totalDeficitUnits;
    private Double totalDeficitMetricTons;
    private String formattedTotalDeficitMetricTons;
    private List<CategoryDeficitItem> categoryDeficits;
    private List<RecyclerMarketOffer> matchingRecyclerOffers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDeficitItem {
        private String category;
        private String categoryDisplayName;
        private Long targetUnits;
        private Long collectedUnits;
        private Long deficitUnits;
        private Double deficitMetricTons;
        private String formattedDeficitMetricTons;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecyclerMarketOffer {
        private String recyclerId;
        private String recyclerName;
        private String state;
        private String city;
        private String matchingCategory;
        private Integer totalCapacityTonnesPerYear;
        private Double availableCreditsTonnes;
        private Double ratePerMtInr;
        private String formattedRatePerMtInr;
        private Double totalEstimatedCostInr;
        private String formattedTotalEstimatedCostInr;
        private Boolean cpcbCertified;
        private String contactEmail;
        private String contactPhone;
    }
}
