package org.example.resourcebookingappbackend.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.resourcebookingappbackend.Enum.AssignableRole;
import org.example.resourcebookingappbackend.Enum.RoleRequestStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * A user-initiated request to change their own role (e.g. USER -> PUBLISHER).
 * The account's actual role only changes once an administrator approves the
 * request - this entity never grants ADMIN, since AssignableRole excludes it.
 */
@Entity
@Table(name = "role_request")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "requested_role", nullable = false)
    private AssignableRole requestedRole;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private RoleRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Account reviewedBy;

    @CreationTimestamp
    private Timestamp requestedAt;

    private Timestamp reviewedAt;
}