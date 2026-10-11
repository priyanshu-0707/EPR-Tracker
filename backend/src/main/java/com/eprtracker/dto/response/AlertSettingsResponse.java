package com.eprtracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertSettingsResponse {
    private Boolean emailAlertsEnabled;
    private Boolean smsAlertsEnabled;
    private String alertEmail;
    private String alertPhone;
    private List<ActiveTrigger> activeTriggers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveTrigger {
        private String name;
        private String description;
        private String frequency;
        private boolean active;
    }
}
