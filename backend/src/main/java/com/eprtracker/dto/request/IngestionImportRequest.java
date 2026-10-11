package com.eprtracker.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestionImportRequest {

    private String fileContentBase64;
    private String filename;

    /**
     * Mapping from Source ERP Column Header -> Standard CPCB System Field
     * Supported fields:
     * - "productName"
     * - "category"
     * - "unitsSold"
     * - "financialYear"
     * - "weightKg"
     */
    @NotNull(message = "Column mapping is required")
    private Map<String, String> columnMapping;

    private boolean saveTemplate;
    private String templateName;
}
