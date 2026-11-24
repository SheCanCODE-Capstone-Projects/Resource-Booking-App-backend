package org.example.resourcebookingappbackend.DTO.Authentication;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * DTO for {@link org.example.resourcebookingappbackend.Model.Users}
 */
@Data
public class UserRegisterRequest{


    @NotNull(message = "provide your name")
    private String firstName;

    @NotNull(message = "provide your surname")
    private String lastName;

    @Email(message = "provide the valid email")
    @Column(unique = true)
    private String userName;


    @Pattern(
            message = "Password must include at least one uppercase letter, one number, and one symbol.",
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9\\s]).{5,15}$"

    )
    @Length(min = 5, max = 15  ,message = "Password must be 5-15 characters long")
    private String password;
}