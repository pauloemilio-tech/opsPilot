package com.opspilot.dto.order;

import com.opspilot.model.enums.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreateRequest(
        @NotBlank @Size(max = 50) String orderNumber,
        @NotNull @DecimalMin("0.00") BigDecimal amount,
        @NotNull OrderStatus status,
        @NotNull Instant orderedAt,
        Instant expectedDeliveryAt,
        Instant deliveredAt
) {
}
