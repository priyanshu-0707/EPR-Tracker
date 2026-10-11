package com.eprtracker.controller;

import com.eprtracker.dto.request.ReportRequest;
import com.eprtracker.dto.response.ReportResponse;
import com.eprtracker.security.JwtTokenProvider;
import com.eprtracker.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller providing generation and retrieval of official CPCB PDF compliance reports.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final com.eprtracker.service.CpcbFormExportService cpcbFormExportService;
    private final JwtTokenProvider tokenProvider;

    public ReportController(
            ReportService reportService,
            com.eprtracker.service.CpcbFormExportService cpcbFormExportService,
            JwtTokenProvider tokenProvider) {
        this.reportService = reportService;
        this.cpcbFormExportService = cpcbFormExportService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Generate iText 7 legal PDF report and upload to S3, returning a 24-hr presigned download link.
     */
    @PostMapping("/generate")
    public ResponseEntity<ReportResponse> generateReport(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) ReportRequest request) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        ReportRequest req = request != null ? request : new ReportRequest("2026-27");
        ReportResponse response = reportService.generatePdfReport(companyId, req);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve list of previously generated reports for company.
     */
    @GetMapping("/history")
    public ResponseEntity<List<ReportResponse>> getReportHistory(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        return ResponseEntity.ok(reportService.getReportHistory(companyId));
    }

    /**
     * Direct PDF download endpoint streaming generated report bytes.
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadReport(@org.springframework.web.bind.annotation.PathVariable("id") String reportId) {
        byte[] pdfBytes = reportService.getReportBytes(reportId);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cpcb-compliance-report.pdf\"")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Delete generated PDF report from archive.
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(
            @RequestHeader("Authorization") String authHeader,
            @org.springframework.web.bind.annotation.PathVariable("id") String reportId) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        reportService.deleteReport(companyId, reportId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Generate and download official CPCB Form 2 (Annual Returns) multi-sheet Excel spreadsheet.
     */
    @GetMapping("/form-2-excel")
    public ResponseEntity<byte[]> exportForm2Excel(
            @RequestHeader("Authorization") String authHeader,
            @org.springframework.web.bind.annotation.RequestParam(value = "financialYear", required = false) String financialYear) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        byte[] excelBytes = cpcbFormExportService.generateForm2Excel(companyId, financialYear);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"CPCB-Form2-Annual-Return.xlsx\"")
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
