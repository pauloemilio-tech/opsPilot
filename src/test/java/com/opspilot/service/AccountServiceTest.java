package com.opspilot.service;

import com.opspilot.exception.AccountNotFoundException;
import com.opspilot.model.Account;
import com.opspilot.model.enums.AccountStatus;
import com.opspilot.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Test
    void shouldFindAccountById() {
        AccountService service = new AccountService(accountRepository);
        UUID accountId = UUID.randomUUID();
        Account account = account("120.00", "100.00");
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThat(service.findById(accountId)).isSameAs(account);
    }

    @Test
    void shouldThrowWhenAccountDoesNotExist() {
        AccountService service = new AccountService(accountRepository);
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(accountId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account not found: " + accountId);
    }

    @Test
    void shouldCalculatePositiveRevenueChange() {
        AccountService service = new AccountService(accountRepository);

        assertThat(service.calculateRevenueChangePercentage(account("120.00", "100.00")))
                .isEqualByComparingTo("20.00");
    }

    @Test
    void shouldCalculateNegativeRevenueChange() {
        AccountService service = new AccountService(accountRepository);

        assertThat(service.calculateRevenueChangePercentage(account("80.00", "100.00")))
                .isEqualByComparingTo("-20.00");
    }

    @Test
    void shouldCalculateUnchangedRevenue() {
        AccountService service = new AccountService(accountRepository);

        assertThat(service.calculateRevenueChangePercentage(account("100.00", "100.00")))
                .isEqualByComparingTo("0.00");
    }

    @Test
    void shouldReturnOneHundredPercentWhenRevenueStartsFromZero() {
        AccountService service = new AccountService(accountRepository);

        assertThat(service.calculateRevenueChangePercentage(account("100.00", "0.00")))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void shouldReturnZeroWhenBothRevenueValuesAreZero() {
        AccountService service = new AccountService(accountRepository);

        assertThat(service.calculateRevenueChangePercentage(account("0.00", "0.00")))
                .isEqualByComparingTo("0.00");
    }

    private Account account(String currentRevenue, String previousRevenue) {
        return new Account(
                "Acme",
                "Retail",
                "LATAM",
                AccountStatus.ACTIVE,
                new BigDecimal(currentRevenue),
                new BigDecimal(previousRevenue),
                80
        );
    }
}
