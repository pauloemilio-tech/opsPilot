package com.opspilot.seed;

import com.opspilot.model.Account;
import com.opspilot.model.enums.AccountStatus;
import com.opspilot.repository.AccountRepository;
import com.opspilot.repository.InteractionRepository;
import com.opspilot.repository.OrderRepository;
import com.opspilot.repository.SupportTicketRepository;
import com.opspilot.service.AccountOperationalService;
import com.opspilot.service.analytics.AccountAnalyticsService;
import com.opspilot.service.analytics.model.AccountPriorityAssessment;
import com.opspilot.service.analytics.model.PotentialLevel;
import com.opspilot.service.analytics.model.RiskLevel;
import com.opspilot.service.model.AccountOperationalSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("dev")
class DevelopmentDataSeederTest {

    private static final List<String> ACCOUNT_NAMES = List.of(
            "Northstar Retail",
            "Apex Commerce",
            "BluePeak Technologies",
            "Vertex Health",
            "Evergreen Markets",
            "Nova Distribution",
            "Horizon Supply",
            "Summit Goods",
            "Atlas Consumer Products",
            "Pulse Systems",
            "Meridian Home"
    );

    @Autowired
    private DevelopmentDataSeeder seeder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private AccountOperationalService accountOperationalService;

    @Autowired
    private AccountAnalyticsService accountAnalyticsService;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setReferenceTime() {
        when(clock.instant()).thenReturn(DevelopmentDataSeeder.REFERENCE_TIME);
    }

    @Test
    void shouldCreateExpectedDevelopmentDatasetAndAssociations() {
        List<Account> accounts = seededAccounts();

        assertThat(accounts).hasSize(11);
        assertThat(accounts).extracting(Account::getName).containsExactlyInAnyOrderElementsOf(ACCOUNT_NAMES);
        assertThat(orderRepository.countByOrderNumberStartingWith(DevelopmentDataSeeder.ORDER_PREFIX)).isEqualTo(40);
        assertThat(accounts.stream().mapToLong(account -> supportTicketRepository.countByAccount_Id(account.getId())).sum())
                .isEqualTo(25);
        assertThat(accounts.stream().mapToLong(account -> interactionRepository.countByAccount_Id(account.getId())).sum())
                .isEqualTo(30);
        assertThat(accounts).allSatisfy(account -> {
            long associations = orderRepository.countByAccount_Id(account.getId())
                    + supportTicketRepository.countByAccount_Id(account.getId())
                    + interactionRepository.countByAccount_Id(account.getId());
            assertThat(associations).isPositive();
        });
    }

    @Test
    void shouldRemainIdempotentWhenRunAgain() throws Exception {
        long accountsBefore = seededAccounts().size();
        long ordersBefore = orderRepository.countByOrderNumberStartingWith(DevelopmentDataSeeder.ORDER_PREFIX);
        long ticketsBefore = ticketCount();
        long interactionsBefore = interactionCount();

        seeder.run(new DefaultApplicationArguments());

        assertThat(seededAccounts()).hasSize((int) accountsBefore);
        assertThat(orderRepository.countByOrderNumberStartingWith(DevelopmentDataSeeder.ORDER_PREFIX))
                .isEqualTo(ordersBefore);
        assertThat(ticketCount()).isEqualTo(ticketsBefore);
        assertThat(interactionCount()).isEqualTo(interactionsBefore);
    }

    @Test
    void shouldExposeCoherentHealthyAndHighRiskSnapshots() {
        AccountOperationalSnapshot healthy = snapshot("Northstar Retail");
        AccountOperationalSnapshot highRisk = snapshot("Horizon Supply");

        assertThat(healthy.revenueChangePercentage()).isPositive();
        assertThat(healthy.engagementScore()).isGreaterThanOrEqualTo(90);
        assertThat(healthy.delayedOrders()).isZero();
        assertThat(healthy.openTickets()).isZero();
        assertThat(healthy.criticalOpenTickets()).isZero();
        assertThat(healthy.lastInteractionAt()).contains(Instant.parse("2026-08-31T12:00:00Z"));

        assertThat(highRisk.revenueChangePercentage()).isNegative();
        assertThat(highRisk.delayedOrders()).isEqualTo(4);
        assertThat(highRisk.openTickets()).isEqualTo(4);
        assertThat(highRisk.criticalOpenTickets()).isEqualTo(1);
        assertThat(highRisk.lastInteractionAt()).contains(Instant.parse("2026-07-16T12:00:00Z"));
    }

    @Test
    void shouldExerciseOnboardingRevenueFromZeroRule() {
        Account onboarding = account("Summit Goods");
        AccountOperationalSnapshot snapshot = accountOperationalService.getSnapshot(onboarding.getId());

        assertThat(onboarding.getStatus()).isEqualTo(AccountStatus.ONBOARDING);
        assertThat(snapshot.previousMonthRevenue()).isZero();
        assertThat(snapshot.revenueChangePercentage()).isEqualByComparingTo("100.00");
        assertThat(orderRepository.countByAccount_Id(onboarding.getId())).isEqualTo(2);
        assertThat(supportTicketRepository.countByAccount_Id(onboarding.getId())).isEqualTo(1);
        assertThat(interactionRepository.countByAccount_Id(onboarding.getId())).isEqualTo(3);
    }

    @Test
    void shouldProduceExpectedAnalyticsScenariosAndActiveRanking() {
        List<AccountPriorityAssessment> ranking = accountAnalyticsService.rankAccounts();
        AccountPriorityAssessment healthy = analytics("Northstar Retail");
        AccountPriorityAssessment highRisk = analytics("Horizon Supply");
        AccountPriorityAssessment highPotential = analytics("Nova Distribution");
        AccountPriorityAssessment mixedSignals = analytics("Pulse Systems");

        assertThat(healthy.risk().level()).isEqualTo(RiskLevel.LOW);
        assertThat(ranking.get(0).accountName()).isEqualTo("Horizon Supply");
        assertThat(highRisk.risk().level()).isEqualTo(RiskLevel.CRITICAL);
        assertThat(highPotential.potential().level()).isEqualTo(PotentialLevel.VERY_HIGH);
        assertThat(mixedSignals.risk().score()).isPositive();
        assertThat(mixedSignals.potential().level()).isEqualTo(PotentialLevel.VERY_HIGH);
        assertThat(ranking).extracting(AccountPriorityAssessment::accountName)
                .doesNotContain("Atlas Consumer Products")
                .contains("Summit Goods");
        assertThat(ranking).isSortedAccordingTo(
                java.util.Comparator.comparingInt(AccountPriorityAssessment::priorityScore)
                        .reversed()
                        .thenComparing(AccountPriorityAssessment::accountName)
        );
    }

    private List<Account> seededAccounts() {
        return ACCOUNT_NAMES.stream()
                .map(this::account)
                .toList();
    }

    private Account account(String name) {
        return accountRepository.findByName(name).orElseThrow();
    }

    private AccountOperationalSnapshot snapshot(String name) {
        return accountOperationalService.getSnapshot(account(name).getId());
    }

    private AccountPriorityAssessment analytics(String name) {
        return accountAnalyticsService.analyze(account(name).getId());
    }

    private long ticketCount() {
        return seededAccounts().stream()
                .mapToLong(account -> supportTicketRepository.countByAccount_Id(account.getId()))
                .sum();
    }

    private long interactionCount() {
        return seededAccounts().stream()
                .mapToLong(account -> interactionRepository.countByAccount_Id(account.getId()))
                .sum();
    }
}
