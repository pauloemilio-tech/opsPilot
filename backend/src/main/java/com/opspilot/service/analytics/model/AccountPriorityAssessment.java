package com.opspilot.service.analytics.model;

import java.util.UUID;

public record AccountPriorityAssessment(
        UUID accountId,
        String accountName,
        AccountRiskAssessment risk,
        AccountPotentialAssessment potential,
        int priorityScore,
        PriorityLevel priorityLevel
) {
    public AccountPriorityAssessment {
        if (priorityScore < 0 || priorityScore > 100) {
            throw new IllegalArgumentException("Priority score must be between 0 and 100");
        }
        if (priorityLevel != PriorityLevel.fromScore(priorityScore)) {
            throw new IllegalArgumentException("Priority level must match the score");
        }
    }
}
