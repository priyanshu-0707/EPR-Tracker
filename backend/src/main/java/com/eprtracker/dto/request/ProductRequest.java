package com.eprtracker.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotBlank(message = "Product category is required")
    private String productCategory;

    @NotNull(message = "Units sold is required")
    @Min(value = 1, message = "Units sold must be at least 1")
    private Long unitsSold;

    private String financialYear; // Defaults to 2026-27
}
