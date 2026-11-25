package org.example.resourcebookingappbackend.Service.Reservation;

import org.example.resourcebookingappbackend.DTO.Reservation.ReservationRequest;
import org.example.resourcebookingappbackend.DTO.Reservation.ReservationResponse;
import org.example.resourcebookingappbackend.Enum.ReservationStatus;
import org.example.resourcebookingappbackend.Exception
        .ReservationConflictException;
import org.example.resourcebookingappbackend.Exception.ResourceNotFoundException;
import org.example.resourcebookingappbackend.Model.Account;
import org.example.resourcebookingappbackend.Model.Reservation;
import org.example.resourcebookingappbackend.Model.Resource;
import org.example.resourcebookingappbackend.Repository.ReservationRepository;
import org.example.resourcebookingappbackend.Repository.ResourceRepository;
import org.example.resourcebookingappbackend.Security.AccessControlService;
import org.example.resourcebookingappbackend.Service.Notification.NotificationService;
import org.example.resourcebookingappbackend.Service.Waitlist.WaitlistPromotionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final AccessControlService accessControlService;
    private final NotificationService notificationService;
    private final WaitlistPromotionService waitlistPromotionService;

    public ReservationService(
            ReservationRepository reservationRepository,
            ResourceRepository resourceRepository,
            AccessControlService accessControlService,
            NotificationService notificationService,
            WaitlistPromotionService waitlistPromotionService
    ) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.accessControlService = accessControlService;
        this.notificationService = notificationService;
        this.waitlistPromotionService = waitlistPromotionService;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {

        Account account = accessControlService.getAuthenticatedAccount();

        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        if (!resource.isActive()) {
            throw new IllegalArgumentException("Resource is not currently available for booking");
        }

        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        if (startTime.toLocalTime().isBefore(resource.getOpeningTime())
                || endTime.toLocalTime().isAfter(resource.getClosingTime())) {
            throw new IllegalArgumentException("Reservation must fall within the resource's operating hours");
        }

        boolean overlaps = reservationRepository.existsOverlapping(
                resource.getId(), startTime, endTime, ReservationStatus.CONFIRMED
        );

        if (overlaps) {
            throw new ReservationConflictException("The requested time slot is already booked");
        }

        Reservation reservation = Reservation.builder()
                .resource(resource)
                .account(account)
                .startTime(startTime)
                .endTime(endTime)
                .status(ReservationStatus.CONFIRMED)
                .build();

        Reservation saved;
        try {
            saved = reservationRepository.saveAndFlush(reservation);
        } catch (DataIntegrityViolationException ex) {
            throw new ReservationConflictException("The requested time slot is already booked");
        }

        notificationService.notify(
                saved.getAccount(),
                saved,
                "Your reservation for \"" + saved.getResource().getName() + "\" is confirmed."
        );

        return mapToResponse(saved);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations() {

        Account account = accessControlService.getAuthenticatedAccount();

        return reservationRepository.findAllByAccount_IdOrderByStartTimeDesc(account.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ReservationResponse getReservation(UUID reservationId) {

        Account account = accessControlService.getAuthenticatedAccount();
        Reservation reservation = findReservationOrThrow(reservationId);

        accessControlService.assertOwnerOrAdmin(account, reservation.getAccount().getId());

        return mapToResponse(reservation);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ReservationResponse cancelReservation(UUID reservationId) {

        Account account = accessControlService.getAuthenticatedAccount();
        Reservation reservation = findReservationOrThrow(reservationId);

        accessControlService.assertOwnerOrAdmin(account, reservation.getAccount().getId());

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only confirmed reservations can be canceled");
        }

        reservation.setStatus(ReservationStatus.CANCELED);
        Reservation canceled = reservationRepository.save(reservation);

        notificationService.notify(
                canceled.getAccount(),
                canceled,
                "Your reservation for \"" + canceled.getResource().getName() + "\" has been canceled."
        );

        waitlistPromotionService.promoteNextEligible(
                canceled.getResource(), canceled.getStartTime(), canceled.getEndTime()
        );

        return mapToResponse(canceled);
    }

    private Reservation findReservationOrThrow(UUID reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .resourceId(reservation.getResource().getId())
                .resourceName(reservation.getResource().getName())
                .userId(reservation.getAccount().getId())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}