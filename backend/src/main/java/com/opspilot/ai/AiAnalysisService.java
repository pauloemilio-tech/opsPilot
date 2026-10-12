package com.opspilot.ai;

import com.opspilot.ai.dto.AccountAiAnalysis;
import com.opspilot.ai.model.AccountAnalysisContext;
import com.opspilot.ai.quota.AiDemoQuotaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    private final AccountAnalysisContextBuilder contextBuilder;
    private final LlmClient llmClient;
    private final AiDemoQuotaService aiDemoQuotaService;

    public AccountAiAnalysis analyze(UUID accountId, HttpServletRequest request) {
        aiDemoQuotaService.reserveRequest(request);
        AccountAnalysisContext context = contextBuilder.build(accountId);
        return llmClient.analyze(context);
    }
}
