package org.example.resourcebookingappbackend.DTO.Admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.resourcebookingappbackend.Enum.AssignableRole;

@Data
public class RoleRequestCreateRequest {

    @NotNull(message = "Requested role is required")
    private AssignableRole role;
}