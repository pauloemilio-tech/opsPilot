package com.opspilot.service.analytics;

import com.opspilot.exception.AccountNotFoundException;
import com.opspilot.model.Account;
import com.opspilot.model.enums.AccountStatus;
import com.opspilot.service.AccountOperationalService;
import com.opspilot.service.AccountService;
import com.opspilot.service.analytics.model.AccountPotentialAssessment;
import com.opspilot.service.analytics.model.AccountPriorityAssessment;
import com.opspilot.service.analytics.model.AccountRiskAssessment;
import com.opspilot.service.analytics.model.PotentialLevel;
import com.opspilot.service.analytics.model.RiskLevel;
import com.opspilot.service.model.AccountOperationalSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountAnalyticsServiceTest {

    private static final Instant REFERENCE_TIME = Instant.parse("2026-09-01T12:00:00Z");

    @Mock
    private AccountOperationalService accountOperationalService;

    @Mock
    private AccountService accountService;

    @Mock
    private AccountRiskScoringService riskScoringService;

    @Mock
    private AccountPotentialScoringService potentialScoringService;

    private AccountAnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AccountAnalyticsService(
                accountOperationalService,
                accountService,
                riskScoringService,
                potentialScoringService,
                new AccountPriorityService(),
                Clock.fixed(REFERENCE_TIME, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldOrchestrateSnapshotRiskPotentialAndPriority() {
        UUID accountId = UUID.randomUUID();
        AccountOperationalSnapshot snapshot = snapshot(accountId, "Mixed Signals");
        AccountRiskAssessment risk = risk(50);
        AccountPotentialAssessment potential = potential(80);
        when(accountOperationalService.getSnapshot(accountId)).thenReturn(snapshot);
        when(riskScoringService.assess(snapshot, REFERENCE_TIME)).thenReturn(risk);
        when(potentialScoringService.assess(snapshot, REFERENCE_TIME)).thenReturn(potential);

        AccountPriorityAssessment result = service.analyze(accountId);

        assertThat(result.accountId()).isEqualTo(accountId);
        assertThat(result.risk()).isSameAs(risk);
        assertThat(result.potential()).isSameAs(potential);
        assertThat(result.priorityScore()).isEqualTo(62);
    }

    @Test
    void shouldPropagateAccountNotFound() {
        UUID accountId = UUID.randomUUID();
        when(accountOperationalService.getSnapshot(accountId)).thenThrow(new AccountNotFoundException(accountId));

        assertThatThrownBy(() -> service.analyze(accountId))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void shouldExcludeInactiveAccountsAndUseNameAsStableTieBreaker() {
        Account beta = account(UUID.randomUUID(), "Beta", AccountStatus.ACTIVE);
        Account inactive = mock(Account.class);
        UUID inactiveId = UUID.randomUUID();
        when(inactive.getStatus()).thenReturn(AccountStatus.INACTIVE);
        Account alpha = account(UUID.randomUUID(), "Alpha", AccountStatus.ONBOARDING);
        AccountOperationalSnapshot betaSnapshot = snapshot(beta.getId(), beta.getName());
        AccountOperationalSnapshot alphaSnapshot = snapshot(alpha.getId(), alpha.getName());
        AccountRiskAssessment risk = risk(60);
        AccountPotentialAssessment potential = potential(60);
        when(accountService.findAllOrderedByName()).thenReturn(List.of(beta, inactive, alpha));
        when(accountOperationalService.getSnapshot(beta.getId())).thenReturn(betaSnapshot);
        when(accountOperationalService.getSnapshot(alpha.getId())).thenReturn(alphaSnapshot);
        when(riskScoringService.assess(betaSnapshot, REFERENCE_TIME)).thenReturn(risk);
        when(riskScoringService.assess(alphaSnapshot, REFERENCE_TIME)).thenReturn(risk);
        when(potentialScoringService.assess(betaSnapshot, REFERENCE_TIME)).thenReturn(potential);
        when(potentialScoringService.assess(alphaSnapshot, REFERENCE_TIME)).thenReturn(potential);

        List<AccountPriorityAssessment> ranking = service.rankAccounts();

        assertThat(ranking).extracting(AccountPriorityAssessment::accountName)
                .containsExactly("Alpha", "Beta");
        verify(accountOperationalService, never()).getSnapshot(inactiveId);
    }

    private Account account(UUID id, String name, AccountStatus status) {
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(id);
        when(account.getName()).thenReturn(name);
        when(account.getStatus()).thenReturn(status);
        return account;
    }

    private AccountOperationalSnapshot snapshot(UUID id, String name) {
        return new AccountOperationalSnapshot(
                id,
                name,
                new BigDecimal("50000"),
                new BigDecimal("50000"),
                BigDecimal.ZERO,
                70,
                0,
                0,
                0,
                Optional.of(REFERENCE_TIME)
        );
    }

    private AccountRiskAssessment risk(int score) {
        int revenue = Math.min(score, 30);
        int delayed = Math.min(score - revenue, 20);
        int support = Math.min(score - revenue - delayed, 15);
        int critical = Math.min(score - revenue - delayed - support, 15);
        int engagement = Math.min(score - revenue - delayed - support - critical, 10);
        int inactivity = score - revenue - delayed - support - critical - engagement;
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
        int growth = Math.min(score, 30);
        int engagement = Math.min(score - growth, 25);
        int revenue = Math.min(score - growth - engagement, 20);
        int interaction = Math.min(score - growth - engagement - revenue, 15);
        int stability = score - growth - engagement - revenue - interaction;
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
}
