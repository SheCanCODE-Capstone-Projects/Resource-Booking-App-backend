package org.example.resourcebookingappbackend.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.resourcebookingappbackend.DTO.LoginRequest;
import org.example.resourcebookingappbackend.DTO.LoginResponse;
import org.example.resourcebookingappbackend.DTO.UserRegisterRequest;
import org.example.resourcebookingappbackend.DTO.UserRegisterResponse;
import org.example.resourcebookingappbackend.Service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Authentication",
        description = "Endpoints for user registration, login, and authentication token management"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user account",
            description = "Creates a new user account after validating input fields (such as email format and password complexity) and returns the created user details."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserRegisterResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or password does not meet validation rules",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "An account with the provided email or username already exists",
                    content = @Content
            )
    })
    public ResponseEntity<UserRegisterResponse> registerUser(@Valid @RequestBody UserRegisterRequest registerRequest) {
        return new ResponseEntity<>(authService.register(registerRequest), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user and generate token",
            description = "Validates user credentials and returns an authentication token (e.g., JWT) along with user session details."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing or malformed login credentials",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email/username or password",
                    content = @Content
            )
    })
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        return new ResponseEntity<>(authService.login(loginRequest), HttpStatus.OK);
    }


    @GetMapping("/google")
    @Operation(
            summary = "Sign-up with google",
            description = "when continuing with the google profile that exist instead of manual registration"
    )

    public void googleLogin(HttpServletResponse response)
            throws IOException {

        response.sendRedirect("oauth2/authorization/google");
    }

//    @PatchMapping
//    public ResponseEntity<UserRegisterResponse> updateProfile(@Valid @RequestBody UserRegisterRequest registerRequest) {}
}