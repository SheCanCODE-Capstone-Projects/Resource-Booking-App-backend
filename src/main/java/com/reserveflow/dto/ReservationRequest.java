package com.reserveflow.dto;

import java.time.LocalDateTime;

public class ReservationRequest {
    private int resourceId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
