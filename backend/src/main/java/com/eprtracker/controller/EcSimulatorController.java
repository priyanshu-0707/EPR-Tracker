package com.eprtracker.controller;

import com.eprtracker.dto.request.EcSimulationRequest;
import com.eprtracker.dto.response.EcSimulationResponse;
import com.eprtracker.security.JwtTokenProvider;
import com.eprtracker.service.EcSimulatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for What-If Environmental Compensation (EC) Financial Penalty Simulation Engine.
 */
@RestController
@RequestMapping("/api/simulator")
public class EcSimulatorController {

    private final EcSimulatorService ecSimulatorService;
    private final JwtTokenProvider tokenProvider;

    public EcSimulatorController(EcSimulatorService ecSimulatorService, JwtTokenProvider tokenProvider) {
        this.ecSimulatorService = ecSimulatorService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Calculate legal Environmental Compensation penalty liability vs procurement cost ROI.
     */
    @PostMapping("/ec-liability")
    public ResponseEntity<EcSimulationResponse> simulateEcLiability(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) EcSimulationRequest request) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        EcSimulationRequest req = request != null ? request : new EcSimulationRequest();
        return ResponseEntity.ok(ecSimulatorService.simulateLiability(companyId, req));
    }
}
