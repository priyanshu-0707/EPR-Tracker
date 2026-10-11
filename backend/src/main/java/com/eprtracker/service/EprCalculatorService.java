package com.eprtracker.service;

import com.eprtracker.dto.request.EprCalculateRequest;
import com.eprtracker.dto.response.CategoryResponse;
import com.eprtracker.dto.response.EprCalculationResponse;
import com.eprtracker.dto.response.EprSummaryResponse;

import java.util.List;

public interface EprCalculatorService {
    EprCalculationResponse calculate(EprCalculateRequest request);
    EprSummaryResponse getSummary(String companyId, String financialYear);
    List<CategoryResponse> getAllCategories();
}
