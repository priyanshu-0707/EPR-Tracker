package com.eprtracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestionImportResponse {
    private int importedCount;
    private int skippedCount;
    private List<String> importedProductNames;
    private List<String> errors;
    private String templateSaved;
    private String message;
}
