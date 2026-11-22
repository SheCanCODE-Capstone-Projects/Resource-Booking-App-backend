package org.example.resourcebookingappbackend.DTO.Authentication;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.resourcebookingappbackend.Enum.Role;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String userName;

    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Timestamp createdAt;
}
