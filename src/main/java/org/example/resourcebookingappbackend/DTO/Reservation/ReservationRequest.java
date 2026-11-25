package org.example.resourcebookingappbackend.DTO.Reservation;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReservationRequest {

    @NotNull(message = "Resource id is required")
    private UUID resourceId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
}