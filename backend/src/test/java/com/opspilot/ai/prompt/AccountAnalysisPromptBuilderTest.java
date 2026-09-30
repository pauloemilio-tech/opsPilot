package com.opspilot.ai.prompt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opspilot.ai.model.AccountAnalysisContext;
import com.opspilot.ai.model.AccountAnalysisContext.PotentialContext;
import com.opspilot.ai.model.AccountAnalysisContext.PriorityContext;
import com.opspilot.ai.model.AccountAnalysisContext.RiskContext;
import com.opspilot.model.enums.AccountStatus;
import com.opspilot.service.analytics.model.PotentialLevel;
import com.opspilot.service.analytics.model.PriorityLevel;
import com.opspilot.service.analytics.model.RiskLevel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountAnalysisPromptBuilderTest {

    @Test
    void shouldIncludeTrustedFactsOutputContractAndDeterminismRules() {
        AccountAnalysisContext context = context();

        String prompt = new AccountAnalysisPromptBuilder(new ObjectMapper()).build(context);

        assertThat(prompt)
                .contains("Use only facts explicitly present")
                .contains("Never recalculate, replace, challenge, or modify")
                .contains("Return one valid JSON object only")
                .contains("Do not include Risk, Potential, or Priority replacement fields")
                .contains("\"accountName\" : \"Horizon Supply\"")
                .contains("\"score\" : 95")
                .contains("\"score\" : 12")
                .contains("\"score\" : 62")
                .contains("\"latestInteractionAt\" : null");
    }

    private AccountAnalysisContext context() {
        return new AccountAnalysisContext(
                UUID.randomUUID(),
                "Horizon Supply",
                AccountStatus.ACTIVE,
                "Distribution",
                "LATAM",
                new BigDecimal("29000.00"),
                new BigDecimal("61000.00"),
                new BigDecimal("-52.46"),
                18,
                4,
                4,
                1,
                null,
                new RiskContext(95, RiskLevel.CRITICAL, 30, 20, 15, 15, 10, 5),
                new PotentialContext(12, PotentialLevel.LOW, 0, 0, 12, 0, 0),
                new PriorityContext(62, PriorityLevel.HIGH)
        );
    }
}
