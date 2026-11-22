package org.example.resourcebookingappbackend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterResponse {

    private HttpStatus status;
    private String username;
    private String message;
}
