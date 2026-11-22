package org.example.resourcebookingappbackend.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.resourcebookingappbackend.DTO.Authentication.UserResponse;
import org.example.resourcebookingappbackend.DTO.Authentication.UserUpdateRequest;
import org.example.resourcebookingappbackend.Service.Authentication.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
@Tag(
        name = "User",
        description = "User profile management"
)
public class UserController {

    private final AccountService accountService;

    public UserController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get authenticated user profile",
            description = "Retrieve the profile of the currently authenticated user"
    )
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponse> getMyProfile() {
        return ResponseEntity.ok(
                accountService.getUserProfile()
        );
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all regular users",
            description = "Only administrators can retrieve users with the USER role"
    )
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(
                accountService.getAllAccount()
        );
    }

    @PatchMapping("/edit/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Update own profile",
            description = "Allows a regular user to update their full name"
    )
    public ResponseEntity<UserResponse> updateMyProfile(
            @Valid @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(
                accountService.updateUserProfile(request)
        );
    }

    @DeleteMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Delete own account",
            description = "Allows a regular user to delete their own account"
    )
    public ResponseEntity<Void> deleteUser() {
        accountService.deleteUserProfile();

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete all regular users",
            description = "Only administrators can delete accounts with the USER role"
    )
    public ResponseEntity<Void> deleteAllUsers() {
        accountService.deleteAllAccount();

        return ResponseEntity.noContent().build();
    }
}

