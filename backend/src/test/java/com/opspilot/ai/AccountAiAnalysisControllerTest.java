package com.opspilot.ai;

import com.opspilot.ai.dto.AccountAiAnalysis;
import com.opspilot.ai.dto.RecommendedAction;
import com.opspilot.ai.exception.AiConfigurationException;
import com.opspilot.ai.exception.AiProviderResponseException;
import com.opspilot.ai.exception.AiProviderUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountAiAnalysisController.class)
class AccountAiAnalysisControllerTest {

    private static final UUID ACCOUNT_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiAnalysisService aiAnalysisService;

    @Test
    void shouldReturnStructuredAnalysisWithoutReplacementScores() throws Exception {
        when(aiAnalysisService.analyze(ACCOUNT_ID)).thenReturn(new AccountAiAnalysis(
                "The account has elevated support pressure.",
                List.of("Critical support tickets remain open."),
                List.of(new RecommendedAction(
                        "Review unresolved critical tickets.",
                        "The account currently has critical open tickets."
                ))
        ));

        mockMvc.perform(post("/api/accounts/{accountId}/ai-analysis", ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("The account has elevated support pressure."))
                .andExpect(jsonPath("$.keyConcerns[0]").value("Critical support tickets remain open."))
                .andExpect(jsonPath("$.recommendedActions[0].action")
                        .value("Review unresolved critical tickets."))
                .andExpect(jsonPath("$.recommendedActions[0].evidence")
                        .value("The account currently has critical open tickets."))
                .andExpect(jsonPath("$.riskScore").doesNotExist())
                .andExpect(jsonPath("$.potentialScore").doesNotExist())
                .andExpect(jsonPath("$.priorityScore").doesNotExist());
    }

    @Test
    void shouldReturnServiceUnavailableWhenAiIsNotConfigured() throws Exception {
        when(aiAnalysisService.analyze(ACCOUNT_ID)).thenThrow(new AiConfigurationException());

        mockMvc.perform(post("/api/accounts/{accountId}/ai-analysis", ACCOUNT_ID))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("AI Account Analyst is not configured"))
                .andExpect(jsonPath("$.path").value("/api/accounts/" + ACCOUNT_ID + "/ai-analysis"));
    }

    @Test
    void shouldReturnBadGatewayForInvalidProviderResponse() throws Exception {
        when(aiAnalysisService.analyze(ACCOUNT_ID)).thenThrow(new AiProviderResponseException());

        mockMvc.perform(post("/api/accounts/{accountId}/ai-analysis", ACCOUNT_ID))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("AI Account Analyst returned an invalid response"));
    }

    @Test
    void shouldReturnServiceUnavailableWhenProviderCannotBeReached() throws Exception {
        when(aiAnalysisService.analyze(ACCOUNT_ID)).thenThrow(new AiProviderUnavailableException());

        mockMvc.perform(post("/api/accounts/{accountId}/ai-analysis", ACCOUNT_ID))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("AI Account Analyst is temporarily unavailable"));
    }
}
