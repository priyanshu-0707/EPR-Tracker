package com.eprtracker.repository;

import com.eprtracker.entity.Company;

import java.util.Optional;

public interface CompanyRepository {
    void save(Company company);
    Optional<Company> findById(String companyId);
    Optional<Company> findByEmail(String email);
    void deleteById(String companyId);
}
