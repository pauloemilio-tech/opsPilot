package com.opspilot.repository;

import com.opspilot.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    boolean existsByName(String name);

    Optional<Account> findByName(String name);

    List<Account> findAllByOrderByNameAsc();
}
