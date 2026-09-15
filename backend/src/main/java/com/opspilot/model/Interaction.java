package com.opspilot.model;

import com.opspilot.model.enums.InteractionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "interactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interaction {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_interactions_account"))
    private Account account;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InteractionType type;

    @NotBlank
    @Column(nullable = false, columnDefinition = "text")
    private String summary;

    @NotNull
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Interaction(Account account, InteractionType type, String summary, Instant occurredAt) {
        this.account = account;
        this.type = type;
        this.summary = summary;
        this.occurredAt = occurredAt;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        Interaction interaction = (Interaction) other;
        return id != null && Objects.equals(id, interaction.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
