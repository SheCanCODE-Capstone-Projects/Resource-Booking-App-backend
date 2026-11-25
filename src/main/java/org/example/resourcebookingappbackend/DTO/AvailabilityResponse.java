package org.example.resourcebookingappbackend.DTO; // folder (package) where this file lives

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


public record AvailabilityResponse(
        Long resourceId,
        LocalDate date,
        LocalTime openingTime,
        LocalTime closingTime,
        List<TimeSlot> bookedSlots
) {

    public record TimeSlot(LocalTime start, LocalTime end) {
    }
}