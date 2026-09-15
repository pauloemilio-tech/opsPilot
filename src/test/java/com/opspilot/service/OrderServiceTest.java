package com.opspilot.service;

import com.opspilot.model.Order;
import com.opspilot.model.Account;
import com.opspilot.model.enums.OrderStatus;
import com.opspilot.repository.OrderRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OrderServiceTest {

    private static final Instant REFERENCE_TIME = Instant.parse("2026-09-12T12:00:00Z");

    private final OrderService service = new OrderService(mock(OrderRepository.class));

    @Test
    void shouldIdentifyDelayedOrder() {
        Order order = order(OrderStatus.PROCESSING, Instant.parse("2026-09-11T12:00:00Z"));

        assertThat(service.isDelayed(order, REFERENCE_TIME)).isTrue();
    }

    @Test
    void shouldNotConsiderOrderWithinDeadlineDelayed() {
        Order order = order(OrderStatus.PROCESSING, Instant.parse("2026-09-13T12:00:00Z"));

        assertThat(service.isDelayed(order, REFERENCE_TIME)).isFalse();
    }

    @Test
    void shouldNotConsiderDeliveredOrderDelayed() {
        Order order = order(OrderStatus.DELIVERED, Instant.parse("2026-09-11T12:00:00Z"));

        assertThat(service.isDelayed(order, REFERENCE_TIME)).isFalse();
    }

    @Test
    void shouldNotConsiderCancelledOrderDelayed() {
        Order order = order(OrderStatus.CANCELLED, Instant.parse("2026-09-11T12:00:00Z"));

        assertThat(service.isDelayed(order, REFERENCE_TIME)).isFalse();
    }

    @Test
    void shouldNotConsiderOrderWithoutExpectedDeliveryDelayed() {
        Order order = order(OrderStatus.PROCESSING, null);

        assertThat(service.isDelayed(order, REFERENCE_TIME)).isFalse();
    }

    private Order order(OrderStatus status, Instant expectedDeliveryAt) {
        return new Order(
                mock(Account.class),
                "ORD-TEST",
                new BigDecimal("100.00"),
                status,
                Instant.parse("2026-09-01T12:00:00Z"),
                expectedDeliveryAt
        );
    }
}
