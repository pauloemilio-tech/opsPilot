package com.opspilot.model;

import com.opspilot.model.enums.AccountStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @Size(max = 100)
    @Column(length = 100)
    private String industry;

    @Size(max = 100)
    @Column(length = 100)
    private String region;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "monthly_revenue", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyRevenue;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "previous_month_revenue", nullable = false, precision = 19, scale = 2)
    private BigDecimal previousMonthRevenue;

    @NotNull
    @Min(0)
    @Max(100)
    @Column(name = "engagement_score", nullable = false)
    private Integer engagementScore;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Account(
            String name,
            String industry,
            String region,
            AccountStatus status,
            BigDecimal monthlyRevenue,
            BigDecimal previousMonthRevenue,
            Integer engagementScore
    ) {
        this.name = name;
        this.industry = industry;
        this.region = region;
        this.status = status;
        this.monthlyRevenue = monthlyRevenue;
        this.previousMonthRevenue = previousMonthRevenue;
        this.engagementScore = engagementScore;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        Account account = (Account) other;
        return id != null && Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
