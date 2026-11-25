package org.example.resourcebookingappbackend.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.resourcebookingappbackend.DTO.Admin.RoleRequestCreateRequest;
import org.example.resourcebookingappbackend.DTO.Admin.RoleRequestResponse;
import org.example.resourcebookingappbackend.Service.Admin.RoleRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/role-requests")
@Tag(
        name = "Role Requests",
        description = "Self-service role change requests, reviewed by an administrator. ADMIN is never a requestable or assignable role."
)
public class RoleRequestController {

    private final RoleRequestService roleRequestService;

    public RoleRequestController(RoleRequestService roleRequestService) {
        this.roleRequestService = roleRequestService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Request a role change",
            description = "Allows the authenticated user to request becoming a PUBLISHER or reverting to USER. The request stays PENDING until an administrator approves or rejects it; the account's role does not change until then.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<RoleRequestResponse> requestRole(@Valid @RequestBody RoleRequestCreateRequest request) {
        return new ResponseEntity<>(roleRequestService.requestRole(request.getRole()), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get my role requests",
            description = "Retrieves the authenticated user's own role request history.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<RoleRequestResponse>> getMyRequests() {
        return ResponseEntity.ok(roleRequestService.getMyRequests());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get pending role requests",
            description = "Allows an administrator to list all role requests awaiting review.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<RoleRequestResponse>> getPendingRequests() {
        return ResponseEntity.ok(roleRequestService.getPendingRequests());
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Approve a role request",
            description = "Allows an administrator to approve a pending role request, applying the requested role to the account.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<RoleRequestResponse> approveRequest(@PathVariable UUID id) {
        return ResponseEntity.ok(roleRequestService.approveRequest(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Reject a role request",
            description = "Allows an administrator to reject a pending role request. The account's role is left unchanged.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<RoleRequestResponse> rejectRequest(@PathVariable UUID id) {
        return ResponseEntity.ok(roleRequestService.rejectRequest(id));
    }
}