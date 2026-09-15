package com.opspilot.service.analytics.model;

public enum PriorityLevel {
    LOW,
    MEDIUM,
    HIGH,
    URGENT;

    public static PriorityLevel fromScore(int score) {
        if (score < 25) {
            return LOW;
        }
        if (score < 50) {
            return MEDIUM;
        }
        if (score < 75) {
            return HIGH;
        }
        return URGENT;
    }
}
