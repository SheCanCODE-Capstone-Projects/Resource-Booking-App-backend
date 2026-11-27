package org.example.resourcebookingappbackend.Repository;

import org.example.resourcebookingappbackend.Enum.WaitlistStatus;
import org.example.resourcebookingappbackend.Model.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, UUID> {

    boolean existsByResource_IdAndAccount_IdAndStartTimeAndEndTimeAndStatus(
            UUID resourceId, UUID accountId, LocalDateTime startTime, LocalDateTime endTime, WaitlistStatus status
    );

    List<WaitlistEntry> findAllByAccount_IdOrderByQueuedAtAsc(UUID accountId);

    List<WaitlistEntry> findAllByResource_IdAndStatusOrderByQueuedAtAsc(UUID resourceId, WaitlistStatus status);

    @Query("""
            SELECT w FROM WaitlistEntry w
            WHERE w.resource.id = :resourceId
              AND w.status = :status
              AND w.startTime < :windowEnd
              AND w.endTime > :windowStart
            ORDER BY w.queuedAt ASC
            """)
    List<WaitlistEntry> findEligibleForPromotion(
            @Param("resourceId") UUID resourceId,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd,
            @Param("status") WaitlistStatus status
    );
}