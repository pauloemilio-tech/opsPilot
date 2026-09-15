package com.opspilot.controller;

import com.opspilot.exception.AccountNotFoundException;
import com.opspilot.model.Account;
import com.opspilot.model.enums.AccountStatus;
import com.opspilot.service.AccountOperationalService;
import com.opspilot.service.AccountService;
import com.opspilot.service.model.AccountOperationalSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    private static final UUID APEX_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID NORTHSTAR_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID MISSING_ID = UUID.fromString("10000000-0000-0000-0000-000000000099");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private AccountOperationalService accountOperationalService;

    @Test
    void shouldListAccountSummariesInServiceOrder() throws Exception {
        Account apex = account(APEX_ID, "Apex Commerce", "E-commerce", "Europe", "42500.00", "71000.00", 64);
        Account northstar = account(
                NORTHSTAR_ID,
                "Northstar Retail",
                "Retail",
                "North America",
                "68000.00",
                "62000.00",
                91
        );
        when(accountService.findAllOrderedByName()).thenReturn(List.of(apex, northstar));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(APEX_ID.toString()))
                .andExpect(jsonPath("$[0].name").value("Apex Commerce"))
                .andExpect(jsonPath("$[0].industry").value("E-commerce"))
                .andExpect(jsonPath("$[0].region").value("Europe"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].monthlyRevenue").value(42500.00))
                .andExpect(jsonPath("$[0].previousMonthRevenue").value(71000.00))
                .andExpect(jsonPath("$[0].engagementScore").value(64))
                .andExpect(jsonPath("$[1].name").value("Northstar Retail"))
                .andExpect(jsonPath("$[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$[0].orders").doesNotExist())
                .andExpect(jsonPath("$[0].tickets").doesNotExist())
                .andExpect(jsonPath("$[0].interactions").doesNotExist());

        verify(accountService).findAllOrderedByName();
    }

    @Test
    void shouldReturnAccountDetails() throws Exception {
        Account account = account(
                NORTHSTAR_ID,
                "Northstar Retail",
                "Retail",
                "North America",
                "68000.00",
                "62000.00",
                91
        );
        Instant createdAt = Instant.parse("2026-01-10T12:00:00Z");
        Instant updatedAt = Instant.parse("2026-08-31T18:30:00Z");
        when(account.getCreatedAt()).thenReturn(createdAt);
        when(account.getUpdatedAt()).thenReturn(updatedAt);
        when(accountService.findById(NORTHSTAR_ID)).thenReturn(account);
        when(accountService.calculateRevenueChangePercentage(account)).thenReturn(new BigDecimal("9.68"));

        mockMvc.perform(get("/api/accounts/{accountId}", NORTHSTAR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(NORTHSTAR_ID.toString()))
                .andExpect(jsonPath("$.name").value("Northstar Retail"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.monthlyRevenue").value(68000.00))
                .andExpect(jsonPath("$.previousMonthRevenue").value(62000.00))
                .andExpect(jsonPath("$.revenueChangePercentage").value(9.68))
                .andExpect(jsonPath("$.engagementScore").value(91))
                .andExpect(jsonPath("$.createdAt").value("2026-01-10T12:00:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-08-31T18:30:00Z"));

        verify(accountService).calculateRevenueChangePercentage(account);
    }

    @Test
    void shouldReturnStandardErrorWhenAccountDoesNotExist() throws Exception {
        when(accountService.findById(MISSING_ID)).thenThrow(new AccountNotFoundException(MISSING_ID));

        mockMvc.perform(get("/api/accounts/{accountId}", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Account not found: " + MISSING_ID))
                .andExpect(jsonPath("$.path").value("/api/accounts/" + MISSING_ID));
    }

    @Test
    void shouldReturnOperationalSnapshot() throws Exception {
        Instant lastInteractionAt = Instant.parse("2026-08-31T12:00:00Z");
        AccountOperationalSnapshot snapshot = new AccountOperationalSnapshot(
                NORTHSTAR_ID,
                "Northstar Retail",
                new BigDecimal("68000.00"),
                new BigDecimal("62000.00"),
                new BigDecimal("9.68"),
                91,
                2,
                3,
                1,
                Optional.of(lastInteractionAt)
        );
        when(accountOperationalService.getSnapshot(NORTHSTAR_ID)).thenReturn(snapshot);

        mockMvc.perform(get("/api/accounts/{accountId}/snapshot", NORTHSTAR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(NORTHSTAR_ID.toString()))
                .andExpect(jsonPath("$.accountName").value("Northstar Retail"))
                .andExpect(jsonPath("$.monthlyRevenue").value(68000.00))
                .andExpect(jsonPath("$.previousMonthRevenue").value(62000.00))
                .andExpect(jsonPath("$.revenueChangePercentage").value(9.68))
                .andExpect(jsonPath("$.engagementScore").value(91))
                .andExpect(jsonPath("$.delayedOrders").value(2))
                .andExpect(jsonPath("$.openTickets").value(3))
                .andExpect(jsonPath("$.criticalOpenTickets").value(1))
                .andExpect(jsonPath("$.lastInteractionAt").value("2026-08-31T12:00:00Z"));
    }

    @Test
    void shouldReturnStandardErrorWhenSnapshotAccountDoesNotExist() throws Exception {
        when(accountOperationalService.getSnapshot(MISSING_ID))
                .thenThrow(new AccountNotFoundException(MISSING_ID));

        mockMvc.perform(get("/api/accounts/{accountId}/snapshot", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Account not found: " + MISSING_ID))
                .andExpect(jsonPath("$.path").value("/api/accounts/" + MISSING_ID + "/snapshot"));
    }

    private Account account(
            UUID id,
            String name,
            String industry,
            String region,
            String monthlyRevenue,
            String previousMonthRevenue,
            int engagementScore
    ) {
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(id);
        when(account.getName()).thenReturn(name);
        when(account.getIndustry()).thenReturn(industry);
        when(account.getRegion()).thenReturn(region);
        when(account.getStatus()).thenReturn(AccountStatus.ACTIVE);
        when(account.getMonthlyRevenue()).thenReturn(new BigDecimal(monthlyRevenue));
        when(account.getPreviousMonthRevenue()).thenReturn(new BigDecimal(previousMonthRevenue));
        when(account.getEngagementScore()).thenReturn(engagementScore);
        return account;
    }
}
