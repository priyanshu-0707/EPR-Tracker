package com.eprtracker.service;

import com.eprtracker.dto.request.ReportRequest;
import com.eprtracker.dto.response.ReportResponse;

import java.util.List;

public interface ReportService {
    ReportResponse generatePdfReport(String companyId, ReportRequest request);
    List<ReportResponse> getReportHistory(String companyId);
    byte[] getReportBytes(String reportId);
    void deleteReport(String companyId, String reportId);
}
