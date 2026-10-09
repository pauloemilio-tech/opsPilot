package com.opspilot.service;

import com.opspilot.dto.account.AccountWriteRequest;
import com.opspilot.dto.interaction.InteractionCreateRequest;
import com.opspilot.dto.order.OrderCreateRequest;
import com.opspilot.dto.ticket.SupportTicketCreateRequest;
import com.opspilot.model.Account;
import com.opspilot.model.enums.AccountStatus;
import com.opspilot.model.enums.InteractionType;
import com.opspilot.model.enums.OrderStatus;
import com.opspilot.model.enums.TicketPriority;
import com.opspilot.model.enums.TicketStatus;
import com.opspilot.service.analytics.AccountAnalyticsService;
import com.opspilot.service.analytics.model.AccountPriorityAssessment;
import com.opspilot.service.model.AccountOperationalSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OperationalDataAnalyticsIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private SupportTicketService supportTicketService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private AccountOperationalService operationalService;

    @Autowired
    private AccountAnalyticsService analyticsService;

    @Autowired
    private Clock clock;

    @Test
    void persistedOperationalRecordsShouldFeedSnapshotAndDeterministicAnalytics() {
        Account account = accountService.create(new AccountWriteRequest(
                "Integration Account", "Technology", "LATAM", AccountStatus.ACTIVE,
                new BigDecimal("80000.00"), new BigDecimal("100000.00"), 90
        ));
        AccountPriorityAssessment before = analyticsService.analyze(account.getId());
        Instant now = clock.instant();

        orderService.create(account.getId(), new OrderCreateRequest(
                "INTEGRATION-" + account.getId(), new BigDecimal("1500.00"), OrderStatus.PROCESSING,
                now.minusSeconds(5 * 86400L), now.minusSeconds(86400L), null
        ));
        supportTicketService.create(account.getId(), new SupportTicketCreateRequest(
                "Production outage", TicketStatus.OPEN, TicketPriority.CRITICAL,
                now.minusSeconds(2 * 86400L), null
        ));
        interactionService.create(account.getId(), new InteractionCreateRequest(
                InteractionType.CALL, "Escalation call", now.minusSeconds(3600)
        ));

        AccountOperationalSnapshot snapshot = operationalService.getSnapshot(account.getId());
        AccountPriorityAssessment after = analyticsService.analyze(account.getId());

        assertThat(snapshot.delayedOrders()).isEqualTo(1);
        assertThat(snapshot.openTickets()).isEqualTo(1);
        assertThat(snapshot.criticalOpenTickets()).isEqualTo(1);
        assertThat(snapshot.lastInteractionAt()).contains(now.minusSeconds(3600));
        assertThat(after.risk().score()).isGreaterThan(before.risk().score());
    }
}
