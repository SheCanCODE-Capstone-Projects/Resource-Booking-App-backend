package org.example.resourcebookingappbackend.Service.Resource;

import org.example.resourcebookingappbackend.DTO.Resource.AvailabilityResponse;
import org.example.resourcebookingappbackend.Enum.ReservationStatus;
import org.example.resourcebookingappbackend.Exception.ResourceNotFoundException;
import org.example.resourcebookingappbackend.Model.Reservation;
import org.example.resourcebookingappbackend.Model.Resource;
import org.example.resourcebookingappbackend.Repository.ReservationRepository;
import org.example.resourcebookingappbackend.Repository.ResourceRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AvailabilityService {

    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;

    public AvailabilityService(ResourceRepository resourceRepository, ReservationRepository reservationRepository) {
        this.resourceRepository = resourceRepository;
        this.reservationRepository = reservationRepository;
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public AvailabilityResponse getAvailability(UUID resourceId, LocalDate date) {

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        LocalDateTime dayStart = date.atTime(resource.getOpeningTime());
        LocalDateTime dayEnd = date.atTime(resource.getClosingTime());

        List<Reservation> confirmed = reservationRepository.findOverlapping(
                resourceId, dayStart, dayEnd, ReservationStatus.CONFIRMED
        );

        List<AvailabilityResponse.Interval> occupied = confirmed.stream()
                .map(r -> AvailabilityResponse.Interval.builder()
                        .startTime(r.getStartTime().isBefore(dayStart) ? dayStart : r.getStartTime())
                        .endTime(r.getEndTime().isAfter(dayEnd) ? dayEnd : r.getEndTime())
                        .build())
                .toList();

        List<AvailabilityResponse.Interval> available = new ArrayList<>();
        LocalDateTime cursor = dayStart;

        for (AvailabilityResponse.Interval interval : occupied) {
            if (interval.getStartTime().isAfter(cursor)) {
                available.add(AvailabilityResponse.Interval.builder()
                        .startTime(cursor)
                        .endTime(interval.getStartTime())
                        .build());
            }
            if (interval.getEndTime().isAfter(cursor)) {
                cursor = interval.getEndTime();
            }
        }

        if (cursor.isBefore(dayEnd)) {
            available.add(AvailabilityResponse.Interval.builder()
                    .startTime(cursor)
                    .endTime(dayEnd)
                    .build());
        }

        return AvailabilityResponse.builder()
                .resourceId(resourceId)
                .date(date)
                .occupiedIntervals(occupied)
                .availableIntervals(available)
                .build();
    }
}