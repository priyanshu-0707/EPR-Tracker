package com.eprtracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {
    private String companyId;
    private String companyName;
    private String email;
    private String state;
    private String cpcbRegistrationNumber;
    private String registeredOn;
    private Boolean alertsEnabled;
    private String alertEmail;
    private String alertPhone;
}
