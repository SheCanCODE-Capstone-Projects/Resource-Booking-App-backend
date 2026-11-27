package org.example.resourcebookingappbackend.DTO.Waitlist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.resourcebookingappbackend.Enum.WaitlistStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistResponse {

    private UUID id;
    private UUID resourceId;
    private String resourceName;
    private UUID userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private WaitlistStatus status;
    private Timestamp queuedAt;
}