package com.opspilot.ai;

import com.opspilot.ai.dto.AccountAiAnalysis;
import com.opspilot.ai.model.AccountAnalysisContext;

public interface LlmClient {
    AccountAiAnalysis analyze(AccountAnalysisContext context);
}
