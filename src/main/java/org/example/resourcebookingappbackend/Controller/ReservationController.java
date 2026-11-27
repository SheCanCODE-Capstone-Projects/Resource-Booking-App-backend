package org.example.resourcebookingappbackend.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.resourcebookingappbackend.DTO.Reservation.ReservationRequest;
import org.example.resourcebookingappbackend.DTO.Reservation.ReservationResponse;
import org.example.resourcebookingappbackend.Service.Reservation.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@Tag(
        name = "Reservations",
        description = "Endpoints for creating and managing resource reservations"
)
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Create a reservation",
            description = "Allows an authenticated user to book a resource for a given time interval. Overlapping confirmed reservations for the same resource are rejected.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reservation created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid reservation data, such as start time after end time or outside operating hours",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The requested time slot overlaps an existing confirmed reservation",
                    content = @Content
            )
    })
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        return new ResponseEntity<>(reservationService.createReservation(request), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get my reservations",
            description = "Retrieves the reservations belonging to the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<ReservationResponse>> getMyReservations() {
        return ResponseEntity.ok(reservationService.getMyReservations());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get reservation details",
            description = "Retrieves a reservation. Accessible to the owning user or an administrator.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not the reservation owner or an administrator", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reservation not found", content = @Content)
    })
    public ResponseEntity<ReservationResponse> getReservation(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.getReservation(id));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Cancel a reservation",
            description = "Allows the owning user or an administrator to cancel a confirmed reservation. Cancellation automatically evaluates the resource's waiting list for FIFO promotion.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation canceled successfully"),
            @ApiResponse(responseCode = "400", description = "Reservation is not in a cancelable state", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - not the reservation owner or an administrator", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reservation not found", content = @Content)
    })
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }
}