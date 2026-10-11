package com.eprtracker.repository.impl;

import com.eprtracker.entity.Recycler;
import com.eprtracker.repository.RecyclerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class DynamoDbRecyclerRepository implements RecyclerRepository {

    private final DynamoDbTable<Recycler> table;
    private final DynamoDbIndex<Recycler> stateIndex;
    private final java.util.Map<String, Recycler> localStore = new java.util.concurrent.ConcurrentHashMap<>();

    public DynamoDbRecyclerRepository(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.table-prefix:epr-tracker-}") String tablePrefix) {
        DynamoDbTable<Recycler> tbl = null;
        DynamoDbIndex<Recycler> idx = null;
        try {
            tbl = enhancedClient.table(tablePrefix + "recyclers", TableSchema.fromBean(Recycler.class));
            tbl.createTable();
            idx = tbl.index("state-index");
        } catch (Exception ignored) {}
        this.table = tbl;
        this.stateIndex = idx;
    }

    @Override
    public void save(Recycler recycler) {
        localStore.put(recycler.getRecyclerId(), recycler);
        try {
            if (table != null) table.putItem(recycler);
        } catch (Exception ignored) {}
    }

    @Override
    public Optional<Recycler> findById(String recyclerId) {
        if (localStore.containsKey(recyclerId)) {
            return Optional.of(localStore.get(recyclerId));
        }
        try {
            if (table != null) {
                Recycler item = table.getItem(Key.builder().partitionValue(recyclerId).build());
                return Optional.ofNullable(item);
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    @Override
    public List<Recycler> findAll() {
        if (!localStore.isEmpty()) {
            return new java.util.ArrayList<>(localStore.values());
        }
        try {
            if (table != null) {
                return table.scan().items().stream().collect(Collectors.toList());
            }
        } catch (Exception ignored) {}
        return new java.util.ArrayList<>(localStore.values());
    }

    @Override
    public List<Recycler> findByState(String state) {
        List<Recycler> inMem = localStore.values().stream()
                .filter(r -> state.equalsIgnoreCase(r.getState()))
                .collect(Collectors.toList());
        if (!inMem.isEmpty()) return inMem;

        try {
            if (stateIndex != null) {
                return stateIndex.query(QueryConditional.keyEqualTo(Key.builder().partitionValue(state).build()))
                        .stream()
                        .flatMap(page -> page.items().stream())
                        .collect(Collectors.toList());
            }
        } catch (Exception ignored) {}
        return inMem;
    }

    @Override
    public List<Recycler> findByFilters(String state, String category) {
        return localStore.values().stream()
                .filter(r -> (state == null || state.isBlank() || state.equalsIgnoreCase(r.getState()))
                        && (category == null || category.isBlank() || (r.getCategoriesHandled() != null && r.getCategoriesHandled().contains(category))))
                .collect(Collectors.toList());
    }
}
