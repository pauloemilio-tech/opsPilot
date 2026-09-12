package com.opspilot.service;

import com.opspilot.exception.AccountNotFoundException;
import com.opspilot.model.Account;
import com.opspilot.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final int PERCENTAGE_SCALE = 2;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public Account findById(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    public BigDecimal calculateRevenueChangePercentage(Account account) {
        BigDecimal currentRevenue = account.getMonthlyRevenue();
        BigDecimal previousRevenue = account.getPreviousMonthRevenue();

        if (previousRevenue.signum() == 0) {
            return currentRevenue.signum() == 0
                    ? BigDecimal.ZERO.setScale(PERCENTAGE_SCALE)
                    : ONE_HUNDRED.setScale(PERCENTAGE_SCALE);
        }

        return currentRevenue.subtract(previousRevenue)
                .multiply(ONE_HUNDRED)
                .divide(previousRevenue, PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }
}
