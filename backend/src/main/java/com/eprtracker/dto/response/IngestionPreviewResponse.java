package com.eprtracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestionPreviewResponse {
    private String filename;
    private int totalRows;
    private List<String> detectedHeaders;
    private List<Map<String, String>> previewRows; // First 5 rows for visual preview
    private Map<String, String> suggestedMapping; // e.g., "Prod_Qty" -> "unitsSold"
}
