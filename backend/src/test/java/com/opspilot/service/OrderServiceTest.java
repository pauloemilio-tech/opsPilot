package com.opspilot.service;

import com.opspilot.dto.order.OrderCreateRequest;
import com.opspilot.exception.ConflictingOrderNumberException;
import com.opspilot.exception.InvalidOperationalDataException;
import com.opspilot.model.Order;
import com.opspilot.model.Account;
import com.opspilot.model.enums.OrderStatus;
import com.opspilot.repository.OrderRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private static final Instant REFERENCE_TIME = Instant.parse("2026-09-12T12:00:00Z");

    private final OrderService service = new OrderService(mock(OrderRepository.class), mock(AccountService.class));

    @Test
    void shouldCreateOrderAssociatedWithAccount() {
        OrderRepository repository = mock(OrderRepository.class);
        AccountService accountService = mock(AccountService.class);
        OrderService service = new OrderService(repository, accountService);
        UUID accountId = UUID.randomUUID();
        Account account = mock(Account.class);
        OrderCreateRequest request = new OrderCreateRequest(
                " ORD-100 ", new BigDecimal("250.00"), OrderStatus.PROCESSING,
                Instant.parse("2026-09-01T12:00:00Z"), Instant.parse("2026-09-05T12:00:00Z"), null
        );
        when(accountService.findById(accountId)).thenReturn(account);
        when(repository.save(org.mockito.ArgumentMatchers.any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Order created = service.create(accountId, request);

        assertThat(created.getAccount()).isSameAs(account);
        assertThat(created.getOrderNumber()).isEqualTo("ORD-100");
        verify(repository).save(created);
    }

    @Test
    void shouldRejectDuplicateOrderNumber() {
        OrderRepository repository = mock(OrderRepository.class);
        OrderService service = new OrderService(repository, mock(AccountService.class));
        OrderCreateRequest request = new OrderCreateRequest(
                "ORD-100", new BigDecimal("250.00"), OrderStatus.PROCESSING,
                Instant.parse("2026-09-01T12:00:00Z"), null, null
        );
        when(repository.existsByOrderNumber("ORD-100")).thenReturn(true);

        assertThatThrownBy(() -> service.create(UUID.randomUUID(), request))
                .isInstanceOf(ConflictingOrderNumberException.class);
    }

    @Test
    void shouldRejectDeliveryBeforeOrderDate() {
        OrderCreateRequest request = new OrderCreateRequest(
                "ORD-100", new BigDecimal("250.00"), OrderStatus.PROCESSING,
                Instant.parse("2026-09-05T12:00:00Z"), Instant.parse("2026-09-01T12:00:00Z"), null
        );

        assertThatThrownBy(() -> service.create(UUID.randomUUID(), request))
                .isInstanceOf(InvalidOperationalDataException.class)
                .hasMessage("expectedDeliveryAt must not be before orderedAt");
    }

    @Test
    void shouldListOrdersAfterConfirmingAccountExists() {
        OrderRepository repository = mock(OrderRepository.class);
        AccountService accountService = mock(AccountService.class);
        OrderService service = new OrderService(repository, accountService);
        UUID accountId = UUID.randomUUID();
        List<Order> orders = List.of(mock(Order.class));
        when(repository.findAllByAccount_IdOrderByOrderedAtDescCreatedAtDesc(accountId)).thenReturn(orders);

        assertThat(service.findAllForAccount(accountId)).isSameAs(orders);
        verify(accountService).findById(accountId);
    }

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
