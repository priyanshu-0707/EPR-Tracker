package com.eprtracker.service.impl;

import com.eprtracker.dto.request.UpdateCompanyRequest;
import com.eprtracker.dto.response.CompanyResponse;
import com.eprtracker.dto.response.ComplianceLogResponse;
import com.eprtracker.dto.response.DashboardResponse;
import com.eprtracker.dto.response.EprSummaryResponse;
import com.eprtracker.entity.Company;
import com.eprtracker.exception.ResourceNotFoundException;
import com.eprtracker.repository.CompanyRepository;
import com.eprtracker.service.CompanyService;
import com.eprtracker.service.ComplianceLogService;
import com.eprtracker.service.EprCalculatorService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final EprCalculatorService eprCalculatorService;
    private final ComplianceLogService complianceLogService;

    public CompanyServiceImpl(
            CompanyRepository companyRepository,
            EprCalculatorService eprCalculatorService,
            ComplianceLogService complianceLogService) {
        this.companyRepository = companyRepository;
        this.eprCalculatorService = eprCalculatorService;
        this.complianceLogService = complianceLogService;
    }

    @Override
    public CompanyResponse getProfile(String companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
        return mapToResponse(company);
    }

    @Override
    public CompanyResponse updateProfile(String companyId, UpdateCompanyRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        company.setCompanyName(request.getCompanyName());
        company.setState(request.getState());
        if (request.getCpcbRegistrationNumber() != null) {
            company.setCpcbRegistrationNumber(request.getCpcbRegistrationNumber());
        }
        if (request.getAlertEmail() != null) {
            company.setAlertEmail(request.getAlertEmail());
        }
        if (request.getAlertPhone() != null) {
            company.setAlertPhone(request.getAlertPhone());
        }
        if (request.getAlertsEnabled() != null) {
            company.setAlertsEnabled(request.getAlertsEnabled());
        }

        companyRepository.save(company);
        return mapToResponse(company);
    }

    @Override
    public DashboardResponse getDashboard(String companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        String currentFy = "2026-27";
        EprSummaryResponse summary = eprCalculatorService.getSummary(companyId, currentFy);

        // Calculate days to Indian Financial Year End (March 31, 2027)
        LocalDate today = LocalDate.now();
        LocalDate fyEnd = LocalDate.of(2027, Month.MARCH, 31);
        long daysRemaining = Math.max(0, ChronoUnit.DAYS.between(today, fyEnd));

        // Get recent logs
        List<ComplianceLogResponse> allLogs = complianceLogService.getLogsByCompany(companyId, currentFy);
        List<ComplianceLogResponse> recentLogs = allLogs.stream().limit(5).toList();

        // Build monthly metrics for Recharts
        List<DashboardResponse.MonthlyMetric> monthly = new ArrayList<>();
        String[] months = {"Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar"};
        long monthlyTarget = summary.getTotalObligationUnits() > 0 ? summary.getTotalObligationUnits() / 12 : 10000;

        for (int i = 0; i < months.length; i++) {
            long collectedPortion = (i < 7 && summary.getTotalCollectedUnits() > 0)
                    ? Math.round(summary.getTotalCollectedUnits() * ((double) (i + 1) / 28))
                    : 0;
            monthly.add(new DashboardResponse.MonthlyMetric(months[i], collectedPortion, monthlyTarget));
        }

        return DashboardResponse.builder()
                .companyName(company.getCompanyName())
                .cpcbRegistrationNumber(company.getCpcbRegistrationNumber())
                .state(company.getState())
                .financialYear(currentFy)
                .totalObligationUnits(summary.getTotalObligationUnits())
                .formattedTotalObligationUnits(summary.getFormattedTotalObligationUnits())
                .totalCollectedUnits(summary.getTotalCollectedUnits())
                .formattedTotalCollectedUnits(summary.getFormattedTotalCollectedUnits())
                .outstandingUnits(summary.getOutstandingUnits())
                .formattedOutstandingUnits(summary.getFormattedOutstandingUnits())
                .daysToYearEnd(daysRemaining)
                .fulfillmentPercentage(summary.getFulfillmentPercentage())
                .complianceStatus(summary.getComplianceStatus())
                .monthlyCollections(monthly)
                .recentLogs(recentLogs)
                .build();
    }

    @Override
    public void deleteCompany(String companyId) {
        companyRepository.deleteById(companyId);
    }

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .email(company.getEmail())
                .state(company.getState())
                .cpcbRegistrationNumber(company.getCpcbRegistrationNumber())
                .registeredOn(company.getRegisteredOn())
                .alertsEnabled(company.getAlertsEnabled())
                .alertEmail(company.getAlertEmail())
                .alertPhone(company.getAlertPhone())
                .build();
    }
}
