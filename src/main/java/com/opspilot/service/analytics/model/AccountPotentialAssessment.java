package com.opspilot.service.analytics.model;

public record AccountPotentialAssessment(
        int score,
        PotentialLevel level,
        int revenueGrowthPotential,
        int engagementPotential,
        int revenueStrengthPotential,
        int interactionPotential,
        int operationalStabilityPotential
) {
    public AccountPotentialAssessment {
        int factorTotal = revenueGrowthPotential
                + engagementPotential
                + revenueStrengthPotential
                + interactionPotential
                + operationalStabilityPotential;
        if (score != factorTotal || score < 0 || score > 100) {
            throw new IllegalArgumentException("Potential score must equal its factors and be between 0 and 100");
        }
        if (level != PotentialLevel.fromScore(score)) {
            throw new IllegalArgumentException("Potential level must match the score");
        }
    }
}
