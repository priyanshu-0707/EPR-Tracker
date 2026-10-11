package com.eprtracker.repository;

import com.eprtracker.entity.ComplianceLog;

import java.util.List;
import java.util.Optional;

public interface ComplianceLogRepository {
    void save(ComplianceLog log);
    Optional<ComplianceLog> findByIdAndCompanyId(String logId, String companyId);
    List<ComplianceLog> findByCompanyId(String companyId);
    List<ComplianceLog> findByCompanyIdAndFinancialYear(String companyId, String financialYear);
    List<ComplianceLog> findAll();
    List<ComplianceLog> findByCertificateNumber(String certificateNumber);
    void deleteByIdAndCompanyId(String logId, String companyId);
}
