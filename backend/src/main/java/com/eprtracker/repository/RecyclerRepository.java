package com.eprtracker.repository;

import com.eprtracker.entity.Recycler;

import java.util.List;
import java.util.Optional;

public interface RecyclerRepository {
    void save(Recycler recycler);
    Optional<Recycler> findById(String recyclerId);
    List<Recycler> findAll();
    List<Recycler> findByState(String state);
    List<Recycler> findByFilters(String state, String category);
}
