package org.example.resourcebookingappbackend.DTO;

import org.example.resourcebookingappbackend.Model.Reservation;
import org.example.resourcebookingappbackend.Enum.ReservationStatus;

import java.time.LocalDateTime; // type for a date and time


public record ReservationResponse(
        Long id,
        Long resourceId,
        String resourceName, // name of the booked resource
        String userId, // who booked it
        LocalDateTime startTime, // booking start
        LocalDateTime endTime, // booking end
        ReservationStatus status, // CONFIRMED, CANCELED or WAITLISTED
        LocalDateTime createdAt, // when it was made
        LocalDateTime updatedAt // when it was last changed
) {


    public static ReservationResponse from(Reservation reservation) {return new ReservationResponse(
                reservation.getId(), reservation.getResource().getId(), reservation.getResource().getName(),
                reservation.getUserId(), reservation.getStartTime(), reservation.getEndTime(),
                reservation.getStatus(), reservation.getCreatedAt(), reservation.getUpdatedAt());
    }
}