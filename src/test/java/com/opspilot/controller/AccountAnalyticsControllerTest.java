package com.opspilot.controller;

import com.opspilot.exception.AccountNotFoundException;
import com.opspilot.service.AccountOperationalService;
import com.opspilot.service.AccountService;
import com.opspilot.service.analytics.AccountAnalyticsService;
import com.opspilot.service.analytics.model.AccountPotentialAssessment;
import com.opspilot.service.analytics.model.AccountPriorityAssessment;
import com.opspilot.service.analytics.model.AccountRiskAssessment;
import com.opspilot.service.analytics.model.PotentialLevel;
import com.opspilot.service.analytics.model.PriorityLevel;
import com.opspilot.service.analytics.model.RiskLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AccountAnalyticsController.class, AccountController.class})
class AccountAnalyticsControllerTest {

    private static final UUID ACCOUNT_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID SECOND_ACCOUNT_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID MISSING_ID = UUID.fromString("10000000-0000-0000-0000-000000000099");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountAnalyticsService accountAnalyticsService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private AccountOperationalService accountOperationalService;

    @Test
    void shouldReturnAnalyticsWithReadableBreakdowns() throws Exception {
        when(accountAnalyticsService.analyze(ACCOUNT_ID))
                .thenReturn(assessment(ACCOUNT_ID, "Horizon Supply", 91, 17, 61));

        mockMvc.perform(get("/api/accounts/{accountId}/analytics", ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(ACCOUNT_ID.toString()))
                .andExpect(jsonPath("$.accountName").value("Horizon Supply"))
                .andExpect(jsonPath("$.risk.score").value(91))
                .andExpect(jsonPath("$.risk.level").value("CRITICAL"))
                .andExpect(jsonPath("$.risk.revenueRisk").value(30))
                .andExpect(jsonPath("$.potential.score").value(17))
                .andExpect(jsonPath("$.potential.level").value("LOW"))
                .andExpect(jsonPath("$.potential.revenueStrengthPotential").value(12))
                .andExpect(jsonPath("$.priorityScore").value(61))
                .andExpect(jsonPath("$.priorityLevel").value("HIGH"));
    }

    @Test
    void shouldResolveStaticPrioritiesRouteAndPreserveServiceOrder() throws Exception {
        when(accountAnalyticsService.rankAccounts()).thenReturn(List.of(
                assessment(ACCOUNT_ID, "Horizon Supply", 91, 17, 61),
                assessment(SECOND_ACCOUNT_ID, "Pulse Systems", 25, 80, 47)
        ));

        mockMvc.perform(get("/api/accounts/priorities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountName").value("Horizon Supply"))
                .andExpect(jsonPath("$[0].riskScore").value(91))
                .andExpect(jsonPath("$[0].riskLevel").value("CRITICAL"))
                .andExpect(jsonPath("$[0].potentialScore").value(17))
                .andExpect(jsonPath("$[0].priorityScore").value(61))
                .andExpect(jsonPath("$[1].accountName").value("Pulse Systems"))
                .andExpect(jsonPath("$[1].priorityScore").value(47))
                .andExpect(jsonPath("$[0].risk").doesNotExist())
                .andExpect(jsonPath("$[0].potential").doesNotExist());
    }

    @Test
    void shouldReturnStandardErrorWhenAnalyticsAccountDoesNotExist() throws Exception {
        when(accountAnalyticsService.analyze(MISSING_ID)).thenThrow(new AccountNotFoundException(MISSING_ID));

        mockMvc.perform(get("/api/accounts/{accountId}/analytics", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Account not found: " + MISSING_ID))
                .andExpect(jsonPath("$.path").value("/api/accounts/" + MISSING_ID + "/analytics"));
    }

    private AccountPriorityAssessment assessment(
            UUID id,
            String name,
            int riskScore,
            int potentialScore,
            int priorityScore
    ) {
        AccountRiskAssessment risk = riskScore == 91
                ? new AccountRiskAssessment(91, RiskLevel.CRITICAL, 30, 20, 15, 10, 10, 6)
                : new AccountRiskAssessment(25, RiskLevel.MEDIUM, 0, 0, 15, 10, 0, 0);
        AccountPotentialAssessment potential = potentialScore == 17
                ? new AccountPotentialAssessment(17, PotentialLevel.LOW, 0, 0, 12, 5, 0)
                : new AccountPotentialAssessment(80, PotentialLevel.VERY_HIGH, 24, 25, 16, 15, 0);
        return new AccountPriorityAssessment(
                id,
                name,
                risk,
                potential,
                priorityScore,
                PriorityLevel.fromScore(priorityScore)
        );
    }
}
