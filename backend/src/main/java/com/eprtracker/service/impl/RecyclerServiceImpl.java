package com.eprtracker.service.impl;

import com.eprtracker.dto.response.RecyclerResponse;
import com.eprtracker.entity.Recycler;
import com.eprtracker.exception.ResourceNotFoundException;
import com.eprtracker.repository.RecyclerRepository;
import com.eprtracker.service.RecyclerService;
import com.eprtracker.util.IndianNumberFormatter;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecyclerServiceImpl implements RecyclerService {

    private final RecyclerRepository recyclerRepository;

    public RecyclerServiceImpl(RecyclerRepository recyclerRepository) {
        this.recyclerRepository = recyclerRepository;
    }

    @Override
    public List<RecyclerResponse> getRecyclers(String state, String category) {
        List<Recycler> recyclers;
        if ((state != null && !state.isBlank()) || (category != null && !category.isBlank())) {
            recyclers = recyclerRepository.findByFilters(state, category);
        } else {
            recyclers = recyclerRepository.findAll();
        }

        return recyclers.stream()
                .map(r -> mapToResponse(r, null))
                .collect(Collectors.toList());
    }

    @Override
    public RecyclerResponse getRecyclerById(String recyclerId) {
        Recycler recycler = recyclerRepository.findById(recyclerId)
                .orElseThrow(() -> new ResourceNotFoundException("Recycler not found with id: " + recyclerId));
        return mapToResponse(recycler, null);
    }

    @Override
    public List<RecyclerResponse> getNearbyRecyclers(Double lat, Double lng, Double radiusKm) {
        double maxDist = (radiusKm != null && radiusKm > 0) ? radiusKm : 500.0;
        List<Recycler> all = recyclerRepository.findAll();

        return all.stream()
                .filter(r -> r.getLatitude() != null && r.getLongitude() != null)
                .map(r -> {
                    double dist = calculateDistance(lat, lng, r.getLatitude(), r.getLongitude());
                    return mapToResponse(r, dist);
                })
                .filter(r -> r.getDistanceKm() <= maxDist)
                .sorted(Comparator.comparingDouble(RecyclerResponse::getDistanceKm))
                .collect(Collectors.toList());
    }

    private RecyclerResponse mapToResponse(Recycler r, Double distance) {
        return RecyclerResponse.builder()
                .recyclerId(r.getRecyclerId())
                .name(r.getName())
                .state(r.getState())
                .city(r.getCity())
                .latitude(r.getLatitude())
                .longitude(r.getLongitude())
                .categoriesHandled(r.getCategoriesHandled())
                .cpcbCertified(r.getCpcbCertified())
                .contactEmail(r.getContactEmail())
                .contactPhone(r.getContactPhone())
                .capacityTonnesPerYear(r.getCapacityTonnesPerYear())
                .formattedCapacity(IndianNumberFormatter.format(r.getCapacityTonnesPerYear() != null ? r.getCapacityTonnesPerYear() : 0L) + " T/yr")
                .distanceKm(distance != null ? Math.round(distance * 10.0) / 10.0 : null)
                .build();
    }

    // Haversine formula for distance in kilometers
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
