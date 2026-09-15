package com.opspilot.service;

import com.opspilot.model.Interaction;
import com.opspilot.repository.InteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final InteractionRepository interactionRepository;

    @Transactional(readOnly = true)
    public Optional<Interaction> findLatestInteraction(UUID accountId) {
        return interactionRepository.findFirstByAccount_IdOrderByOccurredAtDescCreatedAtDesc(accountId);
    }
}
