package com.opspilot.ai.exception;

public class AiProviderUnavailableException extends RuntimeException {
    public AiProviderUnavailableException() {
        super("AI Account Analyst is temporarily unavailable");
    }
}
