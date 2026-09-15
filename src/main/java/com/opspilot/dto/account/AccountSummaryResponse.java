package com.opspilot.dto.account;

import com.opspilot.model.Account;
import com.opspilot.model.enums.AccountStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountSummaryResponse(
        UUID id,
        String name,
        String industry,
        String region,
        AccountStatus status,
        BigDecimal monthlyRevenue,
        BigDecimal previousMonthRevenue,
        Integer engagementScore
) {

    public static AccountSummaryResponse from(Account account) {
        return new AccountSummaryResponse(
                account.getId(),
                account.getName(),
                account.getIndustry(),
                account.getRegion(),
                account.getStatus(),
                account.getMonthlyRevenue(),
                account.getPreviousMonthRevenue(),
                account.getEngagementScore()
        );
    }
}
