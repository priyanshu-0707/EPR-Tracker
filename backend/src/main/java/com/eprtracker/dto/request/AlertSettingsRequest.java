package com.eprtracker.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertSettingsRequest {
    private Boolean emailAlertsEnabled;
    private Boolean smsAlertsEnabled;

    @Email(message = "Please provide a valid alert email")
    private String alertEmail;

    private String alertPhone;
}
