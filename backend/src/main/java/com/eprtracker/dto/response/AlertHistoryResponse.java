package com.eprtracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertHistoryResponse {
    private String alertId;
    private String alertType; // EMAIL, SMS
    private String recipient;
    private String title;
    private String message;
    private String sentAt;
    private String status; // DELIVERED, PENDING
}
