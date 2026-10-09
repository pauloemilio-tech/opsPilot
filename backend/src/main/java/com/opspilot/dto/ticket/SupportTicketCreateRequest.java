package com.opspilot.dto.ticket;

import com.opspilot.model.enums.TicketPriority;
import com.opspilot.model.enums.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record SupportTicketCreateRequest(
        @NotBlank @Size(max = 255) String subject,
        @NotNull TicketStatus status,
        @NotNull TicketPriority priority,
        @NotNull Instant openedAt,
        Instant resolvedAt
) {
}
