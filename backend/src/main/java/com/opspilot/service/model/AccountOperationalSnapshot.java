package com.opspilot.service.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record AccountOperationalSnapshot(
        UUID accountId,
        String accountName,
        BigDecimal monthlyRevenue,
        BigDecimal previousMonthRevenue,
        BigDecimal revenueChangePercentage,
        Integer engagementScore,
        long delayedOrders,
        long openTickets,
        long criticalOpenTickets,
        Optional<Instant> lastInteractionAt
) {
}
