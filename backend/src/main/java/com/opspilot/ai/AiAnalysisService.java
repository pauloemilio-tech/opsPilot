package com.opspilot.ai;

import com.opspilot.ai.dto.AccountAiAnalysis;
import com.opspilot.ai.model.AccountAnalysisContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    private final AccountAnalysisContextBuilder contextBuilder;
    private final LlmClient llmClient;

    public AccountAiAnalysis analyze(UUID accountId) {
        AccountAnalysisContext context = contextBuilder.build(accountId);
        return llmClient.analyze(context);
    }
}
