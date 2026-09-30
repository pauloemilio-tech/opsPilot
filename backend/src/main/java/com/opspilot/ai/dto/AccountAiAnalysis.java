package com.opspilot.ai.dto;

import java.util.List;

public record AccountAiAnalysis(
        String summary,
        List<String> keyConcerns,
        List<RecommendedAction> recommendedActions
) {
    public AccountAiAnalysis {
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("AI analysis summary must not be blank");
        }
        if (keyConcerns == null || keyConcerns.stream().anyMatch(value -> value == null || value.isBlank())) {
            throw new IllegalArgumentException("AI analysis concerns must be a list of non-blank values");
        }
        if (recommendedActions == null || recommendedActions.stream().anyMatch(value -> value == null)) {
            throw new IllegalArgumentException("AI analysis recommendations must be a list of actions");
        }

        keyConcerns = List.copyOf(keyConcerns);
        recommendedActions = List.copyOf(recommendedActions);
    }
}
