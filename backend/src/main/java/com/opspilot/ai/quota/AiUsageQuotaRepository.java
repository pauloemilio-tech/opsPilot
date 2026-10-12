package com.opspilot.ai.quota;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;

public interface AiUsageQuotaRepository extends JpaRepository<AiUsageQuota, java.util.UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from AiUsageQuota q where q.scope = :scope and q.visitorHash = :visitorHash and q.usageDate = :usageDate")
    Optional<AiUsageQuota> findForUpdateByScopeAndVisitor(@Param("scope") AiUsageQuotaScope scope,
                                                         @Param("visitorHash") String visitorHash,
                                                         @Param("usageDate") LocalDate usageDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from AiUsageQuota q where q.scope = :scope and q.visitorHash is null and q.usageDate = :usageDate")
    Optional<AiUsageQuota> findGlobalForUpdate(@Param("scope") AiUsageQuotaScope scope,
                                              @Param("usageDate") LocalDate usageDate);
}
