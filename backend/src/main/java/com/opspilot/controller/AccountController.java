package com.opspilot.controller;

import com.opspilot.dto.account.AccountDetailsResponse;
import com.opspilot.dto.account.AccountOperationalSnapshotResponse;
import com.opspilot.dto.account.AccountSummaryResponse;
import com.opspilot.model.Account;
import com.opspilot.service.AccountOperationalService;
import com.opspilot.service.AccountService;
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
public class AccountController {

    private final AccountService accountService;
    private final AccountOperationalService accountOperationalService;

    @GetMapping
    public List<AccountSummaryResponse> findAll() {
        return accountService.findAllOrderedByName().stream()
                .map(AccountSummaryResponse::from)
                .toList();
    }

    @GetMapping("/{accountId}")
    public AccountDetailsResponse findById(@PathVariable UUID accountId) {
        Account account = accountService.findById(accountId);
        return AccountDetailsResponse.from(
                account,
                accountService.calculateRevenueChangePercentage(account)
        );
    }

    @GetMapping("/{accountId}/snapshot")
    public AccountOperationalSnapshotResponse getSnapshot(@PathVariable UUID accountId) {
        return AccountOperationalSnapshotResponse.from(accountOperationalService.getSnapshot(accountId));
    }
}
