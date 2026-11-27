package org.example.resourcebookingappbackend.Repository;

import org.example.resourcebookingappbackend.Enum.ReservationStatus;
import org.example.resourcebookingappbackend.Model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.resource.id = :resourceId
              AND r.status = :status
              AND r.startTime < :endTime
              AND r.endTime > :startTime
            """)
    boolean existsOverlapping(
            @Param("resourceId") UUID resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") ReservationStatus status
    );

    @Query("""
            SELECT r FROM Reservation r
            WHERE r.resource.id = :resourceId
              AND r.status = :status
              AND r.startTime < :windowEnd
              AND r.endTime > :windowStart
            ORDER BY r.startTime ASC
            """)
    List<Reservation> findOverlapping(
            @Param("resourceId") UUID resourceId,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd,
            @Param("status") ReservationStatus status
    );

    List<Reservation> findAllByAccount_IdOrderByStartTimeDesc(UUID accountId);
}