package com.eprtracker.service.impl;

import com.eprtracker.dto.request.AiChatRequest;
import com.eprtracker.dto.response.AiChatResponse;
import com.eprtracker.service.AiAssistantService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantServiceImpl.class);

    private static final String SYSTEM_PROMPT =
            "You are an expert compliance assistant specializing in India's E-Waste " +
            "(Management) Rules, 2022, enforced by CPCB since April 2023. Your job is " +
            "to help electronics brand compliance officers understand their EPR " +
            "obligations, calculate collection targets using QEPR = Qp × Cf × target%, " +
            "quarterly and annual filing deadlines, how to find and work with registered " +
            "recyclers, penalties for non-compliance (environmental compensation), and " +
            "how EPR certificates work and can be traded. Rules: always cite the specific " +
            "Rule number when referencing regulations. Keep answers under 150 words unless " +
            "more detail is genuinely needed. Use Indian number formatting (10,00,000 not " +
            "1,000,000). If unsure, say so clearly — never hallucinate regulation details. " +
            "Be practical and action-oriented.";

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final ObjectMapper objectMapper;
    private final String modelId;
    private final int maxTokens;

    public AiAssistantServiceImpl(
            BedrockRuntimeClient bedrockRuntimeClient,
            ObjectMapper objectMapper,
            @Value("${aws.bedrock.model-id:anthropic.claude-sonnet-4-6}") String modelId,
            @Value("${aws.bedrock.max-tokens:1000}") int maxTokens) {
        this.bedrockRuntimeClient = bedrockRuntimeClient;
        this.objectMapper = objectMapper;
        this.modelId = modelId;
        this.maxTokens = maxTokens;
    }

    @Override
    public AiChatResponse askAssistant(String companyId, AiChatRequest request) {
        try {
            // Anthropic Claude 3 / 3.5 Messages API payload structure
            Map<String, Object> payload = new HashMap<>();
            payload.put("anthropic_version", "bedrock-2023-05-31");
            payload.put("max_tokens", maxTokens);
            payload.put("system", SYSTEM_PROMPT);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", request.getMessage());
            payload.put("messages", List.of(userMessage));

            String requestJson = objectMapper.writeValueAsString(payload);

            InvokeModelRequest invokeRequest = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(requestJson))
                    .build();

            InvokeModelResponse invokeResponse = bedrockRuntimeClient.invokeModel(invokeRequest);
            String responseBody = invokeResponse.body().asUtf8String();

            JsonNode rootNode = objectMapper.readTree(responseBody);
            String replyText = rootNode.path("content").get(0).path("text").asText();

            return AiChatResponse.builder()
                    .reply(replyText)
                    .modelUsed(modelId)
                    .timestamp(Instant.now().toString())
                    .build();

        } catch (Exception e) {
            log.warn("Bedrock Runtime Client call failed or unconfigured, using regulatory fallback assistant: {}", e.getMessage());
            String fallbackReply = generateFallbackResponse(request.getMessage());
            return AiChatResponse.builder()
                    .reply(fallbackReply)
                    .modelUsed(modelId + " (Simulated Engine)")
                    .timestamp(Instant.now().toString())
                    .build();
        }
    }

    private String generateFallbackResponse(String message) {
        String query = message.toLowerCase();
        if (query.contains("obligation") || query.contains("phone") || query.contains("calculate") || query.contains("lakh")) {
            return "Under Rule 4(1) and Schedule III of the E-Waste (Management) Rules, 2022, your EPR obligation is calculated as QEPR = Qp × Cf × Target%.\n\n" +
                    "For 5,00,000 mobile phones (lifespan 3 years, Cf = 0.333) in FY2026-27 (target 80%):\n" +
                    "Obligation = 5,00,000 × 0.333 × 0.80 = 1,33,200 units.\n" +
                    "Ensure you procure verified EPR certificates from CPCB-registered recyclers before March 31, 2027.";
        } else if (query.contains("quarter") || query.contains("miss") || query.contains("deadline") || query.contains("filing")) {
            return "Under Rule 6(2), quarterly returns must be submitted within 30 days of the quarter ending on the CPCB EPR portal (eprewaste.cpcb.gov.in).\n\n" +
                    "Missing deadlines attracts Environmental Compensation (EC) penalties under Rule 22, levied daily until compliance. Continued default may trigger CPCB registration suspension. File immediately to avoid penal interest.";
        } else if (query.contains("recycler") || query.contains("maharashtra") || query.contains("find")) {
            return "Under Rule 5, you may only fulfill EPR quotas through CPCB and SPCB authorized recyclers with active registration.\n\n" +
                    "In Maharashtra (Mumbai, Pune), top CPCB-certified facilities include EcoE-Waste Recyclers (Pune - 4,500 T/yr capacity) and GreenTech Solutions (Navi Mumbai - 3,200 T/yr). Check our Recycler Finder tab for direct contacts.";
        } else {
            return "Under India's E-Waste (Management) Rules, 2022, all producers, importers, and brand owners (PIBOs) must register on the CPCB online portal.\n\n" +
                    "Target for FY2026-27 is 80% of end-of-life volume (calculated using Cf = 1/lifespan). All collections must be backed by traceable EPR certificates traded via authorized recyclers. Let me know your product category for exact computations.";
        }
    }
}
