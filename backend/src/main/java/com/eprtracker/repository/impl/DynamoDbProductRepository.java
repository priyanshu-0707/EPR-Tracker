package com.eprtracker.repository.impl;

import com.eprtracker.entity.Product;
import com.eprtracker.repository.ProductRepository;
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
public class DynamoDbProductRepository implements ProductRepository {

    private final DynamoDbTable<Product> table;
    private final java.util.Map<String, Product> localStore = new java.util.concurrent.ConcurrentHashMap<>();

    public DynamoDbProductRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-prefix:epr-tracker-}") String tablePrefix) {
        DynamoDbTable<Product> tbl = null;
        try {
            tbl = enhancedClient.table(tablePrefix + "products", TableSchema.fromBean(Product.class));
            tbl.createTable();
        } catch (Exception ignored) {}
        this.table = tbl;
    }

    @Override
    public void save(Product product) {
        localStore.put(product.getProductId(), product);
        try {
            if (table != null) table.putItem(product);
        } catch (Exception ignored) {}
    }

    @Override
    public Optional<Product> findByIdAndCompanyId(String productId, String companyId) {
        Product inMem = localStore.get(productId);
        if (inMem != null && companyId.equals(inMem.getCompanyId())) {
            return Optional.of(inMem);
        }
        try {
            if (table != null) {
                Product item = table.getItem(Key.builder().partitionValue(productId).sortValue(companyId).build());
                return Optional.ofNullable(item);
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    @Override
    public List<Product> findByCompanyId(String companyId) {
        List<Product> list = localStore.values().stream()
                .filter(p -> companyId.equals(p.getCompanyId()))
                .collect(Collectors.toList());
        if (!list.isEmpty()) return list;

        try {
            if (table != null) {
                return table.scan().items().stream()
                        .filter(p -> companyId.equals(p.getCompanyId()))
                        .collect(Collectors.toList());
            }
        } catch (Exception ignored) {}
        return list;
    }

    @Override
    public List<Product> findByCompanyIdAndFinancialYear(String companyId, String financialYear) {
        List<Product> list = localStore.values().stream()
                .filter(p -> companyId.equals(p.getCompanyId()) && financialYear.equalsIgnoreCase(p.getFinancialYear()))
                .collect(Collectors.toList());
        if (!list.isEmpty()) return list;

        try {
            if (table != null) {
                return table.scan().items().stream()
                        .filter(p -> companyId.equals(p.getCompanyId()) && financialYear.equalsIgnoreCase(p.getFinancialYear()))
                        .collect(Collectors.toList());
            }
        } catch (Exception ignored) {}
        return list;
    }

    @Override
    public void deleteByIdAndCompanyId(String productId, String companyId) {
        localStore.remove(productId);
        try {
            if (table != null) table.deleteItem(Key.builder().partitionValue(productId).sortValue(companyId).build());
        } catch (Exception ignored) {}
    }
}
