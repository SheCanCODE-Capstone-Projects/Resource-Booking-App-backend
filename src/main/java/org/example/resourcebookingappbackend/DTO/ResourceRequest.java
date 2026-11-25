package org.example.resourcebookingappbackend.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.resourcebookingappbackend.Model.ResourceCategory;

import java.time.LocalTime;
import java.util.List;

public record ResourceRequest(
        @NotBlank(message = "Name is required") @Size(max = 500) String name,
        @NotNull(message = "Category is required") ResourceCategory category,
        @NotBlank(message = "Location is required") @Size(max = 255) String location,
        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1") Integer capacity,
        @Size(max = 1000) String description,
        List<String> amenities,
        @Size(max = 1000) String imageUrl,
        @NotNull(message = "Opening time is required") LocalTime openingTime,
        @NotNull(message = "Closing time is required") LocalTime closingTime
) {
}