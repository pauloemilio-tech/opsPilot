package com.opspilot.service;

import com.opspilot.dto.ticket.SupportTicketCreateRequest;
import com.opspilot.exception.InvalidOperationalDataException;
import com.opspilot.model.Account;
import com.opspilot.model.SupportTicket;
import com.opspilot.model.enums.TicketPriority;
import com.opspilot.model.enums.TicketStatus;
import com.opspilot.repository.SupportTicketRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SupportTicketServiceTest {

    private final SupportTicketService service =
            new SupportTicketService(mock(SupportTicketRepository.class), mock(AccountService.class));

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

    @Test
    void shouldCreateTicketAssociatedWithAccount() {
        SupportTicketRepository repository = mock(SupportTicketRepository.class);
        AccountService accountService = mock(AccountService.class);
        SupportTicketService service = new SupportTicketService(repository, accountService);
        UUID accountId = UUID.randomUUID();
        Account account = mock(Account.class);
        SupportTicketCreateRequest request = new SupportTicketCreateRequest(
                "Payment issue", TicketStatus.OPEN, TicketPriority.HIGH,
                Instant.parse("2026-09-01T12:00:00Z"), null
        );
        when(accountService.findById(accountId)).thenReturn(account);
        when(repository.save(org.mockito.ArgumentMatchers.any(SupportTicket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.create(accountId, request).getAccount()).isSameAs(account);
    }

    @Test
    void shouldRequireResolutionDateForClosedTicket() {
        SupportTicketCreateRequest request = new SupportTicketCreateRequest(
                "Payment issue", TicketStatus.CLOSED, TicketPriority.HIGH,
                Instant.parse("2026-09-01T12:00:00Z"), null
        );

        assertThatThrownBy(() -> service.create(UUID.randomUUID(), request))
                .isInstanceOf(InvalidOperationalDataException.class)
                .hasMessage("resolvedAt is required for resolved or closed tickets");
    }

    @Test
    void shouldListTicketsAfterConfirmingAccountExists() {
        SupportTicketRepository repository = mock(SupportTicketRepository.class);
        AccountService accountService = mock(AccountService.class);
        SupportTicketService service = new SupportTicketService(repository, accountService);
        UUID accountId = UUID.randomUUID();
        List<SupportTicket> tickets = List.of(mock(SupportTicket.class));
        when(repository.findAllByAccount_IdOrderByOpenedAtDescCreatedAtDesc(accountId)).thenReturn(tickets);

        assertThat(service.findAllForAccount(accountId)).isSameAs(tickets);
        org.mockito.Mockito.verify(accountService).findById(accountId);
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
