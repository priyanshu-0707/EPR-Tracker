package com.eprtracker.service;

import com.eprtracker.dto.request.ProductRequest;
import com.eprtracker.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(String companyId, ProductRequest request);
    List<ProductResponse> getProductsByCompany(String companyId);
    ProductResponse getProductById(String companyId, String productId);
    ProductResponse updateProduct(String companyId, String productId, ProductRequest request);
    void deleteProduct(String companyId, String productId);
}
