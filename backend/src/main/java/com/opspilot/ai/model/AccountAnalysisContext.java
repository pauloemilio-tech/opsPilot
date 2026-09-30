package com.opspilot.ai.model;

import com.opspilot.model.enums.AccountStatus;
import com.opspilot.service.analytics.model.PotentialLevel;
import com.opspilot.service.analytics.model.PriorityLevel;
import com.opspilot.service.analytics.model.RiskLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountAnalysisContext(
        UUID accountId,
        String accountName,
        AccountStatus accountStatus,
        String industry,
        String region,
        BigDecimal currentRevenue,
        BigDecimal previousRevenue,
        BigDecimal revenueChangePercentage,
        int engagementScore,
        long delayedOrders,
        long openTickets,
        long criticalOpenTickets,
        Instant latestInteractionAt,
        RiskContext risk,
        PotentialContext potential,
        PriorityContext priority
) {
    public record RiskContext(
            int score,
            RiskLevel level,
            int revenueRisk,
            int delayedOrdersRisk,
            int supportTicketsRisk,
            int criticalTicketsRisk,
            int engagementRisk,
            int inactivityRisk
    ) {
    }

    public record PotentialContext(
            int score,
            PotentialLevel level,
            int revenueGrowthPotential,
            int engagementPotential,
            int revenueStrengthPotential,
            int interactionPotential,
            int operationalStabilityPotential
    ) {
    }

    public record PriorityContext(int score, PriorityLevel level) {
    }
}
