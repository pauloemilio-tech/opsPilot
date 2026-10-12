package com.opspilot.ai.quota;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "ai_usage_quotas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"scope", "visitor_hash", "usage_date"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiUsageQuota {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiUsageQuotaScope scope;

    @Column(name = "visitor_hash", length = 128)
    private String visitorHash;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "request_count", nullable = false)
    private int requestCount;

    @Column(name = "cooldown_until")
    private Instant cooldownUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private AiUsageQuota(AiUsageQuotaScope scope, String visitorHash, LocalDate usageDate) {
        this.scope = scope;
        this.visitorHash = visitorHash;
        this.usageDate = usageDate;
        this.requestCount = 0;
    }

    public static AiUsageQuota forVisitor(String visitorHash, LocalDate usageDate) {
        return new AiUsageQuota(AiUsageQuotaScope.VISITOR, visitorHash, usageDate);
    }

    public static AiUsageQuota forGlobal(LocalDate usageDate) {
        return new AiUsageQuota(AiUsageQuotaScope.GLOBAL, null, usageDate);
    }

    public boolean hasActiveCooldown(Instant now) {
        return cooldownUntil != null && now.isBefore(cooldownUntil);
    }

    public java.time.Duration remainingCooldown(Instant now) {
        if (cooldownUntil == null || !now.isBefore(cooldownUntil)) {
            return java.time.Duration.ZERO;
        }
        return java.time.Duration.between(now, cooldownUntil);
    }

    public void reserve(Instant now, java.time.Duration cooldown) {
        this.requestCount++;
        if (cooldown != null && !cooldown.isZero() && !cooldown.isNegative()) {
            this.cooldownUntil = now.plus(cooldown);
        } else {
            this.cooldownUntil = null;
        }
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
