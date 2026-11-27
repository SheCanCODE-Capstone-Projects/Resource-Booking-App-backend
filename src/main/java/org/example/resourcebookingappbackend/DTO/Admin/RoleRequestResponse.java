package org.example.resourcebookingappbackend.DTO.Admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.resourcebookingappbackend.Enum.AssignableRole;
import org.example.resourcebookingappbackend.Enum.RoleRequestStatus;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequestResponse {

    private UUID id;
    private UUID accountId;
    private String userName;
    private AssignableRole requestedRole;
    private RoleRequestStatus status;
    private Timestamp requestedAt;
    private Timestamp reviewedAt;
}