package com.opspilot.service.analytics.model;

public enum PotentialLevel {
    LOW,
    MEDIUM,
    HIGH,
    VERY_HIGH;

    public static PotentialLevel fromScore(int score) {
        if (score < 25) {
            return LOW;
        }
        if (score < 50) {
            return MEDIUM;
        }
        if (score < 75) {
            return HIGH;
        }
        return VERY_HIGH;
    }
}
