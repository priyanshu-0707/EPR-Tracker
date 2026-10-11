package com.eprtracker.controller;

import com.eprtracker.dto.request.EprCalculateRequest;
import com.eprtracker.dto.response.EprCalculationResponse;
import com.eprtracker.dto.response.EprSummaryResponse;
import com.eprtracker.security.JwtTokenProvider;
import com.eprtracker.service.EprCalculatorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller exposing EPR Calculator operations and summary statistics.
 */
@RestController
@RequestMapping("/api/epr")
public class EprCalculatorController {

    private final EprCalculatorService eprCalculatorService;
    private final JwtTokenProvider tokenProvider;

    public EprCalculatorController(EprCalculatorService eprCalculatorService, JwtTokenProvider tokenProvider) {
        this.eprCalculatorService = eprCalculatorService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Calculate legal EPR obligation units for a specified product category and volume.
     */
    @PostMapping("/calculate")
    public ResponseEntity<EprCalculationResponse> calculate(@Valid @RequestBody EprCalculateRequest request) {
        EprCalculationResponse response = eprCalculatorService.calculate(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get aggregate summary of company obligations vs logged collections.
     */
    @GetMapping("/summary")
    public ResponseEntity<EprSummaryResponse> getSummary(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "financialYear", required = false) String financialYear) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        EprSummaryResponse response = eprCalculatorService.getSummary(companyId, financialYear);
        return ResponseEntity.ok(response);
    }
}
