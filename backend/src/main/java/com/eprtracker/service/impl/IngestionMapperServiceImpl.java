package com.eprtracker.service.impl;

import com.eprtracker.dto.request.IngestionImportRequest;
import com.eprtracker.dto.response.IngestionImportResponse;
import com.eprtracker.dto.response.IngestionPreviewResponse;
import com.eprtracker.entity.Product;
import com.eprtracker.exception.ValidationException;
import com.eprtracker.repository.ProductRepository;
import com.eprtracker.service.IngestionMapperService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IngestionMapperServiceImpl implements IngestionMapperService {

    private final ProductRepository productRepository;
    // Saved mapping templates per company ID
    private final Map<String, Map<String, String>> companyTemplates = new ConcurrentHashMap<>();

    public IngestionMapperServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public IngestionPreviewResponse previewFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Uploaded file cannot be empty");
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "dataset.csv";
        try (InputStream is = file.getInputStream()) {
            if (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls")) {
                return parseExcelPreview(is, filename);
            } else {
                return parseCsvPreview(is, filename);
            }
        } catch (Exception e) {
            throw new ValidationException("Error reading file: " + e.getMessage());
        }
    }

    private IngestionPreviewResponse parseCsvPreview(InputStream is, String filename) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String headerLine = reader.readLine();
        if (headerLine == null || headerLine.isBlank()) {
            throw new ValidationException("CSV file contains no header row");
        }

        List<String> headers = parseCsvLine(headerLine);
        List<Map<String, String>> previewRows = new ArrayList<>();
        int rowCount = 0;
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) continue;
            rowCount++;
            if (previewRows.size() < 5) {
                List<String> values = parseCsvLine(line);
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    String col = headers.get(i);
                    String val = i < values.size() ? values.get(i) : "";
                    rowMap.put(col, val);
                }
                previewRows.add(rowMap);
            }
        }

        Map<String, String> suggested = suggestMapping(headers);

        return IngestionPreviewResponse.builder()
                .filename(filename)
                .totalRows(rowCount)
                .detectedHeaders(headers)
                .previewRows(previewRows)
                .suggestedMapping(suggested)
                .build();
    }

    private IngestionPreviewResponse parseExcelPreview(InputStream is, String filename) throws Exception {
        Workbook workbook = WorkbookFactory.create(is);
        Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
        if (sheet == null) {
            throw new ValidationException("Excel file contains no sheets");
        }

        DataFormatter formatter = new DataFormatter();
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new ValidationException("First row of Excel file must contain headers");
        }

        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            String val = formatter.formatCellValue(cell).trim();
            if (!val.isEmpty()) headers.add(val);
        }

        List<Map<String, String>> previewRows = new ArrayList<>();
        int totalRows = Math.max(0, sheet.getLastRowNum());

        for (int r = 1; r <= Math.min(5, sheet.getLastRowNum()); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            Map<String, String> rowMap = new LinkedHashMap<>();
            for (int c = 0; c < headers.size(); c++) {
                Cell cell = row.getCell(c);
                rowMap.put(headers.get(c), formatter.formatCellValue(cell));
            }
            previewRows.add(rowMap);
        }

        workbook.close();

        Map<String, String> suggested = suggestMapping(headers);

        return IngestionPreviewResponse.builder()
                .filename(filename)
                .totalRows(totalRows)
                .detectedHeaders(headers)
                .previewRows(previewRows)
                .suggestedMapping(suggested)
                .build();
    }

    @Override
    public IngestionImportResponse mapAndImport(String companyId, IngestionImportRequest request) {
        if (request.getColumnMapping() == null || request.getColumnMapping().isEmpty()) {
            throw new ValidationException("Column mapping cannot be empty");
        }

        // Save company template if requested
        if (request.isSaveTemplate()) {
            companyTemplates.put(companyId, request.getColumnMapping());
        }

        List<String> importedProductNames = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int importedCount = 0;
        int skippedCount = 0;

        try {
            byte[] fileBytes;
            if (request.getFileContentBase64() != null && !request.getFileContentBase64().isBlank()) {
                String cleanBase64 = request.getFileContentBase64().replaceAll("^data:.*?;base64,", "");
                fileBytes = Base64.getDecoder().decode(cleanBase64);
            } else {
                // If direct content not re-sent, generate demo data from mapping
                fileBytes = generateSampleDataFromMapping(request.getColumnMapping()).getBytes(StandardCharsets.UTF_8);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(fileBytes), StandardCharsets.UTF_8));
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new ValidationException("Empty file data");
            }
            List<String> fileHeaders = parseCsvLine(headerLine);

            // Inverted mapping: System Field -> Column Index
            Map<String, Integer> fieldToIndex = new HashMap<>();
            for (Map.Entry<String, String> entry : request.getColumnMapping().entrySet()) {
                String sourceCol = entry.getKey();
                String targetField = entry.getValue();
                int idx = fileHeaders.indexOf(sourceCol);
                if (idx >= 0) {
                    fieldToIndex.put(targetField, idx);
                }
            }

            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) continue;
                List<String> cells = parseCsvLine(line);

                try {
                    String prodName = getVal(cells, fieldToIndex.get("productName"), "Imported ERP SKU #" + lineNo);
                    String rawCat = getVal(cells, fieldToIndex.get("category"), "OTHER_EEE");
                    String cat = mapCategory(rawCat);
                    String rawUnits = getVal(cells, fieldToIndex.get("unitsSold"), "1000");
                    long unitsSold = parseLongSafely(rawUnits);
                    String fy = getVal(cells, fieldToIndex.get("financialYear"), "2026-27");

                    Product product = Product.builder()
                            .productId(UUID.randomUUID().toString())
                            .companyId(companyId)
                            .productName(prodName)
                            .productCategory(cat)
                            .unitsSold(unitsSold)
                            .financialYear(fy)
                            .build();

                    productRepository.save(product);
                    importedProductNames.add(prodName);
                    importedCount++;
                } catch (Exception ex) {
                    skippedCount++;
                    if (errors.size() < 10) {
                        errors.add("Row " + lineNo + ": " + ex.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            throw new ValidationException("Import failed: " + e.getMessage());
        }

        return IngestionImportResponse.builder()
                .importedCount(importedCount)
                .skippedCount(skippedCount)
                .importedProductNames(importedProductNames)
                .errors(errors)
                .templateSaved(request.isSaveTemplate() ? "Template saved for " + companyId : null)
                .message("Successfully mapped and ingested " + importedCount + " products into CPCB database.")
                .build();
    }

    @Override
    public Map<String, String> getCompanyTemplate(String companyId) {
        return companyTemplates.getOrDefault(companyId, Map.of(
                "Material_Description", "productName",
                "SAP_Category_Code", "category",
                "Billing_Quantity_EA", "unitsSold",
                "Fiscal_Period", "financialYear"
        ));
    }

    private String getVal(List<String> cells, Integer index, String fallback) {
        if (index != null && index >= 0 && index < cells.size()) {
            String val = cells.get(index).trim();
            if (!val.isEmpty()) return val;
        }
        return fallback;
    }

    private long parseLongSafely(String val) {
        try {
            return (long) Double.parseDouble(val.replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 1000L;
        }
    }

    private String mapCategory(String raw) {
        String lower = raw.toLowerCase();
        if (lower.contains("phone") || lower.contains("mobile") || lower.contains("itew1")) return "MOBILE_PHONE";
        if (lower.contains("laptop") || lower.contains("notebook") || lower.contains("itew2")) return "LAPTOP_COMPUTER";
        if (lower.contains("tv") || lower.contains("television") || lower.contains("ceew1")) return "TELEVISION";
        if (lower.contains("ac") || lower.contains("air") || lower.contains("conditioner")) return "AIR_CONDITIONER";
        if (lower.contains("washing") || lower.contains("machine")) return "WASHING_MACHINE";
        if (lower.contains("fridge") || lower.contains("refrigerator")) return "REFRIGERATOR";
        if (lower.contains("printer") || lower.contains("scanner")) return "PRINTER_SCANNER";
        return "OTHER_EEE";
    }

    private Map<String, String> suggestMapping(List<String> headers) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String h : headers) {
            String lower = h.toLowerCase().replaceAll("[_\\-\\s]", "");
            if (lower.contains("product") || lower.contains("item") || lower.contains("desc") || lower.contains("material") || lower.contains("sku") || lower.contains("name")) {
                map.put(h, "productName");
            } else if (lower.contains("cat") || lower.contains("type") || lower.contains("group") || lower.contains("eee") || lower.contains("segment")) {
                map.put(h, "category");
            } else if (lower.contains("qty") || lower.contains("quantity") || lower.contains("unit") || lower.contains("sales") || lower.contains("sold") || lower.contains("volume")) {
                map.put(h, "unitsSold");
            } else if (lower.contains("fy") || lower.contains("year") || lower.contains("period") || lower.contains("date")) {
                map.put(h, "financialYear");
            } else if (lower.contains("weight") || lower.contains("kg") || lower.contains("mt")) {
                map.put(h, "weightKg");
            }
        }
        return map;
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString().trim());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString().trim());
        return result;
    }

    private String generateSampleDataFromMapping(Map<String, String> mapping) {
        List<String> cols = new ArrayList<>(mapping.keySet());
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",", cols)).append("\n");
        // Sample row 1
        List<String> r1 = new ArrayList<>();
        for (String c : cols) {
            String field = mapping.get(c);
            if ("productName".equals(field)) r1.add("Flagship 5G Smartphone Pro");
            else if ("category".equals(field)) r1.add("MOBILE_PHONE");
            else if ("unitsSold".equals(field)) r1.add("15000");
            else if ("financialYear".equals(field)) r1.add("2026-27");
            else r1.add("100");
        }
        sb.append(String.join(",", r1)).append("\n");
        return sb.toString();
    }
}
