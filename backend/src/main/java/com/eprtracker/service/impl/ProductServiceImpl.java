package com.eprtracker.service.impl;

import com.eprtracker.dto.request.ProductRequest;
import com.eprtracker.dto.response.ProductResponse;
import com.eprtracker.entity.Product;
import com.eprtracker.exception.ResourceNotFoundException;
import com.eprtracker.exception.ValidationException;
import com.eprtracker.repository.ProductRepository;
import com.eprtracker.service.ProductService;
import com.eprtracker.util.EprCategory;
import com.eprtracker.util.IndianNumberFormatter;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponse createProduct(String companyId, ProductRequest request) {
        EprCategory category;
        try {
            category = EprCategory.valueOf(request.getProductCategory().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid product category: " + request.getProductCategory());
        }

        String fy = (request.getFinancialYear() != null && !request.getFinancialYear().isBlank())
                ? request.getFinancialYear() : "2026-27";
        double target = EprCategory.getTargetPercentageForYear(fy);
        double cf = category.getConversionFactor();
        long obligation = Math.round(request.getUnitsSold() * cf * target);

        Product product = Product.builder()
                .productId(UUID.randomUUID().toString())
                .companyId(companyId)
                .productName(request.getProductName())
                .productCategory(category.name())
                .unitsSold(request.getUnitsSold())
                .conversionFactor(cf)
                .financialYear(fy)
                .eprObligationUnits(obligation)
                .createdAt(Instant.now().toString())
                .build();

        productRepository.save(product);
        return mapToResponse(product);
    }

    @Override
    public List<ProductResponse> getProductsByCompany(String companyId) {
        return productRepository.findByCompanyId(companyId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(String companyId, String productId) {
        Product product = productRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        return mapToResponse(product);
    }

    @Override
    public ProductResponse updateProduct(String companyId, String productId, ProductRequest request) {
        Product existing = productRepository.findByIdAndCompanyId(productId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        EprCategory category;
        try {
            category = EprCategory.valueOf(request.getProductCategory().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid product category: " + request.getProductCategory());
        }

        String fy = (request.getFinancialYear() != null && !request.getFinancialYear().isBlank())
                ? request.getFinancialYear() : existing.getFinancialYear();
        double target = EprCategory.getTargetPercentageForYear(fy);
        double cf = category.getConversionFactor();
        long obligation = Math.round(request.getUnitsSold() * cf * target);

        existing.setProductName(request.getProductName());
        existing.setProductCategory(category.name());
        existing.setUnitsSold(request.getUnitsSold());
        existing.setConversionFactor(cf);
        existing.setFinancialYear(fy);
        existing.setEprObligationUnits(obligation);

        productRepository.save(existing);
        return mapToResponse(existing);
    }

    @Override
    public void deleteProduct(String companyId, String productId) {
        productRepository.deleteByIdAndCompanyId(productId, companyId);
    }

    private ProductResponse mapToResponse(Product product) {
        EprCategory category;
        try {
            category = EprCategory.valueOf(product.getProductCategory());
        } catch (Exception e) {
            category = EprCategory.OTHER_EEE;
        }

        return ProductResponse.builder()
                .productId(product.getProductId())
                .companyId(product.getCompanyId())
                .productName(product.getProductName())
                .productCategory(product.getProductCategory())
                .categoryDisplayName(category.getDisplayName())
                .unitsSold(product.getUnitsSold())
                .formattedUnitsSold(IndianNumberFormatter.format(product.getUnitsSold() != null ? product.getUnitsSold() : 0L))
                .conversionFactor(product.getConversionFactor())
                .financialYear(product.getFinancialYear())
                .eprObligationUnits(product.getEprObligationUnits())
                .formattedEprObligationUnits(IndianNumberFormatter.format(product.getEprObligationUnits() != null ? product.getEprObligationUnits() : 0L))
                .createdAt(product.getCreatedAt())
                .build();
    }
}
