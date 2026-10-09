package com.opspilot.service;

import com.opspilot.dto.ticket.SupportTicketCreateRequest;
import com.opspilot.exception.InvalidOperationalDataException;
import com.opspilot.model.Account;
import com.opspilot.model.SupportTicket;
import com.opspilot.model.enums.TicketPriority;
import com.opspilot.model.enums.TicketStatus;
import com.opspilot.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private static final Set<TicketStatus> OPEN_STATUSES = EnumSet.of(
            TicketStatus.OPEN,
            TicketStatus.IN_PROGRESS,
            TicketStatus.WAITING_CUSTOMER
    );

    private final SupportTicketRepository supportTicketRepository;
    private final AccountService accountService;

    @Transactional
    public SupportTicket create(UUID accountId, SupportTicketCreateRequest request) {
        validateDatesAndState(request);
        Account account = accountService.findById(accountId);
        return supportTicketRepository.save(new SupportTicket(
                account,
                request.subject().trim(),
                request.status(),
                request.priority(),
                request.openedAt(),
                request.resolvedAt()
        ));
    }

    @Transactional(readOnly = true)
    public List<SupportTicket> findAllForAccount(UUID accountId) {
        accountService.findById(accountId);
        return supportTicketRepository.findAllByAccount_IdOrderByOpenedAtDescCreatedAtDesc(accountId);
    }

    public boolean isOpen(SupportTicket ticket) {
        return OPEN_STATUSES.contains(ticket.getStatus());
    }

    @Transactional(readOnly = true)
    public long countOpenTickets(UUID accountId) {
        return supportTicketRepository.countByAccount_IdAndStatusIn(accountId, OPEN_STATUSES);
    }

    @Transactional(readOnly = true)
    public long countCriticalOpenTickets(UUID accountId) {
        return supportTicketRepository.countByAccount_IdAndStatusInAndPriority(
                accountId,
                OPEN_STATUSES,
                TicketPriority.CRITICAL
        );
    }

    private void validateDatesAndState(SupportTicketCreateRequest request) {
        boolean resolved = request.status() == TicketStatus.RESOLVED || request.status() == TicketStatus.CLOSED;
        if (request.resolvedAt() != null && request.resolvedAt().isBefore(request.openedAt())) {
            throw new InvalidOperationalDataException("resolvedAt must not be before openedAt");
        }
        if (resolved && request.resolvedAt() == null) {
            throw new InvalidOperationalDataException("resolvedAt is required for resolved or closed tickets");
        }
        if (!resolved && request.resolvedAt() != null) {
            throw new InvalidOperationalDataException("resolvedAt is only allowed for resolved or closed tickets");
        }
    }
}
