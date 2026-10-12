package com.opspilot.ai.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "opspilot.ai.demo-rate-limit")
@Validated
public class AiDemoRateLimitProperties {

    @NotNull
    private Boolean enabled = false;

    @Min(1)
    private int maxRequestsPerVisitorPerDay = 2;

    @Min(1)
    private int maxRequestsPerDay = 10;

    @Min(0)
    private int cooldownSeconds = 60;

    @NotNull
    private Boolean trustForwardedHeaders = false;

    @NotBlank
    private String visitorHashSecret = "change-me-in-production";

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxRequestsPerVisitorPerDay() {
        return maxRequestsPerVisitorPerDay;
    }

    public void setMaxRequestsPerVisitorPerDay(int maxRequestsPerVisitorPerDay) {
        this.maxRequestsPerVisitorPerDay = maxRequestsPerVisitorPerDay;
    }

    public int getMaxRequestsPerDay() {
        return maxRequestsPerDay;
    }

    public void setMaxRequestsPerDay(int maxRequestsPerDay) {
        this.maxRequestsPerDay = maxRequestsPerDay;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public Boolean getTrustForwardedHeaders() {
        return trustForwardedHeaders;
    }

    public void setTrustForwardedHeaders(Boolean trustForwardedHeaders) {
        this.trustForwardedHeaders = trustForwardedHeaders;
    }

    public String getVisitorHashSecret() {
        return visitorHashSecret;
    }

    public void setVisitorHashSecret(String visitorHashSecret) {
        this.visitorHashSecret = visitorHashSecret;
    }
}
