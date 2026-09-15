package com.opspilot.dto.analytics;

import com.opspilot.service.analytics.model.AccountPriorityAssessment;
import com.opspilot.service.analytics.model.PotentialLevel;
import com.opspilot.service.analytics.model.PriorityLevel;
import com.opspilot.service.analytics.model.RiskLevel;

import java.util.UUID;

public record AccountPriorityResponse(
        UUID accountId,
        String accountName,
        int riskScore,
        RiskLevel riskLevel,
        int potentialScore,
        PotentialLevel potentialLevel,
        int priorityScore,
        PriorityLevel priorityLevel
) {
    public static AccountPriorityResponse from(AccountPriorityAssessment assessment) {
        return new AccountPriorityResponse(
                assessment.accountId(),
                assessment.accountName(),
                assessment.risk().score(),
                assessment.risk().level(),
                assessment.potential().score(),
                assessment.potential().level(),
                assessment.priorityScore(),
                assessment.priorityLevel()
        );
    }
}
