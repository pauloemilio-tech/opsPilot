package com.opspilot.service.analytics;

import com.opspilot.service.analytics.model.AccountPotentialAssessment;
import com.opspilot.service.analytics.model.PotentialLevel;
import com.opspilot.service.model.AccountOperationalSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountPotentialScoringServiceTest {

    private static final Instant REFERENCE_TIME = Instant.parse("2026-09-01T12:00:00Z");
    private final AccountPotentialScoringService service = new AccountPotentialScoringService();

    @Test
    void shouldAssessLowPotential() {
        AccountPotentialAssessment result = service.assess(
                snapshot("0", "0", 0, 10, 10, 10, Optional.empty()),
                REFERENCE_TIME
        );

        assertThat(result.score()).isZero();
        assertThat(result.level()).isEqualTo(PotentialLevel.LOW);
    }

    @Test
    void shouldRewardHighEngagement() {
        AccountPotentialAssessment result = service.assess(
                snapshot("0", "0", 90, 10, 10, 10, Optional.empty()),
                REFERENCE_TIME
        );

        assertThat(result.engagementPotential()).isEqualTo(25);
    }

    @ParameterizedTest
    @CsvSource({"3,8", "10,16", "20,24", "40,30"})
    void shouldRewardRevenueGrowthByBand(String growth, int expectedPotential) {
        AccountPotentialAssessment result = service.assess(
                snapshot(growth, "0", 0, 10, 10, 10, Optional.empty()),
                REFERENCE_TIME
        );

        assertThat(result.revenueGrowthPotential()).isEqualTo(expectedPotential);
    }

    @ParameterizedTest
    @CsvSource({"0,0", "5000,4", "15000,8", "30000,12", "75000,16", "120000,20"})
    void shouldRewardCurrentRevenueStrength(String revenue, int expectedPotential) {
        AccountPotentialAssessment result = service.assess(
                snapshot("0", revenue, 0, 10, 10, 10, Optional.empty()),
                REFERENCE_TIME
        );

        assertThat(result.revenueStrengthPotential()).isEqualTo(expectedPotential);
    }

    @ParameterizedTest
    @CsvSource({"1,15", "14,10", "45,5", "90,0"})
    void shouldRewardRecentInteraction(long inactiveDays, int expectedPotential) {
        AccountPotentialAssessment result = service.assess(
                snapshot("0", "0", 0, 10, 10, 10, interaction(inactiveDays)),
                REFERENCE_TIME
        );

        assertThat(result.interactionPotential()).isEqualTo(expectedPotential);
    }

    @ParameterizedTest
    @CsvSource({"0,0,0,10", "1,0,0,7", "1,1,0,4", "1,0,1,0", "5,5,5,0"})
    void shouldReducePotentialAsOperationalFrictionGrows(
            long delayed,
            long open,
            long critical,
            int expectedPotential
    ) {
        AccountPotentialAssessment result = service.assess(
                snapshot("0", "0", 0, delayed, open, critical, Optional.empty()),
                REFERENCE_TIME
        );

        assertThat(result.operationalStabilityPotential()).isEqualTo(expectedPotential);
    }

    @Test
    void shouldAssessHighPotentialAndLimitScoreToOneHundred() {
        AccountPotentialAssessment result = service.assess(
                snapshot("100", "250000", 100, 0, 0, 0, interaction(1)),
                REFERENCE_TIME
        );

        assertThat(result.score()).isEqualTo(100);
        assertThat(result.level()).isEqualTo(PotentialLevel.VERY_HIGH);
        assertThat(factorTotal(result)).isEqualTo(100);
    }

    @Test
    void shouldTreatOnboardingGrowthFromZeroAsBoundedPotential() {
        AccountPotentialAssessment result = service.assess(
                snapshot("100", "8500", 74, 0, 1, 0, interaction(1)),
                REFERENCE_TIME
        );

        assertThat(result.revenueGrowthPotential()).isEqualTo(30);
        assertThat(result.score()).isLessThanOrEqualTo(100);
    }

    @ParameterizedTest
    @CsvSource({"0,LOW", "24,LOW", "25,MEDIUM", "49,MEDIUM", "50,HIGH", "74,HIGH", "75,VERY_HIGH", "100,VERY_HIGH"})
    void shouldClassifyPotentialLevelAtCentralizedBoundaries(int score, PotentialLevel expectedLevel) {
        assertThat(PotentialLevel.fromScore(score)).isEqualTo(expectedLevel);
    }

    private AccountOperationalSnapshot snapshot(
            String growth,
            String revenue,
            int engagement,
            long delayed,
            long open,
            long critical,
            Optional<Instant> lastInteractionAt
    ) {
        return new AccountOperationalSnapshot(
                UUID.randomUUID(),
                "Account",
                new BigDecimal(revenue),
                BigDecimal.ZERO,
                new BigDecimal(growth),
                engagement,
                delayed,
                open,
                critical,
                lastInteractionAt
        );
    }

    private Optional<Instant> interaction(long inactiveDays) {
        return Optional.of(REFERENCE_TIME.minus(inactiveDays, ChronoUnit.DAYS));
    }

    private int factorTotal(AccountPotentialAssessment result) {
        return result.revenueGrowthPotential()
                + result.engagementPotential()
                + result.revenueStrengthPotential()
                + result.interactionPotential()
                + result.operationalStabilityPotential();
    }
}
