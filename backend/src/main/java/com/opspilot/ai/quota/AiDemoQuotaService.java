package com.opspilot.ai.quota;

import com.opspilot.ai.config.AiDemoRateLimitProperties;
import com.opspilot.ai.exception.AiRateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiDemoQuotaService {

    private final AiDemoRateLimitProperties properties;
    private final AiUsageQuotaRepository repository;

    @Transactional
    public void reserveRequest(HttpServletRequest request) {
        if (!Boolean.TRUE.equals(properties.getEnabled())) {
            return;
        }

        String clientIp = resolveClientIp(request);
        String visitorHash = hashVisitor(clientIp);
        Instant now = Instant.now();
        LocalDate date = LocalDate.ofInstant(now, ZoneOffset.UTC);
        Duration cooldown = Duration.ofSeconds(properties.getCooldownSeconds());

        AiUsageQuota visitorQuota = repository.findForUpdateByScopeAndVisitor(AiUsageQuotaScope.VISITOR, visitorHash, date)
                .orElseGet(() -> AiUsageQuota.forVisitor(visitorHash, date));
        AiUsageQuota globalQuota = repository.findGlobalForUpdate(AiUsageQuotaScope.GLOBAL, date)
                .orElseGet(() -> AiUsageQuota.forGlobal(date));

        if (visitorQuota.hasActiveCooldown(now) || globalQuota.hasActiveCooldown(now)) {
            Duration visitorCooldown = visitorQuota.remainingCooldown(now);
            Duration globalCooldown = globalQuota.remainingCooldown(now);
            throw new AiRateLimitExceededException(
                    "AI analysis demo limit reached. Try again later.",
                    visitorCooldown.compareTo(globalCooldown) >= 0 ? visitorCooldown : globalCooldown
            );
        }

        int visitorUsage = visitorQuota.getRequestCount();
        int globalUsage = globalQuota.getRequestCount();
        int visitorLimit = properties.getMaxRequestsPerVisitorPerDay();
        int globalLimit = properties.getMaxRequestsPerDay();

        if (visitorUsage >= visitorLimit || globalUsage >= globalLimit) {
            throw new AiRateLimitExceededException(
                    "AI analysis demo limit reached. Try again later.",
                    Duration.ofSeconds(properties.getCooldownSeconds())
            );
        }

        visitorQuota.reserve(now, cooldown);
        globalQuota.reserve(now, cooldown);

        repository.saveAndFlush(visitorQuota);
        repository.saveAndFlush(globalQuota);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (Boolean.TRUE.equals(properties.getTrustForwardedHeaders()) && forwardedFor != null && !forwardedFor.isBlank()) {
            String first = forwardedFor.split(",")[0].trim();
            if (!first.isEmpty()) {
                return first;
            }
        }

        String realIp = request.getHeader("X-Real-IP");
        if (Boolean.TRUE.equals(properties.getTrustForwardedHeaders()) && realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return Optional.ofNullable(request.getRemoteAddr()).orElse("unknown");
    }

    private String hashVisitor(String clientIp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getVisitorHashSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(clientIp.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash visitor identifier", ex);
        }
    }
}
