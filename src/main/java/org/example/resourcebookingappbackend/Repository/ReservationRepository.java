package org.example.resourcebookingappbackend.Repository;

import org.example.resourcebookingappbackend.Model.Reservation;
import org.example.resourcebookingappbackend.Model.ReservationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    @Query("select count(reserve) from Reservation reserve where reserve.resource.id = :resourceId " +
            "and reserve.status = :status " +
            "and reserve.startTime < :endTime and reserve.endTime > :startTime")
    long countOverlapping(@Param("resourceId") Long resourceId,
                          @Param("status") ReservationStatus status,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);


    @Query("select reserve from Reservation reserve where reserve.resource.id = :resourceId " +
            "and reserve.status = :status " +
            "and reserve.startTime < :dayEnd and reserve.endTime > :dayStart " +
            "order by reserve.startTime")
    List<Reservation> findForDay(@Param("resourceId") Long resourceId,
                                 @Param("status") ReservationStatus status,
                                 @Param("dayStart") LocalDateTime dayStart,
                                 @Param("dayEnd") LocalDateTime dayEnd);

    @EntityGraph(attributePaths = "resource")
    List<Reservation> findByUserIdOrderByStartTimeDesc(String userId);
}