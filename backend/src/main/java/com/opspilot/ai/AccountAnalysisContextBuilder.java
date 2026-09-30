package com.opspilot.ai;

import com.opspilot.ai.model.AccountAnalysisContext;
import com.opspilot.ai.model.AccountAnalysisContext.PotentialContext;
import com.opspilot.ai.model.AccountAnalysisContext.PriorityContext;
import com.opspilot.ai.model.AccountAnalysisContext.RiskContext;
import com.opspilot.model.Account;
import com.opspilot.service.AccountOperationalService;
import com.opspilot.service.AccountService;
import com.opspilot.service.analytics.AccountAnalyticsService;
import com.opspilot.service.analytics.model.AccountPotentialAssessment;
import com.opspilot.service.analytics.model.AccountPriorityAssessment;
import com.opspilot.service.analytics.model.AccountRiskAssessment;
import com.opspilot.service.model.AccountOperationalSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountAnalysisContextBuilder {

    private final AccountService accountService;
    private final AccountOperationalService accountOperationalService;
    private final AccountAnalyticsService accountAnalyticsService;

    public AccountAnalysisContext build(UUID accountId) {
        Account account = accountService.findById(accountId);
        AccountOperationalSnapshot snapshot = accountOperationalService.getSnapshot(accountId);
        AccountPriorityAssessment analytics = accountAnalyticsService.analyze(snapshot);

        return new AccountAnalysisContext(
                account.getId(),
                account.getName(),
                account.getStatus(),
                account.getIndustry(),
                account.getRegion(),
                snapshot.monthlyRevenue(),
                snapshot.previousMonthRevenue(),
                snapshot.revenueChangePercentage(),
                snapshot.engagementScore(),
                snapshot.delayedOrders(),
                snapshot.openTickets(),
                snapshot.criticalOpenTickets(),
                snapshot.lastInteractionAt().orElse(null),
                riskContext(analytics.risk()),
                potentialContext(analytics.potential()),
                new PriorityContext(analytics.priorityScore(), analytics.priorityLevel())
        );
    }

    private RiskContext riskContext(AccountRiskAssessment risk) {
        return new RiskContext(
                risk.score(),
                risk.level(),
                risk.revenueRisk(),
                risk.delayedOrdersRisk(),
                risk.supportTicketsRisk(),
                risk.criticalTicketsRisk(),
                risk.engagementRisk(),
                risk.inactivityRisk()
        );
    }

    private PotentialContext potentialContext(AccountPotentialAssessment potential) {
        return new PotentialContext(
                potential.score(),
                potential.level(),
                potential.revenueGrowthPotential(),
                potential.engagementPotential(),
                potential.revenueStrengthPotential(),
                potential.interactionPotential(),
                potential.operationalStabilityPotential()
        );
    }
}
