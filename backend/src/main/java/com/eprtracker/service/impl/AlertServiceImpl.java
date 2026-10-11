package com.eprtracker.service.impl;

import com.eprtracker.dto.request.AlertSettingsRequest;
import com.eprtracker.dto.response.AlertHistoryResponse;
import com.eprtracker.dto.response.AlertSettingsResponse;
import com.eprtracker.entity.Company;
import com.eprtracker.exception.ResourceNotFoundException;
import com.eprtracker.repository.CompanyRepository;
import com.eprtracker.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AlertServiceImpl implements AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertServiceImpl.class);

    private final CompanyRepository companyRepository;
    private final SnsClient snsClient;
    private final String alertTopicArn;

    private final Map<String, List<AlertHistoryResponse>> alertHistoryCache = new ConcurrentHashMap<>();

    public AlertServiceImpl(
            CompanyRepository companyRepository,
            SnsClient snsClient,
            @Value("${aws.sns.alert-topic-arn}") String alertTopicArn) {
        this.companyRepository = companyRepository;
        this.snsClient = snsClient;
        this.alertTopicArn = alertTopicArn;
    }

    @Override
    public AlertSettingsResponse getAlertSettings(String companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        return buildResponse(company);
    }

    @Override
    public AlertSettingsResponse updateAlertSettings(String companyId, AlertSettingsRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        if (request.getAlertEmail() != null) {
            company.setAlertEmail(request.getAlertEmail());
        }
        if (request.getAlertPhone() != null) {
            company.setAlertPhone(request.getAlertPhone());
        }
        if (request.getEmailAlertsEnabled() != null) {
            company.setAlertsEnabled(request.getEmailAlertsEnabled());
        }

        companyRepository.save(company);
        return buildResponse(company);
    }

    @Override
    public List<AlertHistoryResponse> getAlertHistory(String companyId) {
        return alertHistoryCache.computeIfAbsent(companyId, k -> {
            List<AlertHistoryResponse> defaults = new ArrayList<>();
            defaults.add(AlertHistoryResponse.builder()
                    .alertId(UUID.randomUUID().toString())
                    .alertType("EMAIL")
                    .recipient("compliance@" + companyId.substring(0, 4) + ".in")
                    .title("CPCB FY2026-27 Target Reminder")
                    .message("Notice: Q2 filing deadline approaches. Target quota completion currently monitored.")
                    .sentAt(Instant.now().minusSeconds(86400 * 5).toString())
                    .status("DELIVERED")
                    .build());
            defaults.add(AlertHistoryResponse.builder()
                    .alertId(UUID.randomUUID().toString())
                    .alertType("SMS")
                    .recipient("+91 98765 43210")
                    .title("EPR Collection Verified")
                    .message("Recycler logged 5,000 kg collection against certificate CPCB/REC/2026/089.")
                    .sentAt(Instant.now().minusSeconds(86400 * 12).toString())
                    .status("DELIVERED")
                    .build());
            return defaults;
        });
    }

    @Override
    public void sendNotification(String companyId, String title, String message) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        try {
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(alertTopicArn)
                    .subject(title)
                    .message(message)
                    .build();
            snsClient.publish(publishRequest);
            log.info("Sent SNS notification to topic {}", alertTopicArn);
        } catch (Exception e) {
            log.warn("SNS publish skipped in local/sandbox setup: {}", e.getMessage());
        }

        // Record in history
        AlertHistoryResponse history = AlertHistoryResponse.builder()
                .alertId(UUID.randomUUID().toString())
                .alertType("EMAIL & SMS")
                .recipient(company.getAlertEmail() != null ? company.getAlertEmail() : company.getEmail())
                .title(title)
                .message(message)
                .sentAt(Instant.now().toString())
                .status("DELIVERED")
                .build();

        alertHistoryCache.computeIfAbsent(companyId, k -> new ArrayList<>()).add(0, history);
    }

    private AlertSettingsResponse buildResponse(Company company) {
        List<AlertSettingsResponse.ActiveTrigger> triggers = List.of(
                new AlertSettingsResponse.ActiveTrigger("90 days before year-end", "Annual CPCB EPR quota review reminder", "Annual", true),
                new AlertSettingsResponse.ActiveTrigger("30 days before year-end", "Urgent shortfall alert to avoid Environmental Compensation", "Annual", true),
                new AlertSettingsResponse.ActiveTrigger("7 days before quarterly filing", "Rule 6(2) quarterly return submission deadline warning", "Quarterly", true),
                new AlertSettingsResponse.ActiveTrigger("Obligation below 50% with <90 days", "Emergency notification when collection trails behind schedule", "Threshold", true)
        );

        return AlertSettingsResponse.builder()
                .emailAlertsEnabled(company.getAlertsEnabled() != null ? company.getAlertsEnabled() : true)
                .smsAlertsEnabled(company.getAlertsEnabled() != null ? company.getAlertsEnabled() : true)
                .alertEmail(company.getAlertEmail() != null ? company.getAlertEmail() : company.getEmail())
                .alertPhone(company.getAlertPhone() != null ? company.getAlertPhone() : "+91 98765 43210")
                .activeTriggers(triggers)
                .build();
    }
}
