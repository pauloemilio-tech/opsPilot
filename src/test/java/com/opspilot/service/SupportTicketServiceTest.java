package com.opspilot.service;

import com.opspilot.model.Account;
import com.opspilot.model.SupportTicket;
import com.opspilot.model.enums.TicketPriority;
import com.opspilot.model.enums.TicketStatus;
import com.opspilot.repository.SupportTicketRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SupportTicketServiceTest {

    private final SupportTicketService service =
            new SupportTicketService(mock(SupportTicketRepository.class));

    @ParameterizedTest
    @EnumSource(value = TicketStatus.class, names = {"OPEN", "IN_PROGRESS", "WAITING_CUSTOMER"})
    void shouldConsiderActiveStatusOpen(TicketStatus status) {
        assertThat(service.isOpen(ticket(status))).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = TicketStatus.class, names = {"RESOLVED", "CLOSED"})
    void shouldNotConsiderFinalStatusOpen(TicketStatus status) {
        assertThat(service.isOpen(ticket(status))).isFalse();
    }

    private SupportTicket ticket(TicketStatus status) {
        return new SupportTicket(
                mock(Account.class),
                "Test ticket",
                status,
                TicketPriority.MEDIUM,
                Instant.parse("2026-09-01T12:00:00Z")
        );
    }
}
