package com.opspilot.service;

import com.opspilot.dto.interaction.InteractionCreateRequest;
import com.opspilot.model.Account;
import com.opspilot.model.Interaction;
import com.opspilot.model.enums.InteractionType;
import com.opspilot.repository.InteractionRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InteractionServiceTest {

    @Test
    void shouldCreateAndListInteractionsForAccount() {
        InteractionRepository repository = mock(InteractionRepository.class);
        AccountService accountService = mock(AccountService.class);
        InteractionService service = new InteractionService(repository, accountService);
        UUID accountId = UUID.randomUUID();
        Account account = mock(Account.class);
        InteractionCreateRequest request = new InteractionCreateRequest(
                InteractionType.CALL, " Follow-up call ", Instant.parse("2026-09-01T12:00:00Z")
        );
        when(accountService.findById(accountId)).thenReturn(account);
        when(repository.save(org.mockito.ArgumentMatchers.any(Interaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Interaction created = service.create(accountId, request);
        when(repository.findAllByAccount_IdOrderByOccurredAtDescCreatedAtDesc(accountId))
                .thenReturn(List.of(created));

        assertThat(created.getAccount()).isSameAs(account);
        assertThat(created.getSummary()).isEqualTo("Follow-up call");
        assertThat(service.findAllForAccount(accountId)).containsExactly(created);
        verify(accountService, org.mockito.Mockito.times(2)).findById(accountId);
    }
}
