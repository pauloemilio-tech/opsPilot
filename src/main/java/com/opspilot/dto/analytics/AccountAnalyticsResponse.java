package com.opspilot.dto.analytics;

import com.opspilot.service.analytics.model.AccountPriorityAssessment;
import com.opspilot.service.analytics.model.PriorityLevel;

import java.util.UUID;

public record AccountAnalyticsResponse(
        UUID accountId,
        String accountName,
        RiskAssessmentResponse risk,
        PotentialAssessmentResponse potential,
        int priorityScore,
        PriorityLevel priorityLevel
) {
    public static AccountAnalyticsResponse from(AccountPriorityAssessment assessment) {
        return new AccountAnalyticsResponse(
                assessment.accountId(),
                assessment.accountName(),
                RiskAssessmentResponse.from(assessment.risk()),
                PotentialAssessmentResponse.from(assessment.potential()),
                assessment.priorityScore(),
                assessment.priorityLevel()
        );
    }
}
