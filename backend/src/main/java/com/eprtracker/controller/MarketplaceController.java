package com.eprtracker.controller;

import com.eprtracker.dto.request.MarketplaceOrderRequest;
import com.eprtracker.dto.response.MarketplaceOrderResponse;
import com.eprtracker.dto.response.MarketplaceRecommendationResponse;
import com.eprtracker.security.JwtTokenProvider;
import com.eprtracker.service.MarketplaceService;
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
 * Controller for Automated Gap-Filling Marketplace Procurement.
 */
@RestController
@RequestMapping("/api/marketplace")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;
    private final JwtTokenProvider tokenProvider;

    public MarketplaceController(MarketplaceService marketplaceService, JwtTokenProvider tokenProvider) {
        this.marketplaceService = marketplaceService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Get automated shortfall analysis and matched surplus credit offers from verified CPCB recyclers.
     */
    @GetMapping("/recommendations")
    public ResponseEntity<MarketplaceRecommendationResponse> getRecommendations(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "financialYear", required = false) String financialYear) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        return ResponseEntity.ok(marketplaceService.getRecommendations(companyId, financialYear));
    }

    /**
     * Place order / RFQ to purchase EPR credits from verified recycler.
     */
    @PostMapping("/order")
    public ResponseEntity<MarketplaceOrderResponse> placeOrder(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody MarketplaceOrderRequest request) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        return ResponseEntity.ok(marketplaceService.placeOrder(companyId, request));
    }
}
