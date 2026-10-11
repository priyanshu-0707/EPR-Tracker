package com.eprtracker.controller;

import com.eprtracker.dto.request.UpdateCompanyRequest;
import com.eprtracker.dto.response.CompanyResponse;
import com.eprtracker.dto.response.DashboardResponse;
import com.eprtracker.security.JwtTokenProvider;
import com.eprtracker.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller managing corporate profile, CPCB registration, and overall compliance dashboard.
 */
@RestController
@RequestMapping("/api/company")
public class CompanyController {

    private final CompanyService companyService;
    private final JwtTokenProvider tokenProvider;

    public CompanyController(CompanyService companyService, JwtTokenProvider tokenProvider) {
        this.companyService = companyService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Retrieve enterprise profile and contact configurations.
     */
    @GetMapping("/profile")
    public ResponseEntity<CompanyResponse> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        return ResponseEntity.ok(companyService.getProfile(companyId));
    }

    /**
     * Update enterprise profile details.
     */
    @PutMapping("/profile")
    public ResponseEntity<CompanyResponse> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateCompanyRequest request) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        return ResponseEntity.ok(companyService.updateProfile(companyId, request));
    }

    /**
     * Retrieve full analytics dashboard including 4 KPI counters, fulfillment badge and trend charts.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        return ResponseEntity.ok(companyService.getDashboard(companyId));
    }

    /**
     * Remove company account and associated records.
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteCompany(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        companyService.deleteCompany(companyId);
        return ResponseEntity.noContent().build();
    }
}
