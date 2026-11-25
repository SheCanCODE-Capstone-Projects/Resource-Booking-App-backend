package org.example.resourcebookingappbackend.DTO; // folder (package) where this file lives

import io.swagger.v3.oas.annotations.media.Schema; // describes a field in Swagger
import jakarta.validation.constraints.NotNull; // rule: value cannot be missing

import java.time.LocalDateTime; // type for a date and time

@Schema(description = "Data sent to book a resource") // describes the whole object in Swagger
public record ReservationRequest(
        @Schema(description = "Id of the resource to book", example = "7") // Swagger description and example
        @NotNull(message = "Resource id is required") Long resourceId, // resource must be chosen
        @Schema(description = "Start of the booking", example = "2026-12-01T10:00:00") // Swagger description and example
        @NotNull(message = "Start time is required") LocalDateTime startTime, // start must be sent
        @Schema(description = "End of the booking, must be after the start", example = "2026-12-01T12:00:00") // Swagger description and example
        @NotNull(message = "End time is required") LocalDateTime endTime // end must be sent
) {
}