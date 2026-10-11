package com.eprtracker.controller;

import com.eprtracker.dto.response.RecyclerResponse;
import com.eprtracker.service.RecyclerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller providing access to CPCB certified recyclers and geospatial locator.
 */
@RestController
@RequestMapping("/api/recyclers")
public class RecyclerController {

    private final RecyclerService recyclerService;

    public RecyclerController(RecyclerService recyclerService) {
        this.recyclerService = recyclerService;
    }

    /**
     * Retrieve recyclers filtered by Indian state and product category handled.
     */
    @GetMapping
    public ResponseEntity<List<RecyclerResponse>> getRecyclers(
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "category", required = false) String category) {
        return ResponseEntity.ok(recyclerService.getRecyclers(state, category));
    }

    /**
     * Retrieve single recycler details by unique ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RecyclerResponse> getRecyclerById(@PathVariable("id") String recyclerId) {
        return ResponseEntity.ok(recyclerService.getRecyclerById(recyclerId));
    }

    /**
     * Find recyclers nearest to given geographic coordinates.
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<RecyclerResponse>> getNearbyRecyclers(
            @RequestParam("lat") Double lat,
            @RequestParam("lng") Double lng,
            @RequestParam(value = "radiusKm", required = false) Double radiusKm) {
        return ResponseEntity.ok(recyclerService.getNearbyRecyclers(lat, lng, radiusKm));
    }
}
