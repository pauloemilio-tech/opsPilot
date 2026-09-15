package com.opspilot.service.analytics;

import com.opspilot.service.analytics.model.AccountPotentialAssessment;
import com.opspilot.service.analytics.model.AccountPriorityAssessment;
import com.opspilot.service.analytics.model.AccountRiskAssessment;
import com.opspilot.service.analytics.model.PriorityLevel;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountPriorityService {

    private static final int RISK_WEIGHT = 60;
    private static final int POTENTIAL_WEIGHT = 40;
    private static final int PERCENT_SCALE = 100;

    public AccountPriorityAssessment assess(
            UUID accountId,
            String accountName,
            AccountRiskAssessment risk,
            AccountPotentialAssessment potential
    ) {
        int weightedTotal = (risk.score() * RISK_WEIGHT) + (potential.score() * POTENTIAL_WEIGHT);
        int priorityScore = (weightedTotal + (PERCENT_SCALE / 2)) / PERCENT_SCALE;

        return new AccountPriorityAssessment(
                accountId,
                accountName,
                risk,
                potential,
                priorityScore,
                PriorityLevel.fromScore(priorityScore)
        );
    }
}
