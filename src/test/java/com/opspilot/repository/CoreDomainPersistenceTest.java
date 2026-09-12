package com.opspilot.repository;

import com.opspilot.model.Account;
import com.opspilot.model.Order;
import com.opspilot.model.enums.AccountStatus;
import com.opspilot.model.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
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
