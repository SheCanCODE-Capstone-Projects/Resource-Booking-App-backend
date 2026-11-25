package org.example.resourcebookingappbackend.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.resourcebookingappbackend.DTO.Resource.AvailabilityResponse;
import org.example.resourcebookingappbackend.DTO.Resource.ResourceCreateRequest;
import org.example.resourcebookingappbackend.DTO.Resource.ResourceResponse;
import org.example.resourcebookingappbackend.DTO.Resource.ResourceUpdateRequest;
import org.example.resourcebookingappbackend.Service.Resource.AvailabilityService;
import org.example.resourcebookingappbackend.Service.Resource.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
@Tag(
        name = "Resources",
        description = "Endpoints for creating, discovering, and managing reservable resources"
)
public class ResourceController {

    private final ResourceService resourceService;
    private final AvailabilityService availabilityService;

    public ResourceController(ResourceService resourceService, AvailabilityService availabilityService) {
        this.resourceService = resourceService;
        this.availabilityService = availabilityService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PUBLISHER')")
    @Operation(
            summary = "Create a resource",
            description = "Allows an administrator or publisher to create a new reservable resource such as a room, office, or piece of equipment. The creator becomes the resource's owner.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Resource created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResourceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid resource data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - administrator or publisher role required", content = @Content)
    })
    public ResponseEntity<ResourceResponse> createResource(@Valid @RequestBody ResourceCreateRequest request) {
        return new ResponseEntity<>(resourceService.createResource(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "List resources",
            description = "Retrieves resources, optionally filtered by category, location, minimum capacity, and active status. Accessible to any authenticated role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<ResourceResponse>> listResources(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Boolean active
    ) {
        return ResponseEntity.ok(resourceService.listResources(category, location, minCapacity, active));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get resource details",
            description = "Retrieves the details of a single resource. Accessible to any authenticated role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource found"),
            @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content)
    })
    public ResponseEntity<ResourceResponse> getResource(@PathVariable UUID id) {
        return ResponseEntity.ok(resourceService.getResource(id));
    }

    @GetMapping("/{id}/availability")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get resource availability",
            description = "Returns the occupied and available time intervals for a resource on a given date, derived from confirmed reservations. Accessible to any authenticated role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability computed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AvailabilityResponse.class))),
            @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content)
    })
    public ResponseEntity<AvailabilityResponse> getAvailability(
            @PathVariable UUID id,
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(availabilityService.getAvailability(id, date));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PUBLISHER')")
    @Operation(
            summary = "Update a resource",
            description = "Allows the owning publisher or an administrator to update resource details.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid resource data", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - not the resource owner or an administrator", content = @Content),
            @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content)
    })
    public ResponseEntity<ResourceResponse> updateResource(
            @PathVariable UUID id,
            @Valid @RequestBody ResourceUpdateRequest request
    ) {
        return ResponseEntity.ok(resourceService.updateResource(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PUBLISHER')")
    @Operation(
            summary = "Deactivate a resource",
            description = "Allows the owning publisher or an administrator to deactivate a resource, removing it from availability for new bookings.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Resource deactivated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not the resource owner or an administrator", content = @Content),
            @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content)
    })
    public ResponseEntity<Void> deactivateResource(@PathVariable UUID id) {
        resourceService.deactivateResource(id);
        return ResponseEntity.noContent().build();
    }
}