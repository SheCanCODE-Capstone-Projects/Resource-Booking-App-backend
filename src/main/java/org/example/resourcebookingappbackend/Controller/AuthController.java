package org.example.resourcebookingappbackend.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.example.resourcebookingappbackend.DTO.Authentication.*;
import org.example.resourcebookingappbackend.Service.Authentication.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

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

        response.sendRedirect("/oauth2/authorization/google");
    }


    @PostMapping("/verify-email")
    @Operation(
            summary = "Verify user email",
            description = "Verifies the registered email using a six-digit OTP."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        boolean verified = authService.verifyEmailResult(request);

        if (!verified) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Invalid or expired OTP"
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Email verified. You may now log in."
                )
        );
    }


    @PostMapping("/resend-verification")
    @Operation(
            summary = "Resend verification OTP",
            description =
                    """
                    this endpoint is going to be accessed in this case:.
                            - The user registered but did not receive the OTP email.
                            - The OTP expired after 10 minutes
                            - The user deleted or lost the previous OTP
                            - The user entered the wrong OTP too many times (5 failed attempts).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verification request processed"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "503", description = "Email service unavailable")
    })
    public ResponseEntity<Map<String, String>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "If eligible, a verification code has been sent."
                )
        );
    }


    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request password reset",
            description = "Sends a password recovery OTP to the registered email."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password recovery request processed"),
            @ApiResponse(responseCode = "400", description = "Invalid email address"),
            @ApiResponse(responseCode = "503", description = "Email service unavailable")
    })
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "If eligible, a recovery code has been sent."
                )
        );
    }


    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password",
            description = "Resets the  forgotten user's password using a valid email OTP and a new password."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        boolean success = authService.resetPassword(request);

        if (!success) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Invalid or expired OTP"
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password reset successfully."
                )
        );
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change-password")
    @Operation(
            summary = "Change password",
            description = "Allows an authenticated user to change their password using the current and new passwords.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid password details"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password changed successfully."
                )
        );
    }
}