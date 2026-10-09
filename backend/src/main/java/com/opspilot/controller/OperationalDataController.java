package com.opspilot.controller;

import com.opspilot.dto.interaction.InteractionCreateRequest;
import com.opspilot.dto.interaction.InteractionResponse;
import com.opspilot.dto.order.OrderCreateRequest;
import com.opspilot.dto.order.OrderResponse;
import com.opspilot.dto.ticket.SupportTicketCreateRequest;
import com.opspilot.dto.ticket.SupportTicketResponse;
import com.opspilot.service.InteractionService;
import com.opspilot.service.OrderService;
import com.opspilot.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts/{accountId}")
@RequiredArgsConstructor
public class OperationalDataController {

    private final OrderService orderService;
    private final SupportTicketService supportTicketService;
    private final InteractionService interactionService;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @PathVariable UUID accountId,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(orderService.create(accountId, request)));
    }

    @GetMapping("/orders")
    public List<OrderResponse> findOrders(@PathVariable UUID accountId) {
        return orderService.findAllForAccount(accountId).stream().map(OrderResponse::from).toList();
    }

    @PostMapping("/tickets")
    public ResponseEntity<SupportTicketResponse> createTicket(
            @PathVariable UUID accountId,
            @Valid @RequestBody SupportTicketCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SupportTicketResponse.from(supportTicketService.create(accountId, request)));
    }

    @GetMapping("/tickets")
    public List<SupportTicketResponse> findTickets(@PathVariable UUID accountId) {
        return supportTicketService.findAllForAccount(accountId).stream().map(SupportTicketResponse::from).toList();
    }

    @PostMapping("/interactions")
    public ResponseEntity<InteractionResponse> createInteraction(
            @PathVariable UUID accountId,
            @Valid @RequestBody InteractionCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InteractionResponse.from(interactionService.create(accountId, request)));
    }

    @GetMapping("/interactions")
    public List<InteractionResponse> findInteractions(@PathVariable UUID accountId) {
        return interactionService.findAllForAccount(accountId).stream().map(InteractionResponse::from).toList();
    }
}
