package com.opspilot.dto.order;

import com.opspilot.model.Order;
import com.opspilot.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID accountId,
        String orderNumber,
        BigDecimal amount,
        OrderStatus status,
        Instant orderedAt,
        Instant expectedDeliveryAt,
        Instant deliveredAt,
        Instant createdAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(), order.getAccount().getId(), order.getOrderNumber(), order.getAmount(),
                order.getStatus(), order.getOrderedAt(), order.getExpectedDeliveryAt(),
                order.getDeliveredAt(), order.getCreatedAt()
        );
    }
}
