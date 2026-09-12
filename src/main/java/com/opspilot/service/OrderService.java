package com.opspilot.service;

import com.opspilot.model.Order;
import com.opspilot.model.enums.OrderStatus;
import com.opspilot.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Set<OrderStatus> TERMINAL_STATUSES =
            EnumSet.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED);

    private final OrderRepository orderRepository;

    public boolean isDelayed(Order order, Instant referenceTime) {
        return order.getDeliveredAt() == null
                && order.getExpectedDeliveryAt() != null
                && order.getExpectedDeliveryAt().isBefore(referenceTime)
                && !TERMINAL_STATUSES.contains(order.getStatus());
    }

    @Transactional(readOnly = true)
    public long countDelayedOrders(UUID accountId, Instant referenceTime) {
        return orderRepository.countDelayedByAccountId(accountId, referenceTime, TERMINAL_STATUSES);
    }
}
