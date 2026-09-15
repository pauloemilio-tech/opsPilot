package com.opspilot.controller;

import com.opspilot.dto.analytics.AccountAnalyticsResponse;
import com.opspilot.dto.analytics.AccountPriorityResponse;
import com.opspilot.service.analytics.AccountAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountAnalyticsController {

    private final AccountAnalyticsService accountAnalyticsService;

    @GetMapping("/{accountId}/analytics")
    public AccountAnalyticsResponse analyze(@PathVariable UUID accountId) {
        return AccountAnalyticsResponse.from(accountAnalyticsService.analyze(accountId));
    }

    @GetMapping("/priorities")
    public List<AccountPriorityResponse> rankAccounts() {
        return accountAnalyticsService.rankAccounts().stream()
                .map(AccountPriorityResponse::from)
                .toList();
    }
}
