package org.example.resourcebookingappbackend.Service.Notification;

import org.example.resourcebookingappbackend.DTO.Notification.NotificationResponse;
import org.example.resourcebookingappbackend.Exception.ResourceNotFoundException;
import org.example.resourcebookingappbackend.Model.Account;
import org.example.resourcebookingappbackend.Model.Notification;
import org.example.resourcebookingappbackend.Model.Reservation;
import org.example.resourcebookingappbackend.Repository.NotificationRepository;
import org.example.resourcebookingappbackend.Security.AccessControlService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AccessControlService accessControlService;

    public NotificationService(NotificationRepository notificationRepository, AccessControlService accessControlService) {
        this.notificationRepository = notificationRepository;
        this.accessControlService = accessControlService;
    }

    @Transactional
    public void notify(Account account, Reservation reservation, String message) {

        Notification notification = Notification.builder()
                .account(account)
                .reservation(reservation)
                .message(message)
                .readStatus(false)
                .build();

        notificationRepository.save(notification);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications() {

        Account account = accessControlService.getAuthenticatedAccount();

        return notificationRepository.findAllByAccount_IdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {

        Account account = accessControlService.getAuthenticatedAccount();
        Notification notification = findNotificationOrThrow(notificationId);

        accessControlService.assertOwnerOrAdmin(account, notification.getAccount().getId());

        notification.setReadStatus(true);

        return mapToResponse(notificationRepository.save(notification));
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void deleteNotification(UUID notificationId) {

        Account account = accessControlService.getAuthenticatedAccount();
        Notification notification = findNotificationOrThrow(notificationId);

        accessControlService.assertOwnerOrAdmin(account, notification.getAccount().getId());

        notificationRepository.delete(notification);
    }

    private Notification findNotificationOrThrow(UUID notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .reservationId(notification.getReservation() != null ? notification.getReservation().getId() : null)
                .message(notification.getMessage())
                .readStatus(notification.isReadStatus())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}