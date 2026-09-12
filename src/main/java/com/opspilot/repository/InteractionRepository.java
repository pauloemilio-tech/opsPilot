package com.opspilot.repository;

import com.opspilot.model.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InteractionRepository extends JpaRepository<Interaction, UUID> {
}
