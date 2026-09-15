package com.opspilot.service.analytics;

import com.opspilot.service.analytics.model.AccountRiskAssessment;
import com.opspilot.service.analytics.model.RiskLevel;
import com.opspilot.service.model.AccountOperationalSnapshot;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AccountRiskScoringService {

    public AccountRiskAssessment assess(AccountOperationalSnapshot snapshot, Instant referenceTime) {
        int revenueRisk = revenueRisk(snapshot.revenueChangePercentage());
        int delayedOrdersRisk = delayedOrdersRisk(snapshot.delayedOrders());
        int supportTicketsRisk = supportTicketsRisk(snapshot.openTickets());
        int criticalTicketsRisk = criticalTicketsRisk(snapshot.criticalOpenTickets());
        int engagementRisk = engagementRisk(snapshot.engagementScore());
        int inactivityRisk = inactivityRisk(snapshot, referenceTime);
        int score = revenueRisk
                + delayedOrdersRisk
                + supportTicketsRisk
                + criticalTicketsRisk
                + engagementRisk
                + inactivityRisk;

        return new AccountRiskAssessment(
                score,
                RiskLevel.fromScore(score),
                revenueRisk,
                delayedOrdersRisk,
                supportTicketsRisk,
                criticalTicketsRisk,
                engagementRisk,
                inactivityRisk
        );
    }

    private int revenueRisk(BigDecimal revenueChangePercentage) {
        if (revenueChangePercentage.signum() >= 0) {
            return 0;
        }
        if (revenueChangePercentage.compareTo(new BigDecimal("-10")) > 0) {
            return 8;
        }
        if (revenueChangePercentage.compareTo(new BigDecimal("-25")) > 0) {
            return 16;
        }
        if (revenueChangePercentage.compareTo(new BigDecimal("-50")) > 0) {
            return 24;
        }
        return 30;
    }

    private int delayedOrdersRisk(long delayedOrders) {
        if (delayedOrders <= 0) {
            return 0;
        }
        if (delayedOrders == 1) {
            return 7;
        }
        if (delayedOrders == 2) {
            return 14;
        }
        return 20;
    }

    private int supportTicketsRisk(long openTickets) {
        if (openTickets <= 0) {
            return 0;
        }
        if (openTickets == 1) {
            return 5;
        }
        if (openTickets == 2) {
            return 10;
        }
        return 15;
    }

    private int criticalTicketsRisk(long criticalOpenTickets) {
        if (criticalOpenTickets <= 0) {
            return 0;
        }
        if (criticalOpenTickets == 1) {
            return 10;
        }
        return 15;
    }

    private int engagementRisk(int engagementScore) {
        if (engagementScore >= 80) {
            return 0;
        }
        if (engagementScore >= 60) {
            return 2;
        }
        if (engagementScore >= 40) {
            return 5;
        }
        if (engagementScore >= 20) {
            return 8;
        }
        return 10;
    }

    private int inactivityRisk(AccountOperationalSnapshot snapshot, Instant referenceTime) {
        if (snapshot.lastInteractionAt().isEmpty()) {
            return 10;
        }

        long inactiveDays = ChronoUnit.DAYS.between(snapshot.lastInteractionAt().orElseThrow(), referenceTime);
        if (inactiveDays <= 7) {
            return 0;
        }
        if (inactiveDays <= 30) {
            return 3;
        }
        if (inactiveDays <= 60) {
            return 6;
        }
        return 10;
    }
}
