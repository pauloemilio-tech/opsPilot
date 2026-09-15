package com.opspilot.dto.analytics;

import com.opspilot.service.analytics.model.AccountRiskAssessment;
import com.opspilot.service.analytics.model.RiskLevel;

public record RiskAssessmentResponse(
        int score,
        RiskLevel level,
        int revenueRisk,
        int delayedOrdersRisk,
        int supportTicketsRisk,
        int criticalTicketsRisk,
        int engagementRisk,
        int inactivityRisk
) {
    public static RiskAssessmentResponse from(AccountRiskAssessment assessment) {
        return new RiskAssessmentResponse(
                assessment.score(),
                assessment.level(),
                assessment.revenueRisk(),
                assessment.delayedOrdersRisk(),
                assessment.supportTicketsRisk(),
                assessment.criticalTicketsRisk(),
                assessment.engagementRisk(),
                assessment.inactivityRisk()
        );
    }
}
