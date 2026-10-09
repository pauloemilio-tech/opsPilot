package com.opspilot.dto.interaction;

import com.opspilot.model.enums.InteractionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record InteractionCreateRequest(
        @NotNull InteractionType type,
        @NotBlank String summary,
        @NotNull Instant occurredAt
) {
}
