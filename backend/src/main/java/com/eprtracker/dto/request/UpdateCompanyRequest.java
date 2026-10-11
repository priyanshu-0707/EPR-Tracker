package com.eprtracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompanyRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "State is required")
    private String state;

    private String cpcbRegistrationNumber;
    private String alertEmail;
    private String alertPhone;
    private Boolean alertsEnabled;
}
