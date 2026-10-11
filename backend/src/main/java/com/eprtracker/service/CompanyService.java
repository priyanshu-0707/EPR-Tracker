package com.eprtracker.service;

import com.eprtracker.dto.request.UpdateCompanyRequest;
import com.eprtracker.dto.response.CompanyResponse;
import com.eprtracker.dto.response.DashboardResponse;

public interface CompanyService {
    CompanyResponse getProfile(String companyId);
    CompanyResponse updateProfile(String companyId, UpdateCompanyRequest request);
    DashboardResponse getDashboard(String companyId);
    void deleteCompany(String companyId);
}
