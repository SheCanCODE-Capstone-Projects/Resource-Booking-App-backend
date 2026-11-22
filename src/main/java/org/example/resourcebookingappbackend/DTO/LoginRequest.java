package org.example.resourcebookingappbackend.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class LoginRequest {

    private String username;

    @Length(min = 5, max = 15  ,message = "Password must be 5-15 characters long")
    private String password;
}
