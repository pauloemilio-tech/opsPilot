package com.opspilot.dto.account;

import com.opspilot.service.model.AccountOperationalSnapshot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountOperationalSnapshotResponse(
        UUID accountId,
        String accountName,
        BigDecimal monthlyRevenue,
        BigDecimal previousMonthRevenue,
        BigDecimal revenueChangePercentage,
        Integer engagementScore,
        long delayedOrders,
        long openTickets,
        long criticalOpenTickets,
        Instant lastInteractionAt
) {

    public static AccountOperationalSnapshotResponse from(AccountOperationalSnapshot snapshot) {
        return new AccountOperationalSnapshotResponse(
                snapshot.accountId(),
                snapshot.accountName(),
                snapshot.monthlyRevenue(),
                snapshot.previousMonthRevenue(),
                snapshot.revenueChangePercentage(),
                snapshot.engagementScore(),
                snapshot.delayedOrders(),
                snapshot.openTickets(),
                snapshot.criticalOpenTickets(),
                snapshot.lastInteractionAt().orElse(null)
        );
    }
}
