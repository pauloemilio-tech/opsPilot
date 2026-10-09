package com.opspilot.controller;

import com.opspilot.exception.ConflictingOrderNumberException;
import com.opspilot.exception.InvalidOperationalDataException;
import com.opspilot.model.Account;
import com.opspilot.model.Interaction;
import com.opspilot.model.Order;
import com.opspilot.model.SupportTicket;
import com.opspilot.model.enums.InteractionType;
import com.opspilot.model.enums.OrderStatus;
import com.opspilot.model.enums.TicketPriority;
import com.opspilot.model.enums.TicketStatus;
import com.opspilot.service.InteractionService;
import com.opspilot.service.OrderService;
import com.opspilot.service.SupportTicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OperationalDataController.class)
class OperationalDataControllerTest {

    private static final UUID ACCOUNT_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID RECORD_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private SupportTicketService supportTicketService;

    @MockitoBean
    private InteractionService interactionService;

    @Test
    void shouldCreateAndReadOrders() throws Exception {
        Order order = order();
        when(orderService.create(org.mockito.ArgumentMatchers.eq(ACCOUNT_ID), org.mockito.ArgumentMatchers.any()))
                .thenReturn(order);
        when(orderService.findAllForAccount(ACCOUNT_ID)).thenReturn(List.of(order));

        mockMvc.perform(post("/api/accounts/{accountId}/orders", ACCOUNT_ID)
                        .contentType("application/json")
                        .content("""
                                {"orderNumber":"ORD-42","amount":350.00,"status":"PROCESSING",
                                 "orderedAt":"2026-09-01T12:00:00Z","expectedDeliveryAt":"2026-09-10T12:00:00Z"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(ACCOUNT_ID.toString()))
                .andExpect(jsonPath("$.orderNumber").value("ORD-42"));

        mockMvc.perform(get("/api/accounts/{accountId}/orders", ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(RECORD_ID.toString()));
    }

    @Test
    void shouldCreateAndReadTickets() throws Exception {
        SupportTicket ticket = ticket();
        when(supportTicketService.create(org.mockito.ArgumentMatchers.eq(ACCOUNT_ID), org.mockito.ArgumentMatchers.any()))
                .thenReturn(ticket);
        when(supportTicketService.findAllForAccount(ACCOUNT_ID)).thenReturn(List.of(ticket));

        mockMvc.perform(post("/api/accounts/{accountId}/tickets", ACCOUNT_ID)
                        .contentType("application/json")
                        .content("""
                                {"subject":"Payment issue","status":"OPEN","priority":"CRITICAL",
                                 "openedAt":"2026-09-01T12:00:00Z"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject").value("Payment issue"));

        mockMvc.perform(get("/api/accounts/{accountId}/tickets", ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value("CRITICAL"));
    }

    @Test
    void shouldCreateAndReadInteractions() throws Exception {
        Interaction interaction = interaction();
        when(interactionService.create(org.mockito.ArgumentMatchers.eq(ACCOUNT_ID), org.mockito.ArgumentMatchers.any()))
                .thenReturn(interaction);
        when(interactionService.findAllForAccount(ACCOUNT_ID)).thenReturn(List.of(interaction));

        mockMvc.perform(post("/api/accounts/{accountId}/interactions", ACCOUNT_ID)
                        .contentType("application/json")
                        .content("""
                                {"type":"CALL","summary":"Renewal follow-up","occurredAt":"2026-09-01T12:00:00Z"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.summary").value("Renewal follow-up"));

        mockMvc.perform(get("/api/accounts/{accountId}/interactions", ACCOUNT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("CALL"));
    }

    @Test
    void shouldRejectBlankOperationalIdentifiersAndText() throws Exception {
        mockMvc.perform(post("/api/accounts/{accountId}/orders", ACCOUNT_ID)
                        .contentType("application/json")
                        .content("""
                                {"orderNumber":" ","amount":-1,"status":"PENDING",
                                 "orderedAt":"2026-09-01T12:00:00Z"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("orderNumber")));
    }

    @Test
    void shouldReturnConflictForDuplicateOrderNumber() throws Exception {
        when(orderService.create(org.mockito.ArgumentMatchers.eq(ACCOUNT_ID), org.mockito.ArgumentMatchers.any()))
                .thenThrow(new ConflictingOrderNumberException("ORD-42"));

        mockMvc.perform(post("/api/accounts/{accountId}/orders", ACCOUNT_ID)
                        .contentType("application/json")
                        .content("""
                                {"orderNumber":"ORD-42","amount":350.00,"status":"PROCESSING",
                                 "orderedAt":"2026-09-01T12:00:00Z"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Order number already exists: ORD-42"));
    }

    @Test
    void shouldReturnBadRequestForInvalidOperationalDates() throws Exception {
        when(supportTicketService.create(
                org.mockito.ArgumentMatchers.eq(ACCOUNT_ID), org.mockito.ArgumentMatchers.any()
        )).thenThrow(new InvalidOperationalDataException("resolvedAt must not be before openedAt"));

        mockMvc.perform(post("/api/accounts/{accountId}/tickets", ACCOUNT_ID)
                        .contentType("application/json")
                        .content("""
                                {"subject":"Payment issue","status":"RESOLVED","priority":"HIGH",
                                 "openedAt":"2026-09-02T12:00:00Z","resolvedAt":"2026-09-01T12:00:00Z"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("resolvedAt must not be before openedAt"));
    }

    private Order order() {
        Order order = mock(Order.class);
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(ACCOUNT_ID);
        when(order.getId()).thenReturn(RECORD_ID);
        when(order.getAccount()).thenReturn(account);
        when(order.getOrderNumber()).thenReturn("ORD-42");
        when(order.getAmount()).thenReturn(new BigDecimal("350.00"));
        when(order.getStatus()).thenReturn(OrderStatus.PROCESSING);
        when(order.getOrderedAt()).thenReturn(Instant.parse("2026-09-01T12:00:00Z"));
        return order;
    }

    private SupportTicket ticket() {
        SupportTicket ticket = mock(SupportTicket.class);
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(ACCOUNT_ID);
        when(ticket.getId()).thenReturn(RECORD_ID);
        when(ticket.getAccount()).thenReturn(account);
        when(ticket.getSubject()).thenReturn("Payment issue");
        when(ticket.getStatus()).thenReturn(TicketStatus.OPEN);
        when(ticket.getPriority()).thenReturn(TicketPriority.CRITICAL);
        when(ticket.getOpenedAt()).thenReturn(Instant.parse("2026-09-01T12:00:00Z"));
        return ticket;
    }

    private Interaction interaction() {
        Interaction interaction = mock(Interaction.class);
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(ACCOUNT_ID);
        when(interaction.getId()).thenReturn(RECORD_ID);
        when(interaction.getAccount()).thenReturn(account);
        when(interaction.getType()).thenReturn(InteractionType.CALL);
        when(interaction.getSummary()).thenReturn("Renewal follow-up");
        when(interaction.getOccurredAt()).thenReturn(Instant.parse("2026-09-01T12:00:00Z"));
        return interaction;
    }
}
