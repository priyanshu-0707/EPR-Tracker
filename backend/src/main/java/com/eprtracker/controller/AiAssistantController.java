package com.eprtracker.controller;

import com.eprtracker.dto.request.AiChatRequest;
import com.eprtracker.dto.response.AiChatResponse;
import com.eprtracker.security.JwtTokenProvider;
import com.eprtracker.service.AiAssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller providing AI compliance advisory via AWS Bedrock (Claude 3.5 Sonnet).
 */
@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;
    private final JwtTokenProvider tokenProvider;

    public AiAssistantController(AiAssistantService aiAssistantService, JwtTokenProvider tokenProvider) {
        this.aiAssistantService = aiAssistantService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Ask CPCB compliance question with automatic Rule citation and Indian numeral guidance.
     */
    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AiChatRequest request) {
        String token = authHeader.replace("Bearer ", "");
        String companyId = tokenProvider.getCompanyIdFromToken(token);
        AiChatResponse response = aiAssistantService.askAssistant(companyId, request);
        return ResponseEntity.ok(response);
    }
}
