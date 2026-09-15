package com.opspilot.seed;

import com.opspilot.model.Account;
import com.opspilot.model.Interaction;
import com.opspilot.model.Order;
import com.opspilot.model.SupportTicket;
import com.opspilot.model.enums.AccountStatus;
import com.opspilot.model.enums.InteractionType;
import com.opspilot.model.enums.OrderStatus;
import com.opspilot.model.enums.TicketPriority;
import com.opspilot.model.enums.TicketStatus;
import com.opspilot.repository.AccountRepository;
import com.opspilot.repository.InteractionRepository;
import com.opspilot.repository.OrderRepository;
import com.opspilot.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevelopmentDataSeeder implements ApplicationRunner {

    public static final Instant REFERENCE_TIME = Instant.parse("2026-09-01T12:00:00Z");
    public static final String ORDER_PREFIX = "DEMO-";

    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final InteractionRepository interactionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        SeedTotals totals = seedAccounts();
        log.info(
                "Development seed finished: {} accounts, {} orders, {} support tickets and {} interactions created",
                totals.accounts(), totals.orders(), totals.tickets(), totals.interactions()
        );
    }

    private SeedTotals seedAccounts() {
        SeedTotals totals = SeedTotals.EMPTY;

        totals = totals.add(seedAccount(
                account("Northstar Retail", "Retail", "North America", AccountStatus.ACTIVE, "68000.00", "62000.00", 91),
                orders(
                        order("NORTH-001", "14200.00", OrderStatus.DELIVERED, -35, -27, -28),
                        order("NORTH-002", "16800.00", OrderStatus.DELIVERED, -22, -14, -15),
                        order("NORTH-003", "18500.00", OrderStatus.SHIPPED, -4, 3, null),
                        order("NORTH-004", "18500.00", OrderStatus.PROCESSING, -2, 6, null)),
                tickets(ticket("Invoice copy request", TicketStatus.CLOSED, TicketPriority.LOW, -20, -19)),
                interactions(
                        interaction(InteractionType.MEETING, "Quarterly account review", -1),
                        interaction(InteractionType.EMAIL, "Confirmed seasonal order forecast", -5),
                        interaction(InteractionType.CALL, "Reviewed delivery performance", -12),
                        interaction(InteractionType.NOTE, "Account health remains strong", -25))));

        totals = totals.add(seedAccount(
                account("Apex Commerce", "E-commerce", "Europe", AccountStatus.ACTIVE, "42500.00", "71000.00", 64),
                orders(
                        order("APEX-001", "11800.00", OrderStatus.DELIVERED, -32, -24, -25),
                        order("APEX-002", "9700.00", OrderStatus.DELIVERED, -19, -11, -11),
                        order("APEX-003", "10600.00", OrderStatus.SHIPPED, -5, 4, null),
                        order("APEX-004", "10400.00", OrderStatus.PENDING, -1, 8, null)),
                tickets(
                        ticket("Catalog synchronization question", TicketStatus.RESOLVED, TicketPriority.LOW, -18, -16),
                        ticket("Payment terms clarification", TicketStatus.CLOSED, TicketPriority.MEDIUM, -9, -7)),
                interactions(
                        interaction(InteractionType.CALL, "Discussed lower monthly order volume", -4),
                        interaction(InteractionType.EMAIL, "Shared retention proposal", -10),
                        interaction(InteractionType.MEETING, "Reviewed commercial performance", -24))));

        totals = totals.add(seedAccount(
                account("BluePeak Technologies", "Technology", "APAC", AccountStatus.ACTIVE, "54000.00", "52500.00", 70),
                orders(
                        order("BLUE-001", "13200.00", OrderStatus.DELAYED, -24, -12, null),
                        order("BLUE-002", "14800.00", OrderStatus.DELAYED, -18, -7, null),
                        order("BLUE-003", "12600.00", OrderStatus.DELAYED, -13, -2, null),
                        order("BLUE-004", "13400.00", OrderStatus.DELIVERED, -30, -21, -20)),
                tickets(
                        ticket("Shipment tracking unavailable", TicketStatus.IN_PROGRESS, TicketPriority.MEDIUM, -6, null),
                        ticket("Delivery confirmation request", TicketStatus.RESOLVED, TicketPriority.LOW, -16, -14)),
                interactions(
                        interaction(InteractionType.CALL, "Discussed delayed shipment escalation", -3),
                        interaction(InteractionType.EMAIL, "Shared revised delivery plan", -8))));

        totals = totals.add(seedAccount(
                account("Vertex Health", "Healthcare", "North America", AccountStatus.ACTIVE, "78000.00", "76500.00", 58),
                orders(
                        order("VERT-001", "21000.00", OrderStatus.DELIVERED, -27, -18, -19),
                        order("VERT-002", "19500.00", OrderStatus.DELIVERED, -17, -9, -9),
                        order("VERT-003", "20500.00", OrderStatus.SHIPPED, -4, 4, null),
                        order("VERT-004", "17000.00", OrderStatus.PROCESSING, -2, 7, null)),
                tickets(
                        ticket("Production access unavailable", TicketStatus.OPEN, TicketPriority.CRITICAL, -5, null),
                        ticket("Data export timing out", TicketStatus.IN_PROGRESS, TicketPriority.HIGH, -8, null),
                        ticket("User provisioning backlog", TicketStatus.WAITING_CUSTOMER, TicketPriority.MEDIUM, -11, null),
                        ticket("Report layout correction", TicketStatus.RESOLVED, TicketPriority.LOW, -20, -17)),
                interactions(
                        interaction(InteractionType.CALL, "Coordinated critical support response", -2),
                        interaction(InteractionType.FOLLOW_UP, "Reviewed open incident actions", -6),
                        interaction(InteractionType.MEETING, "Service review with operations team", -15))));

        totals = totals.add(seedAccount(
                account("Evergreen Markets", "Retail", "LATAM", AccountStatus.ACTIVE, "31500.00", "32000.00", 22),
                orders(
                        order("EVER-001", "11000.00", OrderStatus.DELIVERED, -95, -87, -88),
                        order("EVER-002", "10500.00", OrderStatus.DELIVERED, -70, -61, -62),
                        order("EVER-003", "10000.00", OrderStatus.CANCELLED, -48, -40, null)),
                tickets(ticket("Update billing contact", TicketStatus.CLOSED, TicketPriority.LOW, -72, -70)),
                interactions(
                        interaction(InteractionType.EMAIL, "Requested account planning meeting", -63),
                        interaction(InteractionType.NOTE, "No response to recent outreach", -94))));

        totals = totals.add(seedAccount(
                account("Nova Distribution", "Distribution", "Europe", AccountStatus.ACTIVE, "118000.00", "98000.00", 96),
                orders(
                        order("NOVA-001", "27500.00", OrderStatus.DELIVERED, -28, -20, -21),
                        order("NOVA-002", "29000.00", OrderStatus.DELIVERED, -16, -8, -9),
                        order("NOVA-003", "31500.00", OrderStatus.SHIPPED, -4, 5, null),
                        order("NOVA-004", "30000.00", OrderStatus.PROCESSING, -1, 9, null)),
                tickets(ticket("Add warehouse notification contact", TicketStatus.RESOLVED, TicketPriority.LOW, -14, -13)),
                interactions(
                        interaction(InteractionType.MEETING, "Reviewed expansion opportunity", -1),
                        interaction(InteractionType.FOLLOW_UP, "Followed up on renewal opportunity", -3),
                        interaction(InteractionType.CALL, "Confirmed additional warehouse demand", -7),
                        interaction(InteractionType.EMAIL, "Shared volume pricing proposal", -11))));

        totals = totals.add(seedAccount(
                account("Horizon Supply", "Distribution", "LATAM", AccountStatus.ACTIVE, "29000.00", "61000.00", 18),
                orders(
                        order("HORI-001", "7200.00", OrderStatus.DELAYED, -38, -25, null),
                        order("HORI-002", "6800.00", OrderStatus.DELAYED, -31, -18, null),
                        order("HORI-003", "5900.00", OrderStatus.DELAYED, -22, -10, null),
                        order("HORI-004", "4700.00", OrderStatus.DELAYED, -14, -4, null),
                        order("HORI-005", "4400.00", OrderStatus.CANCELLED, -9, -2, null)),
                tickets(
                        ticket("Repeated fulfillment failures", TicketStatus.OPEN, TicketPriority.CRITICAL, -21, null),
                        ticket("Missing items in shipment", TicketStatus.IN_PROGRESS, TicketPriority.HIGH, -16, null),
                        ticket("Credit request pending", TicketStatus.WAITING_CUSTOMER, TicketPriority.HIGH, -12, null),
                        ticket("Incorrect freight charge", TicketStatus.OPEN, TicketPriority.MEDIUM, -9, null)),
                interactions(
                        interaction(InteractionType.EMAIL, "Sent unresolved issue summary", -47),
                        interaction(InteractionType.CALL, "Escalated account recovery plan", -66))));

        totals = totals.add(seedAccount(
                account("Summit Goods", "Consumer Goods", "APAC", AccountStatus.ONBOARDING, "8500.00", "0.00", 74),
                orders(
                        order("SUMM-001", "5000.00", OrderStatus.PROCESSING, -3, 6, null),
                        order("SUMM-002", "3500.00", OrderStatus.PENDING, -1, 10, null)),
                tickets(ticket("Initial catalog configuration", TicketStatus.WAITING_CUSTOMER, TicketPriority.MEDIUM, -4, null)),
                interactions(
                        interaction(InteractionType.MEETING, "Reviewed onboarding progress", -1),
                        interaction(InteractionType.EMAIL, "Shared integration checklist", -3),
                        interaction(InteractionType.CALL, "Completed onboarding kickoff", -6))));

        totals = totals.add(seedAccount(
                account("Atlas Consumer Products", "Consumer Goods", "Europe", AccountStatus.INACTIVE, "0.00", "0.00", 5),
                orders(order("ATLA-001", "8900.00", OrderStatus.DELIVERED, -190, -180, -181)),
                tickets(ticket("Historical invoice request", TicketStatus.CLOSED, TicketPriority.LOW, -170, -168)),
                interactions()));

        totals = totals.add(seedAccount(
                account("Pulse Systems", "Technology", "North America", AccountStatus.ACTIVE, "92000.00", "74000.00", 88),
                orders(
                        order("PULS-001", "23500.00", OrderStatus.DELIVERED, -26, -18, -19),
                        order("PULS-002", "22500.00", OrderStatus.DELIVERED, -15, -7, -7),
                        order("PULS-003", "24000.00", OrderStatus.SHIPPED, -4, 5, null),
                        order("PULS-004", "22000.00", OrderStatus.PROCESSING, -2, 8, null)),
                tickets(
                        ticket("API requests intermittently failing", TicketStatus.OPEN, TicketPriority.CRITICAL, -4, null),
                        ticket("Dashboard totals inconsistent", TicketStatus.IN_PROGRESS, TicketPriority.HIGH, -7, null),
                        ticket("Webhook delivery delays", TicketStatus.OPEN, TicketPriority.HIGH, -10, null),
                        ticket("Export format enhancement", TicketStatus.RESOLVED, TicketPriority.MEDIUM, -17, -14)),
                interactions(
                        interaction(InteractionType.MEETING, "Reviewed growth plan and support issues", -1),
                        interaction(InteractionType.CALL, "Escalated API reliability concerns", -3),
                        interaction(InteractionType.FOLLOW_UP, "Confirmed incident action owners", -6),
                        interaction(InteractionType.EMAIL, "Shared expansion forecast", -9))));

        totals = totals.add(seedAccount(
                account("Meridian Home", "E-commerce", "LATAM", AccountStatus.ACTIVE, "47000.00", "47000.00", 67),
                orders(
                        order("MERI-001", "9400.00", OrderStatus.DELIVERED, -29, -21, -22),
                        order("MERI-002", "9600.00", OrderStatus.DELIVERED, -18, -10, -10),
                        order("MERI-003", "9300.00", OrderStatus.SHIPPED, -5, 3, null),
                        order("MERI-004", "9200.00", OrderStatus.PROCESSING, -3, 6, null),
                        order("MERI-005", "9500.00", OrderStatus.PENDING, -1, 9, null)),
                tickets(
                        ticket("Return policy clarification", TicketStatus.CLOSED, TicketPriority.LOW, -25, -23),
                        ticket("Address validation question", TicketStatus.RESOLVED, TicketPriority.MEDIUM, -12, -11),
                        ticket("Promotion setup assistance", TicketStatus.IN_PROGRESS, TicketPriority.MEDIUM, -3, null),
                        ticket("Product feed warning", TicketStatus.WAITING_CUSTOMER, TicketPriority.LOW, -6, null)),
                interactions(
                        interaction(InteractionType.EMAIL, "Confirmed next campaign schedule", -2),
                        interaction(InteractionType.CALL, "Reviewed steady account performance", -8),
                        interaction(InteractionType.NOTE, "Monitoring product feed warning", -14))));

        return totals;
    }

    private SeedTotals seedAccount(
            AccountData accountData,
            List<OrderData> orders,
            List<TicketData> tickets,
            List<InteractionData> interactions
    ) {
        if (accountRepository.existsByName(accountData.name())) {
            return SeedTotals.EMPTY;
        }

        Account account = accountRepository.save(new Account(
                accountData.name(),
                accountData.industry(),
                accountData.region(),
                accountData.status(),
                accountData.monthlyRevenue(),
                accountData.previousMonthRevenue(),
                accountData.engagementScore()
        ));

        orderRepository.saveAll(orders.stream()
                .map(data -> new Order(
                        account,
                        ORDER_PREFIX + data.number(),
                        data.amount(),
                        data.status(),
                        data.orderedAt(),
                        data.expectedDeliveryAt(),
                        data.deliveredAt()
                ))
                .toList());
        supportTicketRepository.saveAll(tickets.stream()
                .map(data -> new SupportTicket(
                        account,
                        data.subject(),
                        data.status(),
                        data.priority(),
                        data.openedAt(),
                        data.resolvedAt()
                ))
                .toList());
        interactionRepository.saveAll(interactions.stream()
                .map(data -> new Interaction(account, data.type(), data.summary(), data.occurredAt()))
                .toList());

        return new SeedTotals(1, orders.size(), tickets.size(), interactions.size());
    }

    private static AccountData account(
            String name,
            String industry,
            String region,
            AccountStatus status,
            String monthlyRevenue,
            String previousMonthRevenue,
            int engagementScore
    ) {
        return new AccountData(
                name,
                industry,
                region,
                status,
                new BigDecimal(monthlyRevenue),
                new BigDecimal(previousMonthRevenue),
                engagementScore
        );
    }

    private static OrderData order(
            String number,
            String amount,
            OrderStatus status,
            int orderedDay,
            int expectedDeliveryDay,
            Integer deliveredDay
    ) {
        return new OrderData(
                number,
                new BigDecimal(amount),
                status,
                day(orderedDay),
                day(expectedDeliveryDay),
                deliveredDay == null ? null : day(deliveredDay)
        );
    }

    private static TicketData ticket(
            String subject,
            TicketStatus status,
            TicketPriority priority,
            int openedDay,
            Integer resolvedDay
    ) {
        return new TicketData(
                subject,
                status,
                priority,
                day(openedDay),
                resolvedDay == null ? null : day(resolvedDay)
        );
    }

    private static InteractionData interaction(InteractionType type, String summary, int occurredDay) {
        return new InteractionData(type, summary, day(occurredDay));
    }

    private static Instant day(int offset) {
        return REFERENCE_TIME.plus(offset, ChronoUnit.DAYS);
    }

    private static List<OrderData> orders(OrderData... orders) {
        return Arrays.asList(orders);
    }

    private static List<TicketData> tickets(TicketData... tickets) {
        return Arrays.asList(tickets);
    }

    private static List<InteractionData> interactions(InteractionData... interactions) {
        return Arrays.asList(interactions);
    }

    private record AccountData(
            String name,
            String industry,
            String region,
            AccountStatus status,
            BigDecimal monthlyRevenue,
            BigDecimal previousMonthRevenue,
            int engagementScore
    ) {
    }

    private record OrderData(
            String number,
            BigDecimal amount,
            OrderStatus status,
            Instant orderedAt,
            Instant expectedDeliveryAt,
            Instant deliveredAt
    ) {
    }

    private record TicketData(
            String subject,
            TicketStatus status,
            TicketPriority priority,
            Instant openedAt,
            Instant resolvedAt
    ) {
    }

    private record InteractionData(InteractionType type, String summary, Instant occurredAt) {
    }

    private record SeedTotals(int accounts, int orders, int tickets, int interactions) {

        private static final SeedTotals EMPTY = new SeedTotals(0, 0, 0, 0);

        private SeedTotals add(SeedTotals other) {
            return new SeedTotals(
                    accounts + other.accounts,
                    orders + other.orders,
                    tickets + other.tickets,
                    interactions + other.interactions
            );
        }
    }
}
