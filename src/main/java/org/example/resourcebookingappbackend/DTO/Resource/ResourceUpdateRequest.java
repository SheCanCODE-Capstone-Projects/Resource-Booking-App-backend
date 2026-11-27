package org.example.resourcebookingappbackend.DTO.Resource;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ResourceUpdateRequest {

    @NotBlank(message = "Resource name is required")
    private String name;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private String amenities;

    private String description;

    private String imageUrl;

    @NotNull(message = "Opening time is required")
    private LocalTime openingTime;

    @NotNull(message = "Closing time is required")
    private LocalTime closingTime;
}