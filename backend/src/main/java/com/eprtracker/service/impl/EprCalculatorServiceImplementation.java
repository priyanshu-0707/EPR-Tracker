package com.eprtracker.service.impl;

import com.eprtracker.dto.request.EprCalculateRequest;
import com.eprtracker.dto.response.CategoryResponse;
import com.eprtracker.dto.response.EprCalculationResponse;
import com.eprtracker.dto.response.EprSummaryResponse;
import com.eprtracker.entity.ComplianceLog;
import com.eprtracker.entity.Product;
import com.eprtracker.exception.ValidationException;
import com.eprtracker.repository.ComplianceLogRepository;
import com.eprtracker.repository.ProductRepository;
import com.eprtracker.service.EprCalculatorService;
import com.eprtracker.util.EprCategory;
import com.eprtracker.util.IndianNumberFormatter;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EprCalculatorServiceImplementation implements EprCalculatorService {

    private final ProductRepository productRepository;
    private final ComplianceLogRepository complianceLogRepository;

    public EprCalculatorServiceImplementation(
            ProductRepository productRepository,
            ComplianceLogRepository complianceLogRepository) {
        this.productRepository = productRepository;
        this.complianceLogRepository = complianceLogRepository;
    }

    @Override
    public EprCalculationResponse calculate(EprCalculateRequest request) {
        EprCategory category;
        try {
            category = EprCategory.valueOf(request.getCategory().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid product category: " + request.getCategory());
        }

        String fy = (request.getFinancialYear() != null && !request.getFinancialYear().isBlank())
                ? request.getFinancialYear() : "2026-27";
        double targetPercentage = EprCategory.getTargetPercentageForYear(fy);
        double conversionFactor = category.getConversionFactor();
        long unitsSold = request.getUnitsSold();

        long obligationUnits = Math.round(unitsSold * conversionFactor * targetPercentage);

        String breakdown = String.format("%s sold × %s Cf (1/%d yrs) × %d%% target = %s units obligation",
                IndianNumberFormatter.format(unitsSold),
                conversionFactor,
                category.getLifespanYears(),
                (int) (targetPercentage * 100),
                IndianNumberFormatter.format(obligationUnits));

        return EprCalculationResponse.builder()
                .category(category.name())
                .categoryDisplayName(category.getDisplayName())
                .unitsSold(unitsSold)
                .formattedUnitsSold(IndianNumberFormatter.format(unitsSold))
                .lifespanYears(category.getLifespanYears())
                .conversionFactor(conversionFactor)
                .financialYear(fy)
                .targetPercentage(targetPercentage)
                .obligationUnits(obligationUnits)
                .formattedObligationUnits(IndianNumberFormatter.format(obligationUnits))
                .calculationBreakdown(breakdown)
                .build();
    }

    @Override
    public EprSummaryResponse getSummary(String companyId, String financialYear) {
        String fy = (financialYear != null && !financialYear.isBlank()) ? financialYear : "2026-27";

        List<Product> products = productRepository.findByCompanyIdAndFinancialYear(companyId, fy);
        long totalObligation = products.stream()
                .mapToLong(p -> p.getEprObligationUnits() != null ? p.getEprObligationUnits() : 0L)
                .sum();

        List<ComplianceLog> logs = complianceLogRepository.findByCompanyIdAndFinancialYear(companyId, fy);
        // Note: In CPCB rules, collected kg is converted or matched to units obligation depending on average weight factor (1 kg roughly 1 unit equivalent default)
        long totalCollected = Math.round(logs.stream()
                .mapToDouble(l -> l.getQuantityCollectedKg() != null ? l.getQuantityCollectedKg() : 0.0)
                .sum());

        long outstanding = Math.max(0, totalObligation - totalCollected);
        double percentage = totalObligation > 0 ? ((double) totalCollected / totalObligation) * 100 : 0.0;
        percentage = Math.round(percentage * 10.0) / 10.0;

        String status;
        if (percentage >= 70.0) {
            status = "ON TRACK";
        } else if (percentage >= 40.0) {
            status = "AT RISK";
        } else {
            status = "NON-COMPLIANT";
        }

        return EprSummaryResponse.builder()
                .totalObligationUnits(totalObligation)
                .formattedTotalObligationUnits(IndianNumberFormatter.format(totalObligation))
                .totalCollectedUnits(totalCollected)
                .formattedTotalCollectedUnits(IndianNumberFormatter.format(totalCollected))
                .outstandingUnits(outstanding)
                .formattedOutstandingUnits(IndianNumberFormatter.format(outstanding))
                .fulfillmentPercentage(percentage)
                .complianceStatus(status)
                .financialYear(fy)
                .build();
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return Arrays.stream(EprCategory.values())
                .map(cat -> CategoryResponse.builder()
                        .category(cat.name())
                        .displayName(cat.getDisplayName())
                        .lifespanYears(cat.getLifespanYears())
                        .conversionFactor(cat.getConversionFactor())
                        .build())
                .collect(Collectors.toList());
    }
}
