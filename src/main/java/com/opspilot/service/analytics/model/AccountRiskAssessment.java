package com.opspilot.service.analytics.model;

public record AccountRiskAssessment(
        int score,
        RiskLevel level,
        int revenueRisk,
        int delayedOrdersRisk,
        int supportTicketsRisk,
        int criticalTicketsRisk,
        int engagementRisk,
        int inactivityRisk
) {
    public AccountRiskAssessment {
        int factorTotal = revenueRisk
                + delayedOrdersRisk
                + supportTicketsRisk
                + criticalTicketsRisk
                + engagementRisk
                + inactivityRisk;
        if (score != factorTotal || score < 0 || score > 100) {
            throw new IllegalArgumentException("Risk score must equal its factors and be between 0 and 100");
        }
        if (level != RiskLevel.fromScore(score)) {
            throw new IllegalArgumentException("Risk level must match the score");
        }
    }
}
