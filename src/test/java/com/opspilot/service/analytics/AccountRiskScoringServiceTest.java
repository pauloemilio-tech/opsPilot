package com.opspilot.service.analytics;

import com.opspilot.service.analytics.model.AccountRiskAssessment;
import com.opspilot.service.analytics.model.RiskLevel;
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

class AccountRiskScoringServiceTest {

    private static final Instant REFERENCE_TIME = Instant.parse("2026-09-01T12:00:00Z");
    private final AccountRiskScoringService service = new AccountRiskScoringService();

    @Test
    void shouldAssessAccountWithoutRisk() {
        AccountRiskAssessment result = service.assess(snapshot("10", 90, 0, 0, 0, 1), REFERENCE_TIME);

        assertThat(result.score()).isZero();
        assertThat(result.level()).isEqualTo(RiskLevel.LOW);
        assertThat(factorTotal(result)).isEqualTo(result.score());
    }

    @ParameterizedTest
    @CsvSource({"-5,8", "-20,16", "-40,24", "-55,30"})
    void shouldIncreaseRevenueRiskByDeclineBand(String revenueChange, int expectedRisk) {
        AccountRiskAssessment result = service.assess(snapshot(revenueChange, 90, 0, 0, 0, 1), REFERENCE_TIME);

        assertThat(result.revenueRisk()).isEqualTo(expectedRisk);
    }

    @ParameterizedTest
    @CsvSource({"1,7", "2,14", "3,20", "100,20"})
    void shouldCapDelayedOrdersRisk(long delayedOrders, int expectedRisk) {
        AccountRiskAssessment result = service.assess(snapshot("0", 90, delayedOrders, 0, 0, 1), REFERENCE_TIME);

        assertThat(result.delayedOrdersRisk()).isEqualTo(expectedRisk);
    }

    @ParameterizedTest
    @CsvSource({"1,5", "2,10", "3,15", "100,15"})
    void shouldCapOpenTicketsRisk(long openTickets, int expectedRisk) {
        AccountRiskAssessment result = service.assess(snapshot("0", 90, 0, openTickets, 0, 1), REFERENCE_TIME);

        assertThat(result.supportTicketsRisk()).isEqualTo(expectedRisk);
    }

    @ParameterizedTest
    @CsvSource({"1,10", "2,15", "100,15"})
    void shouldGiveCriticalTicketsSignificantCappedRisk(long criticalTickets, int expectedRisk) {
        AccountRiskAssessment result = service.assess(snapshot("0", 90, 0, 0, criticalTickets, 1), REFERENCE_TIME);

        assertThat(result.criticalTicketsRisk()).isEqualTo(expectedRisk);
    }

    @ParameterizedTest
    @CsvSource({"85,0", "65,2", "45,5", "25,8", "10,10"})
    void shouldIncreaseRiskAsEngagementFalls(int engagement, int expectedRisk) {
        AccountRiskAssessment result = service.assess(snapshot("0", engagement, 0, 0, 0, 1), REFERENCE_TIME);

        assertThat(result.engagementRisk()).isEqualTo(expectedRisk);
    }

    @ParameterizedTest
    @CsvSource({"7,0", "8,3", "31,6", "61,10"})
    void shouldIncreaseRiskAsInteractionGetsOlder(long inactiveDays, int expectedRisk) {
        AccountRiskAssessment result = service.assess(
                snapshot("0", 90, 0, 0, 0, inactiveDays),
                REFERENCE_TIME
        );

        assertThat(result.inactivityRisk()).isEqualTo(expectedRisk);
    }

    @Test
    void shouldUseMaximumInactivityRiskWhenThereIsNoInteraction() {
        AccountOperationalSnapshot snapshot = snapshot("0", 90, 0, 0, 0, 1, Optional.empty());

        assertThat(service.assess(snapshot, REFERENCE_TIME).inactivityRisk()).isEqualTo(10);
    }

    @Test
    void shouldLimitTotalRiskToOneHundred() {
        AccountOperationalSnapshot snapshot = snapshot("-80", 0, 100, 100, 100, 1, Optional.empty());

        AccountRiskAssessment result = service.assess(snapshot, REFERENCE_TIME);

        assertThat(result.score()).isEqualTo(100);
        assertThat(result.level()).isEqualTo(RiskLevel.CRITICAL);
        assertThat(factorTotal(result)).isEqualTo(100);
    }

    @ParameterizedTest
    @CsvSource({"0,LOW", "24,LOW", "25,MEDIUM", "49,MEDIUM", "50,HIGH", "74,HIGH", "75,CRITICAL", "100,CRITICAL"})
    void shouldClassifyRiskLevelAtCentralizedBoundaries(int score, RiskLevel expectedLevel) {
        assertThat(RiskLevel.fromScore(score)).isEqualTo(expectedLevel);
    }

    private AccountOperationalSnapshot snapshot(
            String revenueChange,
            int engagement,
            long delayedOrders,
            long openTickets,
            long criticalTickets,
            long inactiveDays
    ) {
        return snapshot(
                revenueChange,
                engagement,
                delayedOrders,
                openTickets,
                criticalTickets,
                inactiveDays,
                Optional.of(REFERENCE_TIME.minus(inactiveDays, ChronoUnit.DAYS))
        );
    }

    private AccountOperationalSnapshot snapshot(
            String revenueChange,
            int engagement,
            long delayedOrders,
            long openTickets,
            long criticalTickets,
            long inactiveDays,
            Optional<Instant> lastInteractionAt
    ) {
        return new AccountOperationalSnapshot(
                UUID.randomUUID(),
                "Account",
                new BigDecimal("50000"),
                new BigDecimal("50000"),
                new BigDecimal(revenueChange),
                engagement,
                delayedOrders,
                openTickets,
                criticalTickets,
                lastInteractionAt
        );
    }

    private int factorTotal(AccountRiskAssessment result) {
        return result.revenueRisk()
                + result.delayedOrdersRisk()
                + result.supportTicketsRisk()
                + result.criticalTicketsRisk()
                + result.engagementRisk()
                + result.inactivityRisk();
    }
}
