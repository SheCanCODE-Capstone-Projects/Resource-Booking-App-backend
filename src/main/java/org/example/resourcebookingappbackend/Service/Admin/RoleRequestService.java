package org.example.resourcebookingappbackend.Service.Admin;

import org.example.resourcebookingappbackend.DTO.Admin.RoleRequestResponse;
import org.example.resourcebookingappbackend.Enum.AssignableRole;
import org.example.resourcebookingappbackend.Enum.Role;
import org.example.resourcebookingappbackend.Enum.RoleRequestStatus;
import org.example.resourcebookingappbackend.Exception.ReservationConflictException;
import org.example.resourcebookingappbackend.Exception.ResourceNotFoundException;
import org.example.resourcebookingappbackend.Model.Account;
import org.example.resourcebookingappbackend.Model.RoleRequest;
import org.example.resourcebookingappbackend.Repository.AccountRepository;
import org.example.resourcebookingappbackend.Repository.RoleRequestRepository;
import org.example.resourcebookingappbackend.Security.AccessControlService;
import org.example.resourcebookingappbackend.Service.Authentication.EmailService;
import org.example.resourcebookingappbackend.Service.Notification.NotificationService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Self-service role changes: a user requests a role, an administrator approves
 * or rejects it. The account's role only ever changes on approval - there is no
 * endpoint that sets a role directly. ADMIN is never a requestable role, since
 * it is excluded from AssignableRole.
 */
@Service
public class RoleRequestService {

    private final RoleRequestRepository roleRequestRepository;
    private final AccountRepository accountRepository;
    private final AccessControlService accessControlService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public RoleRequestService(
            RoleRequestRepository roleRequestRepository,
            AccountRepository accountRepository,
            AccessControlService accessControlService,
            NotificationService notificationService,
            EmailService emailService
    ) {
        this.roleRequestRepository = roleRequestRepository;
        this.accountRepository = accountRepository;
        this.accessControlService = accessControlService;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public RoleRequestResponse requestRole(AssignableRole requestedRole) {

        Account account = accessControlService.getAuthenticatedAccount();

        if (account.getRole().name().equals(requestedRole.name())) {
            throw new IllegalArgumentException("You already have the " + requestedRole + " role");
        }

        if (roleRequestRepository.existsByAccount_IdAndStatus(account.getId(), RoleRequestStatus.PENDING)) {
            throw new ReservationConflictException("You already have a pending role request awaiting admin approval");
        }

        RoleRequest request = RoleRequest.builder()
                .account(account)
                .requestedRole(requestedRole)
                .status(RoleRequestStatus.PENDING)
                .build();

        RoleRequest saved;
        try {
            saved = roleRequestRepository.saveAndFlush(request);
        } catch (DataIntegrityViolationException ex) {
            throw new ReservationConflictException("You already have a pending role request awaiting admin approval");
        }

        emailService.sendPlainEmail(
                account.getUserName(),
                "SpaceSync - Role Request Received",
                "Hello,\n\nYour request to become a " + requestedRole
                        + " has been submitted and is now pending administrator approval."
                        + " You'll be notified once it has been reviewed.\n\nBest regards,\nSpaceSync Team"
        );

        return mapToResponse(saved);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public List<RoleRequestResponse> getMyRequests() {

        Account account = accessControlService.getAuthenticatedAccount();

        return roleRequestRepository.findAllByAccount_IdOrderByRequestedAtDesc(account.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<RoleRequestResponse> getPendingRequests() {

        return roleRequestRepository.findAllByStatusOrderByRequestedAtAsc(RoleRequestStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public RoleRequestResponse approveRequest(UUID requestId) {

        Account admin = accessControlService.getAuthenticatedAccount();
        RoleRequest request = findPendingRequestOrThrow(requestId);

        Account requester = request.getAccount();
        requester.setRole(Role.valueOf(request.getRequestedRole().name()));
        accountRepository.save(requester);

        request.setStatus(RoleRequestStatus.APPROVED);
        request.setReviewedBy(admin);
        request.setReviewedAt(Timestamp.from(Instant.now()));
        RoleRequest reviewed = roleRequestRepository.save(request);

        notificationService.notify(
                requester,
                null,
                "Your request to become a " + request.getRequestedRole() + " has been approved."
        );

        emailService.sendPlainEmail(
                requester.getUserName(),
                "SpaceSync - Role Request Approved",
                "Hello,\n\nYour request to become a " + request.getRequestedRole()
                        + " has been approved by an administrator. Your account now has the "
                        + request.getRequestedRole() + " role.\n\nBest regards,\nSpaceSync Team"
        );

        return mapToResponse(reviewed);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public RoleRequestResponse rejectRequest(UUID requestId) {

        Account admin = accessControlService.getAuthenticatedAccount();
        RoleRequest request = findPendingRequestOrThrow(requestId);

        request.setStatus(RoleRequestStatus.REJECTED);
        request.setReviewedBy(admin);
        request.setReviewedAt(Timestamp.from(Instant.now()));
        RoleRequest reviewed = roleRequestRepository.save(request);

        notificationService.notify(
                request.getAccount(),
                null,
                "Your request to become a " + request.getRequestedRole() + " has been declined."
        );

        emailService.sendPlainEmail(
                request.getAccount().getUserName(),
                "SpaceSync - Role Request Declined",
                "Hello,\n\nYour request to become a " + request.getRequestedRole()
                        + " has been declined by an administrator.\n\nBest regards,\nSpaceSync Team"
        );

        return mapToResponse(reviewed);
    }

    private RoleRequest findPendingRequestOrThrow(UUID requestId) {

        RoleRequest request = roleRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Role request not found"));

        if (request.getStatus() != RoleRequestStatus.PENDING) {
            throw new IllegalArgumentException("This role request has already been reviewed");
        }

        return request;
    }

    private RoleRequestResponse mapToResponse(RoleRequest request) {
        return RoleRequestResponse.builder()
                .id(request.getId())
                .accountId(request.getAccount().getId())
                .userName(request.getAccount().getUserName())
                .requestedRole(request.getRequestedRole())
                .status(request.getStatus())
                .requestedAt(request.getRequestedAt())
                .reviewedAt(request.getReviewedAt())
                .build();
    }
}