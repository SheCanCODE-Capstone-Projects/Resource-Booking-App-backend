package org.example.resourcebookingappbackend.Model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.resourcebookingappbackend.Enum.Purpose;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_otps")
@Getter
@Setter
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Purpose purpose;

    @Column(nullable = false, length = 64)
    private String codeHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private int attempts;

}

