package com.opspilot.ai.exception;

import java.time.Duration;

public class AiRateLimitExceededException extends RuntimeException {

    private final Duration retryAfter;

    public AiRateLimitExceededException(String message, Duration retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
