package org.example.resourcebookingappbackend.Service.Authentication;

import org.example.resourcebookingappbackend.DTO.Authentication.UserResponse;
import org.example.resourcebookingappbackend.DTO.Authentication.UserUpdateRequest;
import org.example.resourcebookingappbackend.Enum.Role;
import org.example.resourcebookingappbackend.Model.Account;
import org.example.resourcebookingappbackend.Repository.AccountRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserResponse> getAllAccount() {

        return accountRepository.findAllByRole(Role.USER.toString())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('USER')")
    public UserResponse getUserProfile() {

        Account account = getAuthenticatedUser();

        return mapToResponse(account);
    }


    @PreAuthorize("hasRole('USER')")
    @Transactional
    public UserResponse updateUserProfile(UserUpdateRequest request) {

        Account account = getAuthenticatedUser();

        String fullName = request.getFirstName().trim()
                + " "
                + request.getLastName().trim();

        account.setFullName(fullName);

        Account updatedUser = accountRepository.save(account);

        return mapToResponse(updatedUser);
    }


    @PreAuthorize("hasRole('USER')")
    @Transactional
    public void deleteUserProfile() {

        Account account = getAuthenticatedUser();

        accountRepository.delete(account);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteAllAccount() {

        List<Account> account =
                accountRepository.findAllByRole(Role.USER.toString());

        accountRepository.deleteAll(account);
    }


    private Account getAuthenticatedUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {

            throw new AccessDeniedException("Authentication is required");
        }

        String username = authentication.getName();

        return accountRepository
                .findAccountByUserName(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }


    private UserResponse mapToResponse(Account account) {

        return UserResponse.builder()
                .id(account.getId())
                .userName(account.getUserName())
                .fullName(account.getFullName())
                .role(account.getRole())
                .createdAt(account.getCreatedAt())
                .build();
    }
}

