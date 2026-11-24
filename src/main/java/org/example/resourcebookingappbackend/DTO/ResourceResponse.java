package org.example.resourcebookingappbackend.DTO; // folder (package) where this file lives

import org.example.resourcebookingappbackend.Model.ResourceCategory; // the category enum
import org.example.resourcebookingappbackend.Model.Resources; // the entity used in from()

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;


public record ResourceResponse(
        Long id,
        String name,
        ResourceCategory category,
        String location,
        Integer capacity,
        String description,
        List<String> amenities,
        String imageUrl,
        LocalTime openingTime,
        LocalTime closingTime
) {


    public static ResourceResponse from(Resources resources) {
        return new ResourceResponse(
                resources.getId(), resources.getName(), resources.getCategory(), resources.getLocation(),
                resources.getCapacity(), resources.getDescription(), splitAmenities(resources.getAmenities()),
                resources.getImageUrl(), resources.getOpeningTime(), resources.getClosingTime());
    }

    private static List<String> splitAmenities(String amenities) {
        if (amenities == null || amenities.isBlank()) {
            return List.of();
        }
        return Arrays.stream(amenities.split(",")).map(String::trim).toList();
    }
}