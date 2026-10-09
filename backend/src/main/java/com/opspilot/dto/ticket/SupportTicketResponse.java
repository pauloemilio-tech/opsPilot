package com.opspilot.dto.ticket;

import com.opspilot.model.SupportTicket;
import com.opspilot.model.enums.TicketPriority;
import com.opspilot.model.enums.TicketStatus;

import java.time.Instant;
import java.util.UUID;

public record SupportTicketResponse(
        UUID id,
        UUID accountId,
        String subject,
        TicketStatus status,
        TicketPriority priority,
        Instant openedAt,
        Instant resolvedAt,
        Instant createdAt
) {
    public static SupportTicketResponse from(SupportTicket ticket) {
        return new SupportTicketResponse(
                ticket.getId(), ticket.getAccount().getId(), ticket.getSubject(), ticket.getStatus(),
                ticket.getPriority(), ticket.getOpenedAt(), ticket.getResolvedAt(), ticket.getCreatedAt()
        );
    }
}
