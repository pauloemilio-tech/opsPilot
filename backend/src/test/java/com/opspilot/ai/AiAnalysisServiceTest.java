package com.opspilot.ai;

import com.opspilot.ai.dto.AccountAiAnalysis;
import com.opspilot.ai.dto.RecommendedAction;
import com.opspilot.ai.model.AccountAnalysisContext;
import com.opspilot.ai.quota.AiDemoQuotaService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiAnalysisServiceTest {

    @Mock
    private AccountAnalysisContextBuilder contextBuilder;

    @Mock
    private LlmClient llmClient;

    @Mock
    private AiDemoQuotaService aiDemoQuotaService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private AccountAnalysisContext context;

    @Test
    void shouldPassTrustedContextToLlmAndReturnInterpretationUnchanged() {
        UUID accountId = UUID.randomUUID();
        AccountAiAnalysis analysis = new AccountAiAnalysis(
                "Support pressure is elevated.",
                List.of("Critical tickets remain open."),
                List.of(new RecommendedAction(
                        "Review critical tickets with support.",
                        "The trusted context reports critical open tickets."
                ))
        );
        when(contextBuilder.build(accountId)).thenReturn(context);
        when(llmClient.analyze(context)).thenReturn(analysis);

        AccountAiAnalysis result = new AiAnalysisService(contextBuilder, llmClient, aiDemoQuotaService).analyze(accountId, request);

        assertThat(result).isSameAs(analysis);
        verify(aiDemoQuotaService).reserveRequest(request);
        verify(contextBuilder).build(accountId);
        verify(llmClient).analyze(context);
    }
}
