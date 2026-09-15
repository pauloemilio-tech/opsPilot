package com.opspilot.service.analytics;

import com.opspilot.service.analytics.model.AccountPotentialAssessment;
import com.opspilot.service.analytics.model.AccountPriorityAssessment;
import com.opspilot.service.analytics.model.AccountRiskAssessment;
import com.opspilot.service.analytics.model.PotentialLevel;
import com.opspilot.service.analytics.model.PriorityLevel;
import com.opspilot.service.analytics.model.RiskLevel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountPriorityServiceTest {

    private final AccountPriorityService service = new AccountPriorityService();

    @ParameterizedTest
    @CsvSource({
            "10,10,10,LOW",
            "80,20,56,HIGH",
            "50,80,62,HIGH",
            "80,80,80,URGENT"
    })
    void shouldCombineRiskAndPotentialWithExplicitWeights(
            int riskScore,
            int potentialScore,
            int expectedScore,
            PriorityLevel expectedLevel
    ) {
        AccountPriorityAssessment result = service.assess(
                UUID.randomUUID(),
                "Account",
                risk(riskScore),
                potential(potentialScore)
        );

        assertThat(result.priorityScore()).isEqualTo(expectedScore);
        assertThat(result.priorityLevel()).isEqualTo(expectedLevel);
    }

    @ParameterizedTest
    @CsvSource({"0,LOW", "24,LOW", "25,MEDIUM", "49,MEDIUM", "50,HIGH", "74,HIGH", "75,URGENT", "100,URGENT"})
    void shouldClassifyPriorityLevelAtCentralizedBoundaries(int score, PriorityLevel expectedLevel) {
        assertThat(PriorityLevel.fromScore(score)).isEqualTo(expectedLevel);
    }

    private AccountRiskAssessment risk(int score) {
        int remaining = score;
        int revenue = take(remaining, 30);
        remaining -= revenue;
        int delayed = take(remaining, 20);
        remaining -= delayed;
        int support = take(remaining, 15);
        remaining -= support;
        int critical = take(remaining, 15);
        remaining -= critical;
        int engagement = take(remaining, 10);
        remaining -= engagement;
        int inactivity = take(remaining, 10);
        return new AccountRiskAssessment(
                score,
                RiskLevel.fromScore(score),
                revenue,
                delayed,
                support,
                critical,
                engagement,
                inactivity
        );
    }

    private AccountPotentialAssessment potential(int score) {
        int remaining = score;
        int growth = take(remaining, 30);
        remaining -= growth;
        int engagement = take(remaining, 25);
        remaining -= engagement;
        int revenue = take(remaining, 20);
        remaining -= revenue;
        int interaction = take(remaining, 15);
        remaining -= interaction;
        int stability = take(remaining, 10);
        return new AccountPotentialAssessment(
                score,
                PotentialLevel.fromScore(score),
                growth,
                engagement,
                revenue,
                interaction,
                stability
        );
    }

    private int take(int remaining, int maximum) {
        return Math.min(remaining, maximum);
    }
}
