package com.eprtracker.service;

import com.eprtracker.dto.request.MarketplaceOrderRequest;
import com.eprtracker.dto.response.MarketplaceOrderResponse;
import com.eprtracker.dto.response.MarketplaceRecommendationResponse;

public interface MarketplaceService {
    MarketplaceRecommendationResponse getRecommendations(String companyId, String financialYear);
    MarketplaceOrderResponse placeOrder(String companyId, MarketplaceOrderRequest request);
}
