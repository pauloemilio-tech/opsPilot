package com.opspilot.repository;

import com.opspilot.model.Account;
import com.opspilot.model.Interaction;
import com.opspilot.model.Order;
import com.opspilot.model.SupportTicket;
import com.opspilot.model.enums.AccountStatus;
import com.opspilot.model.enums.InteractionType;
import com.opspilot.model.enums.OrderStatus;
import com.opspilot.model.enums.TicketPriority;
import com.opspilot.model.enums.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CoreDomainPersistenceTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private InteractionRepository interactionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldPersistAccount() {
        Account account = accountRepository.saveAndFlush(newAccount());

        assertThat(account.getId()).isNotNull();
        assertThat(account.getCreatedAt()).isNotNull();
        assertThat(account.getUpdatedAt()).isEqualTo(account.getCreatedAt());
        assertThat(accountRepository.findById(account.getId())).contains(account);
    }

    @Test
    void shouldPersistOrderAssociatedWithAccount() {
        Account account = accountRepository.saveAndFlush(newAccount());
        Order order = new Order(
                account,
                "ORD-2026-000123",
                new BigDecimal("1499.90"),
                OrderStatus.PROCESSING,
                Instant.parse("2026-09-12T12:00:00Z"),
                Instant.parse("2026-09-20T12:00:00Z")
        );

        Order persistedOrder = orderRepository.saveAndFlush(order);

        assertThat(persistedOrder.getId()).isNotNull();
        assertThat(persistedOrder.getAccount()).isEqualTo(account);
        assertThat(persistedOrder.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRejectOrderForUnknownAccount() {
        String sql = """
                INSERT INTO orders (
                    id, account_id, order_number, amount, status, ordered_at, created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        Instant now = Instant.now();

        assertThatThrownBy(() -> jdbcTemplate.update(
                sql,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "ORD-INVALID-FK",
                new BigDecimal("100.00"),
                OrderStatus.PENDING.name(),
                Timestamp.from(now),
                Timestamp.from(now)
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldQueryOperationalIndicatorsWithoutLoadingCollections() {
        Account account = accountRepository.saveAndFlush(newAccount());
        Instant referenceTime = Instant.parse("2026-09-12T12:00:00Z");

        orderRepository.save(new Order(
                account, "ORD-QUERY-1", new BigDecimal("100.00"), OrderStatus.PROCESSING,
                Instant.parse("2026-09-01T12:00:00Z"), Instant.parse("2026-09-10T12:00:00Z")
        ));
        orderRepository.save(new Order(
                account, "ORD-QUERY-2", new BigDecimal("100.00"), OrderStatus.DELIVERED,
                Instant.parse("2026-09-01T12:00:00Z"), Instant.parse("2026-09-10T12:00:00Z")
        ));

        supportTicketRepository.save(new SupportTicket(
                account, "Critical issue", TicketStatus.OPEN, TicketPriority.CRITICAL,
                Instant.parse("2026-09-08T12:00:00Z")
        ));
        supportTicketRepository.save(new SupportTicket(
                account, "Resolved issue", TicketStatus.RESOLVED, TicketPriority.CRITICAL,
                Instant.parse("2026-09-07T12:00:00Z")
        ));

        interactionRepository.save(new Interaction(
                account, InteractionType.EMAIL, "Older interaction", Instant.parse("2026-09-01T12:00:00Z")
        ));
        Interaction latest = interactionRepository.save(new Interaction(
                account, InteractionType.MEETING, "Latest interaction", Instant.parse("2026-09-11T12:00:00Z")
        ));
        orderRepository.flush();
        supportTicketRepository.flush();
        interactionRepository.flush();

        assertThat(orderRepository.countDelayedByAccountId(
                account.getId(), referenceTime, EnumSet.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED)
        )).isEqualTo(1);
        assertThat(supportTicketRepository.countByAccount_IdAndStatusIn(
                account.getId(), EnumSet.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.WAITING_CUSTOMER)
        )).isEqualTo(1);
        assertThat(supportTicketRepository.countByAccount_IdAndStatusInAndPriority(
                account.getId(),
                EnumSet.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.WAITING_CUSTOMER),
                TicketPriority.CRITICAL
        )).isEqualTo(1);
        assertThat(interactionRepository.findFirstByAccount_IdOrderByOccurredAtDescCreatedAtDesc(account.getId()))
                .contains(latest);
    }

    private Account newAccount() {
        return new Account(
                "Acme Retail",
                "Retail",
                "LATAM",
                AccountStatus.ACTIVE,
                new BigDecimal("25000.00"),
                new BigDecimal("23500.00"),
                82
        );
    }
}
