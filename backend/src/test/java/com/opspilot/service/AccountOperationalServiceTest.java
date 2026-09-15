package com.opspilot.service;

import com.opspilot.model.Account;
import com.opspilot.model.Interaction;
import com.opspilot.service.model.AccountOperationalSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountOperationalServiceTest {

    @Test
    void shouldBuildOperationalSnapshot() {
        UUID accountId = UUID.randomUUID();
        Instant referenceTime = Instant.parse("2026-09-12T12:00:00Z");
        Instant lastInteractionAt = Instant.parse("2026-09-10T15:30:00Z");
        Account account = mock(Account.class);
        Interaction interaction = mock(Interaction.class);
        AccountService accountService = mock(AccountService.class);
        OrderService orderService = mock(OrderService.class);
        SupportTicketService ticketService = mock(SupportTicketService.class);
        InteractionService interactionService = mock(InteractionService.class);
        Clock clock = mock(Clock.class);

        when(clock.instant()).thenReturn(referenceTime);
        when(accountService.findById(accountId)).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        when(account.getName()).thenReturn("Acme Retail");
        when(account.getMonthlyRevenue()).thenReturn(new BigDecimal("8000.00"));
        when(account.getPreviousMonthRevenue()).thenReturn(new BigDecimal("10000.00"));
        when(account.getEngagementScore()).thenReturn(72);
        when(accountService.calculateRevenueChangePercentage(account)).thenReturn(new BigDecimal("-20.00"));
        when(orderService.countDelayedOrders(accountId, referenceTime)).thenReturn(2L);
        when(ticketService.countOpenTickets(accountId)).thenReturn(4L);
        when(ticketService.countCriticalOpenTickets(accountId)).thenReturn(1L);
        when(interactionService.findLatestInteraction(accountId)).thenReturn(Optional.of(interaction));
        when(interaction.getOccurredAt()).thenReturn(lastInteractionAt);

        AccountOperationalService service = new AccountOperationalService(
                accountService,
                orderService,
                ticketService,
                interactionService,
                clock
        );

        AccountOperationalSnapshot snapshot = service.getSnapshot(accountId);

        assertThat(snapshot.accountId()).isEqualTo(accountId);
        assertThat(snapshot.accountName()).isEqualTo("Acme Retail");
        assertThat(snapshot.monthlyRevenue()).isEqualByComparingTo("8000.00");
        assertThat(snapshot.previousMonthRevenue()).isEqualByComparingTo("10000.00");
        assertThat(snapshot.revenueChangePercentage()).isEqualByComparingTo("-20.00");
        assertThat(snapshot.engagementScore()).isEqualTo(72);
        assertThat(snapshot.delayedOrders()).isEqualTo(2);
        assertThat(snapshot.openTickets()).isEqualTo(4);
        assertThat(snapshot.criticalOpenTickets()).isEqualTo(1);
        assertThat(snapshot.lastInteractionAt()).contains(lastInteractionAt);
        verify(orderService).countDelayedOrders(accountId, referenceTime);
    }
}
