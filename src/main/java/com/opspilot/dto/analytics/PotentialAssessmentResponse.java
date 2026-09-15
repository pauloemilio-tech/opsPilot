package com.opspilot.dto.analytics;

import com.opspilot.service.analytics.model.AccountPotentialAssessment;
import com.opspilot.service.analytics.model.PotentialLevel;

public record PotentialAssessmentResponse(
        int score,
        PotentialLevel level,
        int revenueGrowthPotential,
        int engagementPotential,
        int revenueStrengthPotential,
        int interactionPotential,
        int operationalStabilityPotential
) {
    public static PotentialAssessmentResponse from(AccountPotentialAssessment assessment) {
        return new PotentialAssessmentResponse(
                assessment.score(),
                assessment.level(),
                assessment.revenueGrowthPotential(),
                assessment.engagementPotential(),
                assessment.revenueStrengthPotential(),
                assessment.interactionPotential(),
                assessment.operationalStabilityPotential()
        );
    }
}
