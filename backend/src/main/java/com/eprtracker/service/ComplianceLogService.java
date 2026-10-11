package com.eprtracker.service;

import com.eprtracker.dto.request.CertificateValidationRequest;
import com.eprtracker.dto.request.ComplianceLogRequest;
import com.eprtracker.dto.response.CertificateValidationResponse;
import com.eprtracker.dto.response.ComplianceLogResponse;

import java.util.List;

public interface ComplianceLogService {
    ComplianceLogResponse createLog(String companyId, ComplianceLogRequest request);
    List<ComplianceLogResponse> getLogsByCompany(String companyId, String financialYear);
    void deleteLog(String companyId, String logId);
    CertificateValidationResponse validateCertificate(CertificateValidationRequest request);
    byte[] exportLogsAsCsv(String companyId, String financialYear);
}
