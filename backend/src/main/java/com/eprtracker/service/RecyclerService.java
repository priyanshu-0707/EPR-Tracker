package com.eprtracker.service;

import com.eprtracker.dto.response.RecyclerResponse;

import java.util.List;

public interface RecyclerService {
    List<RecyclerResponse> getRecyclers(String state, String category);
    RecyclerResponse getRecyclerById(String recyclerId);
    List<RecyclerResponse> getNearbyRecyclers(Double lat, Double lng, Double radiusKm);
}
