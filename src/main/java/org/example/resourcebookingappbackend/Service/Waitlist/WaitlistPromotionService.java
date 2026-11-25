package org.example.resourcebookingappbackend.Service.Waitlist;

import org.example.resourcebookingappbackend.Enum.ReservationStatus;
import org.example.resourcebookingappbackend.Enum.WaitlistStatus;
import org.example.resourcebookingappbackend.Model.Reservation;
import org.example.resourcebookingappbackend.Model.Resource;
import org.example.resourcebookingappbackend.Model.WaitlistEntry;
import org.example.resourcebookingappbackend.Repository.ReservationRepository;
import org.example.resourcebookingappbackend.Repository.WaitlistEntryRepository;
import org.example.resourcebookingappbackend.Service.Notification.NotificationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Promotes, in strict FIFO order, the earliest waiting-list entry whose requested
 * interval can now be confirmed without violating the resource's overlap constraint.
 * Only one entry is promoted per freed slot.
 */
@Service
public class WaitlistPromotionService {

    private final WaitlistEntryRepository waitlistEntryRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    public WaitlistPromotionService(
            WaitlistEntryRepository waitlistEntryRepository,
            ReservationRepository reservationRepository,
            NotificationService notificationService
    ) {
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void promoteNextEligible(Resource resource, LocalDateTime freedStart, LocalDateTime freedEnd) {

        List<WaitlistEntry> candidates = waitlistEntryRepository.findEligibleForPromotion(
                resource.getId(), freedStart, freedEnd, WaitlistStatus.WAITING
        );

        for (WaitlistEntry candidate : candidates) {

            boolean stillOverlapsAnotherConfirmed = reservationRepository.existsOverlapping(
                    resource.getId(), candidate.getStartTime(), candidate.getEndTime(), ReservationStatus.CONFIRMED
            );

            if (stillOverlapsAnotherConfirmed) {
                continue;
            }

            Reservation reservation = Reservation.builder()
                    .resource(resource)
                    .account(candidate.getAccount())
                    .startTime(candidate.getStartTime())
                    .endTime(candidate.getEndTime())
                    .status(ReservationStatus.CONFIRMED)
                    .build();

            try {
                reservation = reservationRepository.saveAndFlush(reservation);
            } catch (DataIntegrityViolationException ex) {
                continue;
            }

            candidate.setStatus(WaitlistStatus.PROMOTED);
            waitlistEntryRepository.save(candidate);

            notificationService.notify(
                    candidate.getAccount(),
                    reservation,
                    "You've been promoted from the waiting list - your reservation for \""
                            + resource.getName() + "\" is now confirmed."
            );

            return;
        }
    }
}