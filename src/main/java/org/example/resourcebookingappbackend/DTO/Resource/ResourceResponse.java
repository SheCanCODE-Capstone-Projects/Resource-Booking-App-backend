package org.example.resourcebookingappbackend.DTO.Resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceResponse {

    private UUID id;
    private UUID ownerId;
    private String name;
    private String category;
    private String location;
    private Integer capacity;
    private String amenities;
    private String description;
    private String imageUrl;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private boolean active;
    private Timestamp createdAt;
}