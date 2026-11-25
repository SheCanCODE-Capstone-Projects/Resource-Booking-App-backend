package org.example.resourcebookingappbackend.Service.Waitlist;

import org.example.resourcebookingappbackend.DTO.Waitlist.WaitlistRequest;
import org.example.resourcebookingappbackend.DTO.Waitlist.WaitlistResponse;
import org.example.resourcebookingappbackend.Enum.WaitlistStatus;
import org.example.resourcebookingappbackend.Exception.ReservationConflictException;
import org.example.resourcebookingappbackend.Exception.ResourceNotFoundException;
import org.example.resourcebookingappbackend.Model.Account;
import org.example.resourcebookingappbackend.Model.Resource;
import org.example.resourcebookingappbackend.Model.WaitlistEntry;
import org.example.resourcebookingappbackend.Repository.ResourceRepository;
import org.example.resourcebookingappbackend.Repository.WaitlistEntryRepository;
import org.example.resourcebookingappbackend.Security.AccessControlService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WaitlistService {

    private final WaitlistEntryRepository waitlistEntryRepository;
    private final ResourceRepository resourceRepository;
    private final AccessControlService accessControlService;

    public WaitlistService(
            WaitlistEntryRepository waitlistEntryRepository,
            ResourceRepository resourceRepository,
            AccessControlService accessControlService
    ) {
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.resourceRepository = resourceRepository;
        this.accessControlService = accessControlService;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public WaitlistResponse joinWaitlist(WaitlistRequest request) {

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
            throw new IllegalArgumentException("Requested interval must fall within the resource's operating hours");
        }

        boolean duplicate = waitlistEntryRepository.existsByResource_IdAndAccount_IdAndStartTimeAndEndTimeAndStatus(
                resource.getId(), account.getId(), startTime, endTime, WaitlistStatus.WAITING
        );

        if (duplicate) {
            throw new ReservationConflictException("You are already on the waiting list for this resource and time slot");
        }

        WaitlistEntry entry = WaitlistEntry.builder()
                .resource(resource)
                .account(account)
                .startTime(startTime)
                .endTime(endTime)
                .status(WaitlistStatus.WAITING)
                .build();

        WaitlistEntry saved;
        try {
            saved = waitlistEntryRepository.saveAndFlush(entry);
        } catch (DataIntegrityViolationException ex) {
            throw new ReservationConflictException("You are already on the waiting list for this resource and time slot");
        }

        return mapToResponse(saved);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<WaitlistResponse> getMyWaitlistEntries() {

        Account account = accessControlService.getAuthenticatedAccount();

        return waitlistEntryRepository.findAllByAccount_IdOrderByQueuedAtAsc(account.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void leaveWaitlist(UUID entryId) {

        Account account = accessControlService.getAuthenticatedAccount();
        WaitlistEntry entry = findEntryOrThrow(entryId);

        accessControlService.assertOwnerOrAdmin(account, entry.getAccount().getId());

        if (entry.getStatus() == WaitlistStatus.WAITING) {
            entry.setStatus(WaitlistStatus.CANCELED);
            waitlistEntryRepository.save(entry);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PUBLISHER')")
    @Transactional(readOnly = true)
    public List<WaitlistResponse> getWaitlistForResource(UUID resourceId) {

        Account account = accessControlService.getAuthenticatedAccount();

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        accessControlService.assertOwnerOrAdmin(account, resource.getOwner().getId());

        return waitlistEntryRepository.findAllByResource_IdAndStatusOrderByQueuedAtAsc(resourceId, WaitlistStatus.WAITING)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private WaitlistEntry findEntryOrThrow(UUID entryId) {
        return waitlistEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry not found"));
    }

    private WaitlistResponse mapToResponse(WaitlistEntry entry) {
        return WaitlistResponse.builder()
                .id(entry.getId())
                .resourceId(entry.getResource().getId())
                .resourceName(entry.getResource().getName())
                .userId(entry.getAccount().getId())
                .startTime(entry.getStartTime())
                .endTime(entry.getEndTime())
                .status(entry.getStatus())
                .queuedAt(entry.getQueuedAt())
                .build();
    }
}