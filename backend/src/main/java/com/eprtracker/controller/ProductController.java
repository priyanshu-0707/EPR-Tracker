package com.eprtracker.controller;

import com.eprtracker.dto.request.ProductRequest;
import com.eprtracker.dto.response.CategoryResponse;
import com.eprtracker.dto.response.ProductResponse;
import com.eprtracker.security.JwtTokenProvider;
import com.eprtracker.service.EprCalculatorService;
import com.eprtracker.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller managing electronic products subject to EPR obligations.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final EprCalculatorService eprCalculatorService;
    private final JwtTokenProvider tokenProvider;

    public ProductController(
            ProductService productService,
            EprCalculatorService eprCalculatorService,
            JwtTokenProvider tokenProvider) {
        this.productService = productService;
        this.eprCalculatorService = eprCalculatorService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Get list of all CPCB prescribed electrical and electronic equipment categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(eprCalculatorService.getAllCategories());
    }

    /**
     * Get all registered products for authenticated company.
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        return ResponseEntity.ok(productService.getProductsByCompany(companyId));
    }

    /**
     * Register a new product item under legal EPR tracking.
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ProductRequest request) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        ProductResponse response = productService.createProduct(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update product details or sales volumes.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") String productId,
            @Valid @RequestBody ProductRequest request) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        ProductResponse response = productService.updateProduct(companyId, productId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete product from EPR inventory.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") String productId) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        productService.deleteProduct(companyId, productId);
        return ResponseEntity.noContent().build();
    }
}
