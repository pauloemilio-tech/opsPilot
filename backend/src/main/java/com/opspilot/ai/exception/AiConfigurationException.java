package com.opspilot.ai.exception;

public class AiConfigurationException extends RuntimeException {
    public AiConfigurationException() {
        super("AI Account Analyst is not configured");
    }
}
