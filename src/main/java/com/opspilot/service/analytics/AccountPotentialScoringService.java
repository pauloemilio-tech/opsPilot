package com.opspilot.service.analytics;

import com.opspilot.service.analytics.model.AccountPotentialAssessment;
import com.opspilot.service.analytics.model.PotentialLevel;
import com.opspilot.service.model.AccountOperationalSnapshot;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AccountPotentialScoringService {

    private static final BigDecimal TEN_THOUSAND = new BigDecimal("10000");
    private static final BigDecimal TWENTY_FIVE_THOUSAND = new BigDecimal("25000");
    private static final BigDecimal FIFTY_THOUSAND = new BigDecimal("50000");
    private static final BigDecimal ONE_HUNDRED_THOUSAND = new BigDecimal("100000");

    public AccountPotentialAssessment assess(AccountOperationalSnapshot snapshot, Instant referenceTime) {
        int revenueGrowthPotential = revenueGrowthPotential(snapshot.revenueChangePercentage());
        int engagementPotential = engagementPotential(snapshot.engagementScore());
        int revenueStrengthPotential = revenueStrengthPotential(snapshot.monthlyRevenue());
        int interactionPotential = interactionPotential(snapshot, referenceTime);
        int operationalStabilityPotential = operationalStabilityPotential(snapshot);
        int score = revenueGrowthPotential
                + engagementPotential
                + revenueStrengthPotential
                + interactionPotential
                + operationalStabilityPotential;

        return new AccountPotentialAssessment(
                score,
                PotentialLevel.fromScore(score),
                revenueGrowthPotential,
                engagementPotential,
                revenueStrengthPotential,
                interactionPotential,
                operationalStabilityPotential
        );
    }

    private int revenueGrowthPotential(BigDecimal revenueChangePercentage) {
        if (revenueChangePercentage.signum() <= 0) {
            return 0;
        }
        if (revenueChangePercentage.compareTo(new BigDecimal("5")) <= 0) {
            return 8;
        }
        if (revenueChangePercentage.compareTo(new BigDecimal("15")) <= 0) {
            return 16;
        }
        if (revenueChangePercentage.compareTo(new BigDecimal("30")) <= 0) {
            return 24;
        }
        return 30;
    }

    private int engagementPotential(int engagementScore) {
        if (engagementScore >= 80) {
            return 25;
        }
        if (engagementScore >= 60) {
            return 19;
        }
        if (engagementScore >= 40) {
            return 12;
        }
        if (engagementScore >= 20) {
            return 6;
        }
        return 0;
    }

    private int revenueStrengthPotential(BigDecimal monthlyRevenue) {
        if (monthlyRevenue.signum() <= 0) {
            return 0;
        }
        if (monthlyRevenue.compareTo(TEN_THOUSAND) < 0) {
            return 4;
        }
        if (monthlyRevenue.compareTo(TWENTY_FIVE_THOUSAND) < 0) {
            return 8;
        }
        if (monthlyRevenue.compareTo(FIFTY_THOUSAND) < 0) {
            return 12;
        }
        if (monthlyRevenue.compareTo(ONE_HUNDRED_THOUSAND) < 0) {
            return 16;
        }
        return 20;
    }

    private int interactionPotential(AccountOperationalSnapshot snapshot, Instant referenceTime) {
        if (snapshot.lastInteractionAt().isEmpty()) {
            return 0;
        }

        long inactiveDays = ChronoUnit.DAYS.between(snapshot.lastInteractionAt().orElseThrow(), referenceTime);
        if (inactiveDays <= 7) {
            return 15;
        }
        if (inactiveDays <= 30) {
            return 10;
        }
        if (inactiveDays <= 60) {
            return 5;
        }
        return 0;
    }

    private int operationalStabilityPotential(AccountOperationalSnapshot snapshot) {
        long friction = snapshot.delayedOrders()
                + snapshot.openTickets()
                + (snapshot.criticalOpenTickets() * 2);
        if (friction <= 0) {
            return 10;
        }
        if (friction == 1) {
            return 7;
        }
        if (friction == 2) {
            return 4;
        }
        return 0;
    }
}
