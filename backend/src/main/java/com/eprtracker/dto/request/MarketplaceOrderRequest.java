package com.eprtracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceOrderRequest {

    @NotBlank(message = "Recycler ID is required")
    private String recyclerId;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Credits in Metric Tons is required")
    @Positive(message = "Credits must be positive")
    private Double creditsMetricTons;

    private Double agreedRatePerMtInr;
    private String financialYear;
    private String notes;
}
