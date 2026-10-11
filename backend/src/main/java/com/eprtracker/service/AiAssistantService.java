package com.eprtracker.service;

import com.eprtracker.dto.request.AiChatRequest;
import com.eprtracker.dto.response.AiChatResponse;

public interface AiAssistantService {
    AiChatResponse askAssistant(String companyId, AiChatRequest request);
}
