package org.example.resourcebookingappbackend.DTO.Resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityResponse {

    private UUID resourceId;
    private LocalDate date;
    private List<Interval> occupiedIntervals;
    private List<Interval> availableIntervals;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Interval {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}