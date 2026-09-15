package com.opspilot.model;

import com.opspilot.model.enums.OrderStatus;
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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
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
@Table(
        name = "orders",
        uniqueConstraints = @UniqueConstraint(name = "uk_orders_order_number", columnNames = "order_number")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_orders_account"))
    private Account account;

    @NotBlank
    @Size(max = 50)
    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @NotNull
    @Column(name = "ordered_at", nullable = false)
    private Instant orderedAt;

    @Column(name = "expected_delivery_at")
    private Instant expectedDeliveryAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Order(
            Account account,
            String orderNumber,
            BigDecimal amount,
            OrderStatus status,
            Instant orderedAt,
            Instant expectedDeliveryAt
    ) {
        this(account, orderNumber, amount, status, orderedAt, expectedDeliveryAt, null);
    }

    public Order(
            Account account,
            String orderNumber,
            BigDecimal amount,
            OrderStatus status,
            Instant orderedAt,
            Instant expectedDeliveryAt,
            Instant deliveredAt
    ) {
        this.account = account;
        this.orderNumber = orderNumber;
        this.amount = amount;
        this.status = status;
        this.orderedAt = orderedAt;
        this.expectedDeliveryAt = expectedDeliveryAt;
        this.deliveredAt = deliveredAt;
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
        Order order = (Order) other;
        return id != null && Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
