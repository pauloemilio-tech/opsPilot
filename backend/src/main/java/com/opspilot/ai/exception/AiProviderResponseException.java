package com.opspilot.ai.exception;

public class AiProviderResponseException extends RuntimeException {
    public AiProviderResponseException() {
        super("AI Account Analyst returned an invalid response");
    }
}
