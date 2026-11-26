package com.reserveflow.repository;

import com.reserveflow.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository
extends JpaRepository<Reservation,Long> {
    List<Reservation> findOverlapping(long resourceId, LocalDateTime startTime, LocalDateTime endTime);

    List<Reservation> findByUserId(int userId);
}
