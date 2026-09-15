package com.opspilot.dto.account;

import com.opspilot.model.Account;
import com.opspilot.model.enums.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountDetailsResponse(
        UUID id,
        String name,
        String industry,
        String region,
        AccountStatus status,
        BigDecimal monthlyRevenue,
        BigDecimal previousMonthRevenue,
        BigDecimal revenueChangePercentage,
        Integer engagementScore,
        Instant createdAt,
        Instant updatedAt
) {

    public static AccountDetailsResponse from(Account account, BigDecimal revenueChangePercentage) {
        return new AccountDetailsResponse(
                account.getId(),
                account.getName(),
                account.getIndustry(),
                account.getRegion(),
                account.getStatus(),
                account.getMonthlyRevenue(),
                account.getPreviousMonthRevenue(),
                revenueChangePercentage,
                account.getEngagementScore(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
