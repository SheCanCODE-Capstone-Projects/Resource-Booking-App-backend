package org.example.resourcebookingappbackend.Security;

import org.example.resourcebookingappbackend.Enum.Role;
import org.example.resourcebookingappbackend.Model.Account;
import org.example.resourcebookingappbackend.Repository.AccountRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccessControlService {

    private final AccountRepository accountRepository;

    public AccessControlService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account getAuthenticatedAccount() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Authentication is required");
        }

        String username = authentication.getName();

        return accountRepository
                .findAccountByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));
    }

    public void assertOwnerOrAdmin(Account authenticated, UUID ownerId) {

        if (authenticated.getRole() == Role.ADMIN) {
            return;
        }

        if (!authenticated.getId().equals(ownerId)) {
            throw new AccessDeniedException("You do not have permission to perform this action");
        }
    }
}