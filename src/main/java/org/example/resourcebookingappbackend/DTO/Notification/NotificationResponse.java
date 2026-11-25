package org.example.resourcebookingappbackend.DTO.Notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private UUID id;
    private UUID reservationId;
    private String message;
    private boolean readStatus;
    private Timestamp createdAt;
}