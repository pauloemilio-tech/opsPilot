package com.opspilot.model;

import com.opspilot.model.enums.TicketPriority;
import com.opspilot.model.enums.TicketStatus;
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
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "support_tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SupportTicket {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_support_tickets_account"))
    private Account account;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String subject;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    @NotNull
    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public SupportTicket(
            Account account,
            String subject,
            TicketStatus status,
            TicketPriority priority,
            Instant openedAt
    ) {
        this(account, subject, status, priority, openedAt, null);
    }

    public SupportTicket(
            Account account,
            String subject,
            TicketStatus status,
            TicketPriority priority,
            Instant openedAt,
            Instant resolvedAt
    ) {
        this.account = account;
        this.subject = subject;
        this.status = status;
        this.priority = priority;
        this.openedAt = openedAt;
        this.resolvedAt = resolvedAt;
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
        SupportTicket ticket = (SupportTicket) other;
        return id != null && Objects.equals(id, ticket.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
