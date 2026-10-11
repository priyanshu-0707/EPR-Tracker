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
public class ComplianceLogRequest {

    @NotBlank(message = "Recycler ID is required")
    private String recyclerId;

    private String recyclerName;

    @NotNull(message = "Quantity collected in kg is required")
    @Positive(message = "Quantity collected must be greater than zero")
    private Double quantityCollectedKg;

    @NotBlank(message = "EPR certificate number is required")
    private String eprCertificateNumber;

    @NotBlank(message = "Date logged is required")
    private String dateLogged;

    private String financialYear; // Defaults to current
}
