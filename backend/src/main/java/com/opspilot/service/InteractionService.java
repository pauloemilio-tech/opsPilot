package com.opspilot.service;

import com.opspilot.dto.interaction.InteractionCreateRequest;
import com.opspilot.model.Account;
import com.opspilot.model.Interaction;
import com.opspilot.repository.InteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final AccountService accountService;

    @Transactional
    public Interaction create(UUID accountId, InteractionCreateRequest request) {
        Account account = accountService.findById(accountId);
        return interactionRepository.save(new Interaction(
                account,
                request.type(),
                request.summary().trim(),
                request.occurredAt()
        ));
    }

    @Transactional(readOnly = true)
    public List<Interaction> findAllForAccount(UUID accountId) {
        accountService.findById(accountId);
        return interactionRepository.findAllByAccount_IdOrderByOccurredAtDescCreatedAtDesc(accountId);
    }

    @Transactional(readOnly = true)
    public Optional<Interaction> findLatestInteraction(UUID accountId) {
        return interactionRepository.findFirstByAccount_IdOrderByOccurredAtDescCreatedAtDesc(accountId);
    }
}
