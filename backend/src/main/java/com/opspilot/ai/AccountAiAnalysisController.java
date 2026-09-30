package com.opspilot.ai;

import com.opspilot.ai.dto.AccountAiAnalysis;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountAiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    @PostMapping("/{accountId}/ai-analysis")
    public AccountAiAnalysis analyze(@PathVariable UUID accountId) {
        return aiAnalysisService.analyze(accountId);
    }
}
