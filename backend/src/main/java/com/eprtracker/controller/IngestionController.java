package com.eprtracker.controller;

import com.eprtracker.dto.request.IngestionImportRequest;
import com.eprtracker.dto.response.IngestionImportResponse;
import com.eprtracker.dto.response.IngestionPreviewResponse;
import com.eprtracker.security.JwtTokenProvider;
import com.eprtracker.service.IngestionMapperService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Controller for Smart Data Ingestion and Dynamic Schema Mapping from ERP exports (SAP/Oracle).
 */
@RestController
@RequestMapping("/api/ingestion")
public class IngestionController {

    private final IngestionMapperService ingestionMapperService;
    private final JwtTokenProvider tokenProvider;

    public IngestionController(IngestionMapperService ingestionMapperService, JwtTokenProvider tokenProvider) {
        this.ingestionMapperService = ingestionMapperService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Preview uploaded CSV or Excel file and detect headers + suggest column mappings.
     */
    @PostMapping("/preview")
    public ResponseEntity<IngestionPreviewResponse> previewFile(
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ingestionMapperService.previewFile(file));
    }

    /**
     * Map extracted ERP columns to CPCB standard fields and ingest into database.
     */
    @PostMapping("/import")
    public ResponseEntity<IngestionImportResponse> mapAndImport(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody IngestionImportRequest request) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        return ResponseEntity.ok(ingestionMapperService.mapAndImport(companyId, request));
    }

    /**
     * Get saved schema mapping template for the current company.
     */
    @GetMapping("/template")
    public ResponseEntity<Map<String, String>> getCompanyTemplate(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        return ResponseEntity.ok(ingestionMapperService.getCompanyTemplate(companyId));
    }
}
