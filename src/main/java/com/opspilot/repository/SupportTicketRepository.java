package com.opspilot.repository;

import com.opspilot.model.SupportTicket;
import com.opspilot.model.enums.TicketPriority;
import com.opspilot.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {

    long countByAccount_IdAndStatusIn(UUID accountId, Collection<TicketStatus> statuses);

    long countByAccount_IdAndStatusInAndPriority(
            UUID accountId,
            Collection<TicketStatus> statuses,
            TicketPriority priority
    );
}
