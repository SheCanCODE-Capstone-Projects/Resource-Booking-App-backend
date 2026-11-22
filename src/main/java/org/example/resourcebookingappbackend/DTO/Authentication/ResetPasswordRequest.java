package org.example.resourcebookingappbackend.DTO.Authentication;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank(message = "Reset token is required")
    private String token;

    @NotBlank(message = "New password is required")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9\\s]).{5,15}$",
            message = "Password must be 5-15 characters long and contain an uppercase letter, number, and symbol"
    )
    private String newPassword;

}
