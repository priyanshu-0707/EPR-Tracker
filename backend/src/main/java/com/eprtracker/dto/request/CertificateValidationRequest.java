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
public class CertificateValidationRequest {

    @NotBlank(message = "Recycler ID is required")
    private String recyclerId;

    @NotBlank(message = "Certificate number is required")
    private String certificateNumber;

    @NotNull(message = "Quantity in kg is required")
    @Positive(message = "Quantity must be greater than zero")
    private Double quantityKg;

    private String financialYear;
}
