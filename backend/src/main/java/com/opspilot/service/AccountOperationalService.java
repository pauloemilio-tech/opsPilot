package com.opspilot.service;

import com.opspilot.model.Account;
import com.opspilot.model.Interaction;
import com.opspilot.service.model.AccountOperationalSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountOperationalService {

    private final AccountService accountService;
    private final OrderService orderService;
    private final SupportTicketService supportTicketService;
    private final InteractionService interactionService;
    private final Clock clock;

    @Transactional(readOnly = true)
    public AccountOperationalSnapshot getSnapshot(UUID accountId) {
        Account account = accountService.findById(accountId);
        Instant referenceTime = clock.instant();
        Optional<Instant> lastInteractionAt = interactionService.findLatestInteraction(accountId)
                .map(Interaction::getOccurredAt);

        return new AccountOperationalSnapshot(
                account.getId(),
                account.getName(),
                account.getMonthlyRevenue(),
                account.getPreviousMonthRevenue(),
                accountService.calculateRevenueChangePercentage(account),
                account.getEngagementScore(),
                orderService.countDelayedOrders(accountId, referenceTime),
                supportTicketService.countOpenTickets(accountId),
                supportTicketService.countCriticalOpenTickets(accountId),
                lastInteractionAt
        );
    }
}
