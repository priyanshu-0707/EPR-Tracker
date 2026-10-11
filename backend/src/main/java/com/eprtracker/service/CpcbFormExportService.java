package com.eprtracker.service;

public interface CpcbFormExportService {
    byte[] generateForm2Excel(String companyId, String financialYear);
}
