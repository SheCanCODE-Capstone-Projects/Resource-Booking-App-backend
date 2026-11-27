package org.example.resourcebookingappbackend.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "resource")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Account owner;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String category;

    @NotBlank
    @Column(nullable = false)
    private String location;

    @NotNull
    @Column(nullable = false)
    private Integer capacity;

    private String amenities;

    @Column(length = 2000)
    private String description;

    private String imageUrl;

    @NotNull
    @Column(nullable = false)
    private LocalTime openingTime;

    @NotNull
    @Column(nullable = false)
    private LocalTime closingTime;

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp
    private Timestamp createdAt;
}