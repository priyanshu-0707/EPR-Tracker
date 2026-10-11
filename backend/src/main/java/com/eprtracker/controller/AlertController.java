package com.eprtracker.controller;

import com.eprtracker.dto.request.AlertSettingsRequest;
import com.eprtracker.dto.response.AlertHistoryResponse;
import com.eprtracker.dto.response.AlertSettingsResponse;
import com.eprtracker.security.JwtTokenProvider;
import com.eprtracker.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller managing notification settings and audit log for SNS alerts.
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;
    private final JwtTokenProvider tokenProvider;

    public AlertController(AlertService alertService, JwtTokenProvider tokenProvider) {
        this.alertService = alertService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Retrieve active alert channels and triggers.
     */
    @GetMapping("/settings")
    public ResponseEntity<AlertSettingsResponse> getSettings(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        return ResponseEntity.ok(alertService.getAlertSettings(companyId));
    }

    /**
     * Update notification email, phone, and channel toggles.
     */
    @PutMapping("/settings")
    public ResponseEntity<AlertSettingsResponse> updateSettings(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AlertSettingsRequest request) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        return ResponseEntity.ok(alertService.updateAlertSettings(companyId, request));
    }

    /**
     * Retrieve audit history of dispatched alerts and compliance reminders.
     */
    @GetMapping("/history")
    public ResponseEntity<List<AlertHistoryResponse>> getHistory(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        return ResponseEntity.ok(alertService.getAlertHistory(companyId));
    }
}
