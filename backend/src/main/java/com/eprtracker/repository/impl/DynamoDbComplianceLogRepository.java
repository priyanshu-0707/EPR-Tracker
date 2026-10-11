package com.eprtracker.repository.impl;

import com.eprtracker.entity.ComplianceLog;
import com.eprtracker.repository.ComplianceLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class DynamoDbComplianceLogRepository implements ComplianceLogRepository {

    private final DynamoDbTable<ComplianceLog> table;
    private final java.util.Map<String, ComplianceLog> localStore = new java.util.concurrent.ConcurrentHashMap<>();

    public DynamoDbComplianceLogRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-prefix:epr-tracker-}") String tablePrefix) {
        DynamoDbTable<ComplianceLog> tbl = null;
        try {
            tbl = enhancedClient.table(tablePrefix + "compliance-log", TableSchema.fromBean(ComplianceLog.class));
            tbl.createTable();
        } catch (Exception ignored) {}
        this.table = tbl;
    }

    @Override
    public void save(ComplianceLog log) {
        localStore.put(log.getLogId(), log);
        try {
            if (table != null) table.putItem(log);
        } catch (Exception ignored) {}
    }

    @Override
    public Optional<ComplianceLog> findByIdAndCompanyId(String logId, String companyId) {
        ComplianceLog inMem = localStore.get(logId);
        if (inMem != null && companyId.equals(inMem.getCompanyId())) {
            return Optional.of(inMem);
        }
        try {
            if (table != null) {
                ComplianceLog item = table.getItem(Key.builder().partitionValue(logId).sortValue(companyId).build());
                return Optional.ofNullable(item);
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    @Override
    public List<ComplianceLog> findByCompanyId(String companyId) {
        List<ComplianceLog> list = localStore.values().stream()
                .filter(l -> companyId.equals(l.getCompanyId()))
                .collect(Collectors.toList());
        if (!list.isEmpty()) return list;

        try {
            if (table != null) {
                return table.scan().items().stream()
                        .filter(l -> companyId.equals(l.getCompanyId()))
                        .collect(Collectors.toList());
            }
        } catch (Exception ignored) {}
        return list;
    }

    @Override
    public List<ComplianceLog> findByCompanyIdAndFinancialYear(String companyId, String financialYear) {
        List<ComplianceLog> list = localStore.values().stream()
                .filter(l -> companyId.equals(l.getCompanyId()) && financialYear.equalsIgnoreCase(l.getFinancialYear()))
                .collect(Collectors.toList());
        if (!list.isEmpty()) return list;

        try {
            if (table != null) {
                return table.scan().items().stream()
                        .filter(l -> companyId.equals(l.getCompanyId()) && financialYear.equalsIgnoreCase(l.getFinancialYear()))
                        .collect(Collectors.toList());
            }
        } catch (Exception ignored) {}
        return list;
    }

    @Override
    public List<ComplianceLog> findAll() {
        if (!localStore.isEmpty()) {
            return new java.util.ArrayList<>(localStore.values());
        }
        try {
            if (table != null) {
                return table.scan().items().stream().collect(Collectors.toList());
            }
        } catch (Exception ignored) {}
        return new java.util.ArrayList<>();
    }

    @Override
    public List<ComplianceLog> findByCertificateNumber(String certificateNumber) {
        if (certificateNumber == null) return List.of();
        return findAll().stream()
                .filter(l -> certificateNumber.equalsIgnoreCase(l.getEprCertificateNumber()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByIdAndCompanyId(String logId, String companyId) {
        localStore.remove(logId);
        try {
            if (table != null) table.deleteItem(Key.builder().partitionValue(logId).sortValue(companyId).build());
        } catch (Exception ignored) {}
    }
}
