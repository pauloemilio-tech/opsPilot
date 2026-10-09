package com.opspilot.dto.interaction;

import com.opspilot.model.Interaction;
import com.opspilot.model.enums.InteractionType;

import java.time.Instant;
import java.util.UUID;

public record InteractionResponse(
        UUID id,
        UUID accountId,
        InteractionType type,
        String summary,
        Instant occurredAt,
        Instant createdAt
) {
    public static InteractionResponse from(Interaction interaction) {
        return new InteractionResponse(
                interaction.getId(), interaction.getAccount().getId(), interaction.getType(),
                interaction.getSummary(), interaction.getOccurredAt(), interaction.getCreatedAt()
        );
    }
}
