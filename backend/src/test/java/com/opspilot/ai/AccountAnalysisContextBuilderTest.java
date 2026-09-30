package com.opspilot.ai;

import com.opspilot.ai.model.AccountAnalysisContext;
import com.opspilot.model.Account;
import com.opspilot.model.enums.AccountStatus;
import com.opspilot.service.AccountOperationalService;
import com.opspilot.service.AccountService;
import com.opspilot.service.analytics.AccountAnalyticsService;
import com.opspilot.service.analytics.model.AccountPotentialAssessment;
import com.opspilot.service.analytics.model.AccountPriorityAssessment;
import com.opspilot.service.analytics.model.AccountRiskAssessment;
import com.opspilot.service.analytics.model.PotentialLevel;
import com.opspilot.service.analytics.model.PriorityLevel;
import com.opspilot.service.analytics.model.RiskLevel;
import com.opspilot.service.model.AccountOperationalSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountAnalysisContextBuilderTest {

    @Mock
    private AccountService accountService;

    @Mock
    private AccountOperationalService accountOperationalService;

    @Mock
    private AccountAnalyticsService accountAnalyticsService;

    @Test
    void shouldBuildTrustedContextFromExistingServicesWithoutRecalculatingAnalytics() {
        UUID accountId = UUID.randomUUID();
        Instant latestInteraction = Instant.parse("2026-09-01T12:00:00Z");
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);
        when(account.getName()).thenReturn("Horizon Supply");
        when(account.getStatus()).thenReturn(AccountStatus.ACTIVE);
        when(account.getIndustry()).thenReturn("Distribution");
        when(account.getRegion()).thenReturn("LATAM");

        AccountOperationalSnapshot snapshot = new AccountOperationalSnapshot(
                accountId,
                "Horizon Supply",
                new BigDecimal("29000.00"),
                new BigDecimal("61000.00"),
                new BigDecimal("-52.46"),
                18,
                4,
                4,
                1,
                Optional.of(latestInteraction)
        );
        AccountRiskAssessment risk = new AccountRiskAssessment(
                95, RiskLevel.CRITICAL, 30, 20, 15, 15, 10, 5
        );
        AccountPotentialAssessment potential = new AccountPotentialAssessment(
                12, PotentialLevel.LOW, 0, 0, 12, 0, 0
        );
        AccountPriorityAssessment analytics = new AccountPriorityAssessment(
                accountId, "Horizon Supply", risk, potential, 62, PriorityLevel.HIGH
        );
        when(accountService.findById(accountId)).thenReturn(account);
        when(accountOperationalService.getSnapshot(accountId)).thenReturn(snapshot);
        when(accountAnalyticsService.analyze(snapshot)).thenReturn(analytics);

        AccountAnalysisContext context = new AccountAnalysisContextBuilder(
                accountService,
                accountOperationalService,
                accountAnalyticsService
        ).build(accountId);

        assertThat(context.accountName()).isEqualTo("Horizon Supply");
        assertThat(context.currentRevenue()).isEqualByComparingTo("29000.00");
        assertThat(context.revenueChangePercentage()).isEqualByComparingTo("-52.46");
        assertThat(context.delayedOrders()).isEqualTo(4);
        assertThat(context.latestInteractionAt()).isEqualTo(latestInteraction);
        assertThat(context.risk().score()).isEqualTo(95);
        assertThat(context.risk().revenueRisk()).isEqualTo(30);
        assertThat(context.potential().score()).isEqualTo(12);
        assertThat(context.priority().score()).isEqualTo(62);
        assertThat(context.priority().level()).isEqualTo(PriorityLevel.HIGH);
        verify(accountOperationalService).getSnapshot(accountId);
        verify(accountAnalyticsService).analyze(snapshot);
    }
}
