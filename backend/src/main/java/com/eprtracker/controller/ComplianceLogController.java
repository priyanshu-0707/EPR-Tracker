package com.eprtracker.controller;

import com.eprtracker.dto.request.ComplianceLogRequest;
import com.eprtracker.dto.response.ComplianceLogResponse;
import com.eprtracker.security.JwtTokenProvider;
import com.eprtracker.service.ComplianceLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller managing official e-waste collection and recycler compliance certificate logs.
 */
@RestController
@RequestMapping("/api/compliance-log")
public class ComplianceLogController {

    private final ComplianceLogService complianceLogService;
    private final JwtTokenProvider tokenProvider;

    public ComplianceLogController(ComplianceLogService complianceLogService, JwtTokenProvider tokenProvider) {
        this.complianceLogService = complianceLogService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Retrieve compliance logs for current company and financial year.
     */
    @GetMapping
    public ResponseEntity<List<ComplianceLogResponse>> getLogs(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "financialYear", required = false) String financialYear) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        return ResponseEntity.ok(complianceLogService.getLogsByCompany(companyId, financialYear));
    }

    /**
     * Record new recycled e-waste collection certificate.
     */
    @PostMapping
    public ResponseEntity<ComplianceLogResponse> createLog(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ComplianceLogRequest request) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        ComplianceLogResponse response = complianceLogService.createLog(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Validate recycler certificate against CPCB capacity limit & ghost credit duplicate registry.
     */
    @PostMapping("/validate-certificate")
    public ResponseEntity<com.eprtracker.dto.response.CertificateValidationResponse> validateCertificate(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody com.eprtracker.dto.request.CertificateValidationRequest request) {
        return ResponseEntity.ok(complianceLogService.validateCertificate(request));
    }

    /**
     * Delete compliance log entry.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") String logId) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        complianceLogService.deleteLog(companyId, logId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Export compliance logs as downloadable CSV report.
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportLogs(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "financialYear", required = false) String financialYear) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        byte[] csv = complianceLogService.exportLogsAsCsv(companyId, financialYear);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cpcb-compliance-logs.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
