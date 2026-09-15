package com.opspilot.service.analytics;

import com.opspilot.model.Account;
import com.opspilot.model.enums.AccountStatus;
import com.opspilot.service.AccountOperationalService;
import com.opspilot.service.AccountService;
import com.opspilot.service.analytics.model.AccountPotentialAssessment;
import com.opspilot.service.analytics.model.AccountPriorityAssessment;
import com.opspilot.service.analytics.model.AccountRiskAssessment;
import com.opspilot.service.model.AccountOperationalSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountAnalyticsService {

    private static final Comparator<AccountPriorityAssessment> PRIORITY_ORDER =
            Comparator.comparingInt(AccountPriorityAssessment::priorityScore)
                    .reversed()
                    .thenComparing(AccountPriorityAssessment::accountName);

    private final AccountOperationalService accountOperationalService;
    private final AccountService accountService;
    private final AccountRiskScoringService riskScoringService;
    private final AccountPotentialScoringService potentialScoringService;
    private final AccountPriorityService priorityService;
    private final Clock clock;

    public AccountPriorityAssessment analyze(UUID accountId) {
        AccountOperationalSnapshot snapshot = accountOperationalService.getSnapshot(accountId);
        Instant referenceTime = clock.instant();
        AccountRiskAssessment risk = riskScoringService.assess(snapshot, referenceTime);
        AccountPotentialAssessment potential = potentialScoringService.assess(snapshot, referenceTime);

        return priorityService.assess(snapshot.accountId(), snapshot.accountName(), risk, potential);
    }

    public List<AccountPriorityAssessment> rankAccounts() {
        return accountService.findAllOrderedByName().stream()
                .filter(this::isEligibleForActiveRanking)
                .map(Account::getId)
                .map(this::analyze)
                .sorted(PRIORITY_ORDER)
                .toList();
    }

    private boolean isEligibleForActiveRanking(Account account) {
        return account.getStatus() != AccountStatus.INACTIVE;
    }
}
