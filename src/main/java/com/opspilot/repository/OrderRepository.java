package com.opspilot.repository;

import com.opspilot.model.Order;
import com.opspilot.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("""
            SELECT COUNT(o)
            FROM Order o
            WHERE o.account.id = :accountId
              AND o.deliveredAt IS NULL
              AND o.expectedDeliveryAt IS NOT NULL
              AND o.expectedDeliveryAt < :referenceTime
              AND o.status NOT IN :excludedStatuses
            """)
    long countDelayedByAccountId(
            @Param("accountId") UUID accountId,
            @Param("referenceTime") Instant referenceTime,
            @Param("excludedStatuses") Collection<OrderStatus> excludedStatuses
    );
}
