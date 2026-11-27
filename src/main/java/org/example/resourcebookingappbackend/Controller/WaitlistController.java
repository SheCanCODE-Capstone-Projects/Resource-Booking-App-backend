package org.example.resourcebookingappbackend.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.resourcebookingappbackend.DTO.Waitlist.WaitlistRequest;
import org.example.resourcebookingappbackend.DTO.Waitlist.WaitlistResponse;
import org.example.resourcebookingappbackend.Service.Waitlist.WaitlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/waitlist")
@Tag(
        name = "Waitlist",
        description = "Endpoints for joining, viewing, and leaving resource waiting lists"
)
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Join a waiting list",
            description = "Allows an authenticated user to join the waiting list for a resource and time interval that is currently unavailable.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<WaitlistResponse> joinWaitlist(@Valid @RequestBody WaitlistRequest request) {
        return new ResponseEntity<>(waitlistService.joinWaitlist(request), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get my waiting list entries",
            description = "Retrieves the waiting list entries belonging to the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<WaitlistResponse>> getMyWaitlistEntries() {
        return ResponseEntity.ok(waitlistService.getMyWaitlistEntries());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Leave a waiting list",
            description = "Allows the owning user to leave a waiting list entry they created.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Void> leaveWaitlist(@PathVariable UUID id) {
        waitlistService.leaveWaitlist(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resource/{resourceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PUBLISHER')")
    @Operation(
            summary = "View the waiting list for a resource",
            description = "Allows the owning publisher of the resource, or an administrator, to see who is on the waiting list, in FIFO order.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<WaitlistResponse>> getWaitlistForResource(@PathVariable UUID resourceId) {
        return ResponseEntity.ok(waitlistService.getWaitlistForResource(resourceId));
    }
}