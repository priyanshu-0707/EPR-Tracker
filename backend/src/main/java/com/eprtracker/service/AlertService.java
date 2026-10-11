package com.eprtracker.service;

import com.eprtracker.dto.request.AlertSettingsRequest;
import com.eprtracker.dto.response.AlertHistoryResponse;
import com.eprtracker.dto.response.AlertSettingsResponse;

import java.util.List;

public interface AlertService {
    AlertSettingsResponse getAlertSettings(String companyId);
    AlertSettingsResponse updateAlertSettings(String companyId, AlertSettingsRequest request);
    List<AlertHistoryResponse> getAlertHistory(String companyId);
    void sendNotification(String companyId, String title, String message);
}
