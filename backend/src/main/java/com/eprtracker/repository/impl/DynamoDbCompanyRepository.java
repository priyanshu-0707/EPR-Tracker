package com.eprtracker.repository.impl;

import com.eprtracker.entity.Company;
import com.eprtracker.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.Optional;

@Repository
public class DynamoDbCompanyRepository implements CompanyRepository {

    private final DynamoDbTable<Company> table;
    private final DynamoDbIndex<Company> emailIndex;
    private final java.util.Map<String, Company> localStore = new java.util.concurrent.ConcurrentHashMap<>();

    public DynamoDbCompanyRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-prefix:epr-tracker-}") String tablePrefix) {
        DynamoDbTable<Company> tbl = null;
        DynamoDbIndex<Company> idx = null;
        try {
            tbl = enhancedClient.table(tablePrefix + "companies", TableSchema.fromBean(Company.class));
            tbl.createTable();
            idx = tbl.index("email-index");
        } catch (Exception ignored) {
        }
        this.table = tbl;
        this.emailIndex = idx;
    }

    @Override
    public void save(Company company) {
        localStore.put(company.getCompanyId(), company);
        try {
            if (table != null) table.putItem(company);
        } catch (Exception ignored) {}
    }

    @Override
    public Optional<Company> findById(String companyId) {
        if (localStore.containsKey(companyId)) {
            return Optional.of(localStore.get(companyId));
        }
        try {
            if (table != null) {
                Company item = table.getItem(Key.builder().partitionValue(companyId).build());
                return Optional.ofNullable(item);
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    @Override
    public Optional<Company> findByEmail(String email) {
        Optional<Company> inMem = localStore.values().stream()
                .filter(c -> email.equalsIgnoreCase(c.getEmail()))
                .findFirst();
        if (inMem.isPresent()) return inMem;

        try {
            if (emailIndex != null) {
                return emailIndex.query(QueryConditional.keyEqualTo(Key.builder().partitionValue(email).build()))
                        .stream()
                        .flatMap(page -> page.items().stream())
                        .findFirst();
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    @Override
    public void deleteById(String companyId) {
        localStore.remove(companyId);
        try {
            if (table != null) table.deleteItem(Key.builder().partitionValue(companyId).build());
        } catch (Exception ignored) {}
    }
}
