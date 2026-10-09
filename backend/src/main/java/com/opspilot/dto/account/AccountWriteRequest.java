package com.opspilot.dto.account;

import com.opspilot.model.enums.AccountStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountWriteRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 100) String industry,
        @Size(max = 100) String region,
        @NotNull AccountStatus status,
        @NotNull @DecimalMin("0.00") BigDecimal monthlyRevenue,
        @NotNull @DecimalMin("0.00") BigDecimal previousMonthRevenue,
        @NotNull @Min(0) @Max(100) Integer engagementScore
) {
}
