package com.opspilot.ai.dto;

public record RecommendedAction(String action, String evidence) {
    public RecommendedAction {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Recommended action must not be blank");
        }
        if (evidence == null || evidence.isBlank()) {
            throw new IllegalArgumentException("Recommended action evidence must not be blank");
        }
    }
}
