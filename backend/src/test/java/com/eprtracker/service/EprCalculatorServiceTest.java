package com.eprtracker.service;

import com.eprtracker.dto.request.EprCalculateRequest;
import com.eprtracker.dto.response.EprCalculationResponse;
import com.eprtracker.repository.ComplianceLogRepository;
import com.eprtracker.repository.ProductRepository;
import com.eprtracker.service.impl.EprCalculatorServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EprCalculatorServiceTest {

    private EprCalculatorService eprCalculatorService;
    private ProductRepository productRepository;
    private ComplianceLogRepository complianceLogRepository;

    @BeforeEach
    void setUp() {
        productRepository = Mockito.mock(ProductRepository.class);
        complianceLogRepository = Mockito.mock(ComplianceLogRepository.class);
        eprCalculatorService = new EprCalculatorServiceImplementation(productRepository, complianceLogRepository);
    }

    @Test
    @DisplayName("Should correctly calculate EPR obligation for 10,00,000 earphones in FY2026-27 (2,66,400 units)")
    void testCalculateEarphonesObligation() {
        // Worked example: boAt sells 10,00,000 earphones in FY2026-27
        // Cf = 1/3 = 0.333, Target% = 80% = 0.80
        // Obligation = 10,00,000 * 0.333 * 0.80 = 2,66,400 units
        EprCalculateRequest request = EprCalculateRequest.builder()
                .category("EARPHONE_HEADPHONE")
                .unitsSold(1000000L)
                .financialYear("2026-27")
                .build();

        EprCalculationResponse response = eprCalculatorService.calculate(request);

        assertNotNull(response);
        assertEquals(3, response.getLifespanYears());
        assertEquals(0.333, response.getConversionFactor());
        assertEquals(0.80, response.getTargetPercentage());
        assertEquals(266400L, response.getObligationUnits());
        assertEquals("2,66,400", response.getFormattedObligationUnits());
        assertEquals("10,00,000", response.getFormattedUnitsSold());
    }

    @Test
    @DisplayName("Should correctly calculate EPR obligation for 5,00,000 mobile phones in FY2026-27 (1,33,200 units)")
    void testCalculateMobilePhonesObligation() {
        EprCalculateRequest request = EprCalculateRequest.builder()
                .category("MOBILE_PHONE")
                .unitsSold(500000L)
                .financialYear("2026-27")
                .build();

        EprCalculationResponse response = eprCalculatorService.calculate(request);

        assertNotNull(response);
        assertEquals(3, response.getLifespanYears());
        assertEquals(0.333, response.getConversionFactor());
        assertEquals(133200L, response.getObligationUnits());
        assertEquals("1,33,200", response.getFormattedObligationUnits());
    }

    @Test
    @DisplayName("Should correctly calculate EPR obligation for 4,00,000 laptop computers (64,000 units)")
    void testCalculateLaptopsObligation() {
        // Laptops: lifespan 5 years, Cf = 0.200, target 80%
        // Obligation = 4,00,000 * 0.200 * 0.80 = 64,000 units
        EprCalculateRequest request = EprCalculateRequest.builder()
                .category("LAPTOP_COMPUTER")
                .unitsSold(400000L)
                .financialYear("2026-27")
                .build();

        EprCalculationResponse response = eprCalculatorService.calculate(request);

        assertNotNull(response);
        assertEquals(5, response.getLifespanYears());
        assertEquals(0.200, response.getConversionFactor());
        assertEquals(64000L, response.getObligationUnits());
        assertEquals("64,000", response.getFormattedObligationUnits());
    }
}
