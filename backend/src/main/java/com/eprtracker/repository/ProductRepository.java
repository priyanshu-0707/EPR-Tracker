package com.eprtracker.repository;

import com.eprtracker.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    void save(Product product);
    Optional<Product> findByIdAndCompanyId(String productId, String companyId);
    List<Product> findByCompanyId(String companyId);
    List<Product> findByCompanyIdAndFinancialYear(String companyId, String financialYear);
    void deleteByIdAndCompanyId(String productId, String companyId);
}
