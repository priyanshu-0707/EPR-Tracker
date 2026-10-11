package com.eprtracker.service.impl;

import com.eprtracker.dto.request.ReportRequest;
import com.eprtracker.dto.response.ComplianceLogResponse;
import com.eprtracker.dto.response.EprSummaryResponse;
import com.eprtracker.dto.response.ProductResponse;
import com.eprtracker.dto.response.ReportResponse;
import com.eprtracker.entity.Company;
import com.eprtracker.exception.ResourceNotFoundException;
import com.eprtracker.repository.CompanyRepository;
import com.eprtracker.service.ComplianceLogService;
import com.eprtracker.service.EprCalculatorService;
import com.eprtracker.service.ProductService;
import com.eprtracker.service.ReportService;
import com.eprtracker.service.S3Service;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final CompanyRepository companyRepository;
    private final ProductService productService;
    private final ComplianceLogService complianceLogService;
    private final EprCalculatorService eprCalculatorService;
    private final S3Service s3Service;

    // Cache generated reports and raw PDF bytes
    private final Map<String, List<ReportResponse>> reportCache = new ConcurrentHashMap<>();
    private final Map<String, byte[]> pdfByteCache = new ConcurrentHashMap<>();

    public ReportServiceImpl(
            CompanyRepository companyRepository,
            ProductService productService,
            ComplianceLogService complianceLogService,
            EprCalculatorService eprCalculatorService,
            S3Service s3Service) {
        this.companyRepository = companyRepository;
        this.productService = productService;
        this.complianceLogService = complianceLogService;
        this.eprCalculatorService = eprCalculatorService;
        this.s3Service = s3Service;
    }

    @Override
    public ReportResponse generatePdfReport(String companyId, ReportRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        String fy = (request.getFinancialYear() != null && !request.getFinancialYear().isBlank())
                ? request.getFinancialYear() : "2026-27";

        EprSummaryResponse summary = eprCalculatorService.getSummary(companyId, fy);
        List<ProductResponse> products = productService.getProductsByCompany(companyId);
        List<ComplianceLogResponse> logs = complianceLogService.getLogsByCompany(companyId, fy);

        byte[] pdfBytes = buildPdf(company, fy, summary, products, logs);

        String reportId = UUID.randomUUID().toString();
        pdfByteCache.put(reportId, pdfBytes);

        String s3Key = "reports/" + companyId + "/" + reportId + ".pdf";
        try {
            s3Service.uploadFile(s3Key, pdfBytes, "application/pdf");
        } catch (Exception ignored) {}

        // Direct server download link ensures reliable viewing in both local development and AWS
        String downloadUrl = "/api/reports/download/" + reportId;

        ReportResponse reportResponse = ReportResponse.builder()
                .reportId(reportId)
                .reportName("CPCB_EPR_Report_" + fy + "_" + company.getCompanyName().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf")
                .downloadUrl(downloadUrl)
                .generatedAt(Instant.now().toString())
                .financialYear(fy)
                .complianceStatus(summary.getComplianceStatus())
                .fulfillmentPercentage(summary.getFulfillmentPercentage())
                .build();

        reportCache.computeIfAbsent(companyId, k -> new ArrayList<>()).add(0, reportResponse);
        return reportResponse;
    }

    @Override
    public List<ReportResponse> getReportHistory(String companyId) {
        return reportCache.getOrDefault(companyId, Collections.emptyList());
    }

    @Override
    public byte[] getReportBytes(String reportId) {
        byte[] bytes = pdfByteCache.get(reportId);
        if (bytes != null) {
            return bytes;
        }
        throw new ResourceNotFoundException("Report not found or expired with id: " + reportId);
    }

    @Override
    public void deleteReport(String companyId, String reportId) {
        List<ReportResponse> reports = reportCache.get(companyId);
        if (reports != null) {
            reports.removeIf(r -> reportId.equals(r.getReportId()));
        }
        pdfByteCache.remove(reportId);
    }

    private byte[] buildPdf(
            Company company,
            String fy,
            EprSummaryResponse summary,
            List<ProductResponse> products,
            List<ComplianceLogResponse> logs) {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc);

            // Styling colors
            DeviceRgb brandDark = new DeviceRgb(13, 17, 23);
            DeviceRgb accentGreen = new DeviceRgb(63, 185, 80);
            DeviceRgb accentAmber = new DeviceRgb(210, 153, 34);
            DeviceRgb accentRed = new DeviceRgb(248, 81, 73);

            // Section 1: Header
            Paragraph title = new Paragraph("EPR COMPLIANCE TRACKER")
                    .setFontSize(22)
                    .setBold()
                    .setFontColor(brandDark);
            Paragraph subtitle = new Paragraph("CPCB Legal Compliance Report — E-Waste (Management) Rules, 2022")
                    .setFontSize(12)
                    .setFontColor(ColorConstants.GRAY);
            doc.add(title);
            doc.add(subtitle);
            doc.add(new Paragraph("\n"));

            // Section 2: Company details
            Table compTable = new Table(UnitValue.createPercentArray(new float[]{30, 70})).useAllAvailableWidth();
            compTable.addCell(createHeaderCell("Company Name"));
            compTable.addCell(new Cell().add(new Paragraph(company.getCompanyName())));
            compTable.addCell(createHeaderCell("CPCB Registration No."));
            compTable.addCell(new Cell().add(new Paragraph(company.getCpcbRegistrationNumber() != null ? company.getCpcbRegistrationNumber() : "N/A")));
            compTable.addCell(createHeaderCell("State / UT"));
            compTable.addCell(new Cell().add(new Paragraph(company.getState())));
            compTable.addCell(createHeaderCell("Financial Year"));
            compTable.addCell(new Cell().add(new Paragraph(fy)));
            doc.add(compTable);
            doc.add(new Paragraph("\n"));

            // Section 3: Executive Summary Table
            Paragraph execTitle = new Paragraph("Executive Summary").setFontSize(14).setBold();
            doc.add(execTitle);
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25})).useAllAvailableWidth();
            summaryTable.addCell(createHeaderCell("Total Obligation"));
            summaryTable.addCell(createHeaderCell("Collected So Far"));
            summaryTable.addCell(createHeaderCell("Outstanding Target"));
            summaryTable.addCell(createHeaderCell("Fulfillment Status"));

            summaryTable.addCell(new Cell().add(new Paragraph(summary.getFormattedTotalObligationUnits() + " units")));
            summaryTable.addCell(new Cell().add(new Paragraph(summary.getFormattedTotalCollectedUnits() + " kg (units eq)")));
            summaryTable.addCell(new Cell().add(new Paragraph(summary.getFormattedOutstandingUnits() + " units")));

            DeviceRgb statusColor = "ON TRACK".equals(summary.getComplianceStatus()) ? accentGreen :
                    ("AT RISK".equals(summary.getComplianceStatus()) ? accentAmber : accentRed);
            summaryTable.addCell(new Cell().add(new Paragraph(summary.getComplianceStatus() + " (" + summary.getFulfillmentPercentage() + "%)").setFontColor(statusColor).setBold()));
            doc.add(summaryTable);
            doc.add(new Paragraph("\n"));

            // Section 4: Products Table
            Paragraph prodTitle = new Paragraph("Products Subject to EPR Obligation").setFontSize(14).setBold();
            doc.add(prodTitle);
            Table prodTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 15, 15, 20})).useAllAvailableWidth();
            prodTable.addCell(createHeaderCell("Category"));
            prodTable.addCell(createHeaderCell("Product Name"));
            prodTable.addCell(createHeaderCell("Units Sold"));
            prodTable.addCell(createHeaderCell("Cf Factor"));
            prodTable.addCell(createHeaderCell("Obligation Units"));

            for (ProductResponse p : products) {
                prodTable.addCell(new Cell().add(new Paragraph(p.getCategoryDisplayName())));
                prodTable.addCell(new Cell().add(new Paragraph(p.getProductName())));
                prodTable.addCell(new Cell().add(new Paragraph(p.getFormattedUnitsSold())));
                prodTable.addCell(new Cell().add(new Paragraph(String.valueOf(p.getConversionFactor()))));
                prodTable.addCell(new Cell().add(new Paragraph(p.getFormattedEprObligationUnits())));
            }
            if (products.isEmpty()) {
                prodTable.addCell(new Cell(1, 5).add(new Paragraph("No products registered for this financial year.").setTextAlignment(TextAlignment.CENTER)));
            }
            doc.add(prodTable);
            doc.add(new Paragraph("\n"));

            // Section 5: Collections Table
            Paragraph colTitle = new Paragraph("Verified Recycler Collections").setFontSize(14).setBold();
            doc.add(colTitle);
            Table colTable = new Table(UnitValue.createPercentArray(new float[]{20, 30, 25, 25})).useAllAvailableWidth();
            colTable.addCell(createHeaderCell("Date"));
            colTable.addCell(createHeaderCell("Recycler Name"));
            colTable.addCell(createHeaderCell("Quantity (kg)"));
            colTable.addCell(createHeaderCell("Certificate No."));

            for (ComplianceLogResponse l : logs) {
                colTable.addCell(new Cell().add(new Paragraph(l.getDateLogged())));
                colTable.addCell(new Cell().add(new Paragraph(l.getRecyclerName())));
                colTable.addCell(new Cell().add(new Paragraph(l.getFormattedQuantityCollectedKg())));
                colTable.addCell(new Cell().add(new Paragraph(l.getEprCertificateNumber())));
            }
            if (logs.isEmpty()) {
                colTable.addCell(new Cell(1, 4).add(new Paragraph("No collection certificates logged yet.").setTextAlignment(TextAlignment.CENTER)));
            }
            doc.add(colTable);
            doc.add(new Paragraph("\n"));

            // Section 6: Compliance Status Badge
            Paragraph badge = new Paragraph("STATUS: " + summary.getComplianceStatus())
                    .setFontSize(16)
                    .setBold()
                    .setFontColor(statusColor);
            doc.add(badge);
            doc.add(new Paragraph("\n"));

            // Section 7: Footer
            String dateFormatted = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                    .withZone(ZoneId.of("Asia/Kolkata"))
                    .format(Instant.now());
            Paragraph footer = new Paragraph("Generated on " + dateFormatted + ". Official submission portal: eprewaste.cpcb.gov.in")
                    .setFontSize(9)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF report: {}", e.getMessage(), e);
            throw new RuntimeException("PDF report generation failed", e);
        }
    }

    private Cell createHeaderCell(String text) {
        return new Cell().add(new Paragraph(text).setBold())
                .setBackgroundColor(new DeviceRgb(240, 242, 245));
    }
}
