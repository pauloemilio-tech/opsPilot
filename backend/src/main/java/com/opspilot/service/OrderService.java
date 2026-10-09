package com.opspilot.service;

import com.opspilot.dto.order.OrderCreateRequest;
import com.opspilot.exception.ConflictingOrderNumberException;
import com.opspilot.exception.InvalidOperationalDataException;
import com.opspilot.model.Account;
import com.opspilot.model.Order;
import com.opspilot.model.enums.OrderStatus;
import com.opspilot.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Set<OrderStatus> TERMINAL_STATUSES =
            EnumSet.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED);

    private final OrderRepository orderRepository;
    private final AccountService accountService;

    @Transactional
    public Order create(UUID accountId, OrderCreateRequest request) {
        validateDatesAndState(request);
        String orderNumber = request.orderNumber().trim();
        if (orderRepository.existsByOrderNumber(orderNumber)) {
            throw new ConflictingOrderNumberException(orderNumber);
        }

        Account account = accountService.findById(accountId);
        return orderRepository.save(new Order(
                account,
                orderNumber,
                request.amount(),
                request.status(),
                request.orderedAt(),
                request.expectedDeliveryAt(),
                request.deliveredAt()
        ));
    }

    @Transactional(readOnly = true)
    public List<Order> findAllForAccount(UUID accountId) {
        accountService.findById(accountId);
        return orderRepository.findAllByAccount_IdOrderByOrderedAtDescCreatedAtDesc(accountId);
    }

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

    private void validateDatesAndState(OrderCreateRequest request) {
        if (request.expectedDeliveryAt() != null && request.expectedDeliveryAt().isBefore(request.orderedAt())) {
            throw new InvalidOperationalDataException("expectedDeliveryAt must not be before orderedAt");
        }
        if (request.deliveredAt() != null && request.deliveredAt().isBefore(request.orderedAt())) {
            throw new InvalidOperationalDataException("deliveredAt must not be before orderedAt");
        }
        if (request.status() == OrderStatus.DELIVERED && request.deliveredAt() == null) {
            throw new InvalidOperationalDataException("deliveredAt is required when order status is DELIVERED");
        }
        if (request.status() != OrderStatus.DELIVERED && request.deliveredAt() != null) {
            throw new InvalidOperationalDataException("deliveredAt is only allowed when order status is DELIVERED");
        }
    }
}
