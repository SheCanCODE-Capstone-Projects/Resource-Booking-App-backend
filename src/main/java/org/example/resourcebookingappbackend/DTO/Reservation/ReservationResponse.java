package org.example.resourcebookingappbackend.DTO.Reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.resourcebookingappbackend.Enum.ReservationStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {

    private UUID id;
    private UUID resourceId;
    private String resourceName;
    private UUID userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    private Timestamp createdAt;
}