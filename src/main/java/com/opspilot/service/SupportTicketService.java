package com.opspilot.service;

import com.opspilot.model.SupportTicket;
import com.opspilot.model.enums.TicketPriority;
import com.opspilot.model.enums.TicketStatus;
import com.opspilot.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
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
}
