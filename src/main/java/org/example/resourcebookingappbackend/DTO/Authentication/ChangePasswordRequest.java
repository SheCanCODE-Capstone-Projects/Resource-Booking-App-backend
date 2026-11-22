package org.example.resourcebookingappbackend.DTO.Authentication;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest{

        @NotBlank
        private String currentPassword;

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9\\s]).{5,15}$",
                message = "Password must be 5-15 characters "
                        + "with uppercase, number and symbol"
        )
        private String newPassword;

}

