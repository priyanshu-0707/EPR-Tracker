package com.eprtracker.service;

import com.eprtracker.dto.request.IngestionImportRequest;
import com.eprtracker.dto.response.IngestionImportResponse;
import com.eprtracker.dto.response.IngestionPreviewResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface IngestionMapperService {
    IngestionPreviewResponse previewFile(MultipartFile file);
    IngestionImportResponse mapAndImport(String companyId, IngestionImportRequest request);
    Map<String, String> getCompanyTemplate(String companyId);
}
