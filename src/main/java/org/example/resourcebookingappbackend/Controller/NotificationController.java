package org.example.resourcebookingappbackend.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.resourcebookingappbackend.DTO.Notification.NotificationResponse;
import org.example.resourcebookingappbackend.Service.Notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@Tag(
        name = "Notifications",
        description = "In-app notifications for reservation and waitlist status changes"
)
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get my notifications",
            description = "Retrieves the notifications belonging to the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        return ResponseEntity.ok(notificationService.getMyNotifications());
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Mark a notification as read",
            description = "Allows the owning user to mark one of their notifications as read.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Delete a notification",
            description = "Allows the owning user to delete one of their notifications.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}